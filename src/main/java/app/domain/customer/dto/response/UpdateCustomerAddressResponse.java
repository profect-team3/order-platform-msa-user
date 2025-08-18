package app.domain.customer.dto.response;

import app.domain.user.model.entity.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCustomerAddressResponse {
	private String alias;
	private String address;
	private String addressDetail;
	private boolean isDefault;

	public static UpdateCustomerAddressResponse from(UserAddress userAddress) {
		return UpdateCustomerAddressResponse.builder()
			.alias(userAddress.getAlias())
			.address(userAddress.getAddress())
			.addressDetail(userAddress.getAddressDetail())
			.isDefault(userAddress.isDefault())
			.build();
	}
}
