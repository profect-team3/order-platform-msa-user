package app.domain.customer;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.request.UpdateCustomerAddressRequest;
import app.domain.customer.dto.response.AddCustomerAddressResponse;
import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.domain.customer.dto.response.UpdateCustomerAddressResponse;
import app.domain.customer.status.CustomerErrorStatus;
import app.domain.customer.status.CustomerSuccessStatus;
import app.commonUtil.apiPayload.ApiResponse;
import app.commonUtil.apiPayload.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "User Address", description = "address 추가 및 조회")
@RestController
@RequestMapping("/user/address")
@RequiredArgsConstructor
public class CustomerAddressController {
	private final CustomerAddressService customerAddressService;

	@GetMapping("/list")
	@Operation(summary = "사용자 주소지 목록 조회", description = "사용자 주소지 목록 조회")
	public ApiResponse<List<GetCustomerAddressListResponse>> GetCustomerAddresses(Authentication authentication) {
		return ApiResponse.onSuccess(CustomerSuccessStatus.ADDRESS_LIST_FOUND,
			customerAddressService.getCustomerAddresses(authentication));
	}

	@PostMapping("/add")
	@Operation(summary = "사용자 주소지 등록", description = "사용자 주소지 등록")
	public ApiResponse<AddCustomerAddressResponse> AddCustomerAddress(
		@RequestBody @Valid AddCustomerAddressRequest request, Authentication authentication) {
		validateAddCustomerRequest(request);
		AddCustomerAddressResponse response = customerAddressService.addCustomerAddress(request,authentication);
		return ApiResponse.onSuccess(CustomerSuccessStatus.ADDRESS_ADDED, response);
	}

	private void validateAddCustomerRequest(AddCustomerAddressRequest request) {
		if (!StringUtils.hasText(request.getAlias())) {
			throw new GeneralException(CustomerErrorStatus.ADDRESS_ALIAS_INVALID);
		}
		if (!StringUtils.hasText(request.getAddress())) {
			throw new GeneralException(CustomerErrorStatus.ADDRESS_ADDRESS_INVALID);
		}
		if (!StringUtils.hasText(request.getAddressDetail())) {
			throw new GeneralException(CustomerErrorStatus.ADDRESS_ADDRESSDETAIL_INVALID);
		}
	}

	@PutMapping("/update/{addressId}")
	@Operation(summary = "사용자 주소지 수정", description = "사용자 주소지 수정")
	public ApiResponse<UpdateCustomerAddressResponse> updateCustomerAddress(
		@PathVariable UUID addressId,
		@RequestBody @Valid UpdateCustomerAddressRequest request,
		Authentication authentication) {
		validateUpdateCustomerRequest(request);
		UpdateCustomerAddressResponse response = customerAddressService.updateAddress(addressId, request, authentication);
		return ApiResponse.onSuccess(CustomerSuccessStatus.ADDRESS_UPDATE, response);
	}

	private void validateUpdateCustomerRequest(UpdateCustomerAddressRequest request) {
		if (request.getAlias() == null && request.getAddress() == null && request.getAddressDetail() == null) {
			throw new GeneralException(CustomerErrorStatus.ADDRESS_UPDATE_FAILED);
		}
	}

	@GetMapping("/{addressId}")
	@Operation(summary = "사용자 주소지 삭제", description = "사용자 주소지 삭제")
	public ApiResponse<String> deleteCustomerAddress(
		@PathVariable("addressId") UUID addressId,
		Authentication authentication) {
		String result = customerAddressService.deleteAddress(addressId, authentication);
		return ApiResponse.onSuccess(CustomerSuccessStatus.ADDRESS_DELETE, result);
	}
}