package app.manager.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import app.customer.dto.response.GetCustomerAddressListResponse;
import app.user.model.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCustomerDetailResponse {

	private Long userId;
	private String email;
	private String userName;
	private String name;
	private String nickName;
	private String phoneNumber;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<GetCustomerAddressListResponse> address;

	public GetCustomerDetailResponse() {
	}

	public GetCustomerDetailResponse(Long userId, String email, String userName, String name, String nickName, String phoneNumber, LocalDateTime createdAt, LocalDateTime updatedAt, List<GetCustomerAddressListResponse> address) {
		this.userId = userId;
		this.email = email;
		this.userName = userName;
		this.name = name;
		this.nickName = nickName;
		this.phoneNumber = phoneNumber;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.address = address;
	}

	public static GetCustomerDetailResponse from(User user, List<GetCustomerAddressListResponse> addressList) {
		return GetCustomerDetailResponse.builder()
			.userId(user.getUserId())
			.email(user.getEmail())
			.userName(user.getUsername())
			.name(user.getRealName())
			.nickName(user.getNickname())
			.phoneNumber(user.getPhoneNumber())
			.createdAt(user.getCreatedAt())
			.updatedAt(user.getUpdatedAt())
			.address(addressList)
			.build();
	}
}