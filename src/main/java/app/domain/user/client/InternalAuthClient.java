package app.domain.user.client;

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
public class InternalAuthClient {

	private final RestTemplate restTemplate;

	@Value("${auth.service.url:http://localhost:8083}")
	private String authServiceUrl;

	public ApiResponse<String> logout() {
		String url = authServiceUrl + "/internal/auth/logout";

		ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
			url,
			HttpMethod.POST,
			null,
			new ParameterizedTypeReference<ApiResponse<String>>() {}
		);

		return response.getBody();
	}
}
