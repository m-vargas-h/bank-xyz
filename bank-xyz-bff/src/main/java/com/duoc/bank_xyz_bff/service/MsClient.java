package com.duoc.bank_xyz_bff.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MsClient {

    private final RestClient restClient;

    public MsClient(RestClient msRestClient) {
        this.restClient = msRestClient;
    }

    @Retry(name = "ms")
    @CircuitBreaker(name = "ms")
    public <T> T get(String url, ParameterizedTypeReference<T> type) {
        return restClient.get().uri(url).retrieve().body(type);
    }

    @CircuitBreaker(name = "ms")
    public <T> T post(String url, Object body, ParameterizedTypeReference<T> type) {
        return restClient.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().body(type);
    }
}