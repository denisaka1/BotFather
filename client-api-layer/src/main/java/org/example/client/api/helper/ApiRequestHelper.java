package org.example.client.api.helper;

import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@AllArgsConstructor
@Component
public class ApiRequestHelper {

    private final RestTemplate restTemplate;

    public <T> T get(String url, Class<T> responseType, Map<String, String> queryParams) {
        try {
            // Build query parameters
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
            if (queryParams != null) {
                queryParams.forEach(uriBuilder::queryParam);
            }

            String fullUrl = uriBuilder.toUriString();
            ResponseEntity<T> responseEntity = restTemplate.getForEntity(fullUrl, responseType);
            return responseEntity.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Get request failed: " + e.getMessage());
        }
    }

    public <T> T get(String url, Class<T> responseType) {
        return get(url, responseType, null);
    }

    public <T, R> T post(String url, R requestBody, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<R> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<T> responseEntity = restTemplate.postForEntity(url, entity, responseType);
            return responseEntity.getBody();
        } catch (Exception e) {
            // Handle exception (log it or rethrow)
            throw new RuntimeException("POST request failed: " + e.getMessage(), e);
        }
    }

    public <T> T delete(String url, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            if (responseType == null) {
                restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
                return null; // Explicitly return null for void responses
            } else {
                return restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType).getBody();
            }
        } catch (Exception e) {
            throw new RuntimeException("DELETE request failed: " + e.getMessage(), e);
        }
    }

    public <T> void delete(String url, T requestBody) {
        delete(url, requestBody, null);
    }

    public <T> T delete(String url, T requestBody, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);

            if (responseType != null) {
                ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType);
                return response.getBody();
            } else {
                restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
                return null;
            }
        } catch (Exception e) {
            // Handle exceptions (log or rethrow)
            throw new RuntimeException("DELETE request failed: " + e.getMessage(), e);
        }
    }

    public <T, R> T put(String url, R requestBody, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<R> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<T> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    responseType
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            // Handle exception (log it or rethrow)
            throw new RuntimeException("PUT request failed: " + e.getMessage(), e);
        }
    }

    public <T, R> T patch(String url, R requestBody, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<R> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<T> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    responseType
            );

            return responseEntity.getBody();
        } catch (Exception e) {
            // Handle exception (log or rethrow)
            throw new RuntimeException("PATCH request failed: " + e.getMessage(), e);
        }
    }
}