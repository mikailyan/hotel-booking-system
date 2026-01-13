package com.example.booking.hotelclient;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HotelClient {
  private static final Logger log = LoggerFactory.getLogger(HotelClient.class);

  private final RestTemplate hotelRestTemplate;

  @Value("${hotel.client.retries:3}")
  private int retries;

  @Value("${hotel.client.backoff-ms:200}")
  private long baseBackoffMs;

  public List<RoomDto> getRecommendedRooms(String bearerToken, LocalDate startDate, LocalDate endDate) {
    String url = UriComponentsBuilder
            .fromHttpUrl("http://hotel-service/api/rooms/recommend")
            .queryParam("startDate", startDate)
            .queryParam("endDate", endDate)
            .toUriString();

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

    ResponseEntity<List<RoomDto>> resp = hotelRestTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<>() {}
    );
    return resp.getBody() == null ? List.of() : resp.getBody();
  }

  public void confirmAvailability(Long roomId, ConfirmAvailabilityRequest req, String bearerToken) {
    callWithRetry("confirmAvailability", () -> {
      String url = "http://hotel-service/internal/rooms/" + roomId + "/confirm-availability";
      HttpHeaders headers = new HttpHeaders();
      headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
      headers.setContentType(MediaType.APPLICATION_JSON);

      hotelRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(req, headers), Void.class);
    });
  }

  public void release(Long roomId, ReleaseRequest req, String bearerToken) {
    callWithRetry("release", () -> {
      String url = "http://hotel-service/internal/rooms/" + roomId + "/release";
      HttpHeaders headers = new HttpHeaders();
      headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
      headers.setContentType(MediaType.APPLICATION_JSON);

      hotelRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(req, headers), Void.class);
    });
  }

  private void callWithRetry(String op, Runnable action) {
    int attempt = 0;
    while (true) {
      attempt++;
      try {
        action.run();
        return;
      } catch (HttpStatusCodeException e) {
        int code = e.getStatusCode().value();
        if (code == 409 || code == 404 || code == 400) throw e; // бизнес-ошибки не ретраим
        if (attempt >= retries) throw e;
        backoff(op, attempt, e);
      } catch (ResourceAccessException e) {
        if (attempt >= retries) throw e;
        backoff(op, attempt, e);
      }
    }
  }

  private void backoff(String op, int attempt, Exception e) {
    long sleep = (long) (baseBackoffMs * Math.pow(2, attempt - 1));
    log.warn("HotelClient op={} attempt={} failed: {}. Backoff {}ms", op, attempt, e.getClass().getSimpleName(), sleep);
    try { Thread.sleep(sleep); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
  }
}
