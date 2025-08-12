package app.manager.dto.response;

import java.time.LocalDateTime;

import app.user.model.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCustomerListResponse {

	private Long id;
	private String email;
	private String name;
	private LocalDateTime createdAt;

	public GetCustomerListResponse() {
	}

	public GetCustomerListResponse(Long id, String email, String name, LocalDateTime createdAt) {
		this.id = id;
		this.email = email;
		this.name = name;
		this.createdAt = createdAt;
	}

	public static GetCustomerListResponse from(User user) {
		return GetCustomerListResponse.builder()
			.id(user.getUserId())
			.email(user.getEmail())
			.name(user.getUsername())
			.createdAt(user.getCreatedAt())
			.build();
	}
}