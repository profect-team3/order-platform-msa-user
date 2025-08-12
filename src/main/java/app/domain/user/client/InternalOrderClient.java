package app.domain.user.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternalOrderClient {

	private final RestTemplate restTemplate;

	@Value("${order.service.url:http://localhost:8084}")
	private String orderServiceUrl;

	public String createCart(Long userId) {
		String url = orderServiceUrl + "internal/cart/" + userId;

		return restTemplate.postForObject(url, null, String.class);
	}
}
