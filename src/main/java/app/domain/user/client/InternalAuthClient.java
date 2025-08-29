package app.domain.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import app.commonUtil.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternalAuthClient {

	private final RestTemplate restTemplate;

	@Value("${auth.service.url:http://localhost:8083}")
	private String authServiceUrl;

	public ApiResponse<Void> logout() {
		String url = authServiceUrl + "/logout";

		ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
			url,
			HttpMethod.POST,
			null,
			new ParameterizedTypeReference<ApiResponse<Void>>() {}
		);
		return response.getBody();
	}
}
