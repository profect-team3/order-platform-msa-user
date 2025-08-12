package app.domain.customer.dto.response;

import app.domain.user.model.entity.UserAddress;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCustomerAddressListResponse {

	private String alias;
	private String address;
	private String addressDetail;
	private boolean isDefault;

	public GetCustomerAddressListResponse() {
	}

	public GetCustomerAddressListResponse(String alias, String address, String addressDetail, boolean isDefault) {
		this.alias = alias;
		this.address = address;
		this.addressDetail = addressDetail;
		this.isDefault = isDefault;
	}

	public static GetCustomerAddressListResponse from(UserAddress address) {
		return GetCustomerAddressListResponse.builder()
			.alias(address.getAlias())
			.address(address.getAddress())
			.addressDetail(address.getAddressDetail())
			.isDefault(address.isDefault())
			.build();
	}
}