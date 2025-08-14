package app.domain.user.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import app.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternalOrderClient {

	private final RestTemplate restTemplate;

	@Value("${order.service.url:http://localhost:8084}")
	private String orderServiceUrl;

	public ApiResponse<String> createCart(Long userId) {
		String url = orderServiceUrl + "/internal/cart/" + userId;

		ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
			url,
			HttpMethod.POST,
			null,
			new ParameterizedTypeReference<ApiResponse<String>>() {}
		);

		return response.getBody();
	}
}
