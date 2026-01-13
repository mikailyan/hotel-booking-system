package com.example.booking.hotelclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HotelClientConfig {

  @Bean
  @LoadBalanced
  RestTemplate hotelRestTemplate(@Value("${hotel.client.connect-timeout-ms:500}") int connectTimeoutMs,
                                 @Value("${hotel.client.read-timeout-ms:2000}") int readTimeoutMs) {
    SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
    f.setConnectTimeout(connectTimeoutMs);
    f.setReadTimeout(readTimeoutMs);
    return new RestTemplate(f);
  }
}
