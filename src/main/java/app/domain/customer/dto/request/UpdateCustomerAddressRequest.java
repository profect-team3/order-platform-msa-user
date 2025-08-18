package app.domain.customer.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class UpdateCustomerAddressRequest {

	private String alias;

	private String address;

	private String addressDetail;

	@JsonProperty("isDefault")
	private Boolean isDefault;

	public UpdateCustomerAddressRequest() {
	}

	public UpdateCustomerAddressRequest(String alias, String address, String addressDetail, Boolean isDefault) {
		this.alias = alias;
		this.address = address;
		this.addressDetail = addressDetail;
		this.isDefault = isDefault;
	}
}
