package app.domain.customer;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.commonUtil.security.TokenPrincipalParser;
import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.request.UpdateCustomerAddressRequest;
import app.domain.customer.dto.response.AddCustomerAddressResponse;
import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.domain.customer.dto.response.UpdateCustomerAddressResponse;
import app.domain.customer.status.CustomerErrorStatus;
import app.domain.user.model.UserRepository;
import app.domain.user.status.UserErrorStatus;
import app.commonUtil.apiPayload.code.status.ErrorStatus;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.user.model.UserAddressRepository;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.UserAddress;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerAddressService {

	private final UserAddressRepository userAddressRepository;
	private final UserRepository userRepository;
	private final TokenPrincipalParser tokenPrincipalParser;

	@Transactional(readOnly = true)
	public List<GetCustomerAddressListResponse> getCustomerAddresses(Authentication authentication){
		String userIdStr = tokenPrincipalParser.getUserId(authentication);
		Long userId = Long.parseLong(userIdStr);

		try {
			return userAddressRepository.findAllByUserUserId(userId)
					.stream()
					.map(GetCustomerAddressListResponse::from)
					.toList();
		} catch (DataAccessException e) {
			throw new GeneralException(CustomerErrorStatus.ADDRESS_READ_FAILED);
		}
	}

	@Transactional
	public AddCustomerAddressResponse addCustomerAddress(AddCustomerAddressRequest request,Authentication authentication) {
		String userIdStr = tokenPrincipalParser.getUserId(authentication);
		Long userId = Long.parseLong(userIdStr);

		User user = userRepository.findByUserId(userId)
			.orElseThrow(()-> new GeneralException(ErrorStatus.USER_NOT_FOUND));
		if (userAddressRepository.existsByUserAndAddressAndAddressDetail(user, request.getAddress(), request.getAddressDetail())) {
			throw new GeneralException(CustomerErrorStatus.ADDRESS_ALREADY_EXISTS);
		}

		boolean finalIsDefault = request.isDefault();

		if (!finalIsDefault) {
			if (userAddressRepository.findAllByUserUserId(user.getUserId()).isEmpty()) {
				finalIsDefault = true;
			}
		}

		if (finalIsDefault) {
			userAddressRepository.findByUser_UserIdAndIsDefaultTrue(user.getUserId())
					.ifPresent(existingDefault -> {
						existingDefault.setDefault(false);
						userAddressRepository.save(existingDefault);
					});
		}

		UserAddress address = UserAddress.builder()
				.user(user)
				.alias(request.getAlias())
				.address(request.getAddress())
				.addressDetail(request.getAddressDetail())
				.isDefault(finalIsDefault)
				.build();

		try {
			UserAddress savedAddress = userAddressRepository.save(address);
			if (savedAddress.getAddressId() == null) {
				throw new GeneralException(CustomerErrorStatus.ADDRESS_ADD_FAILED);
			}

			return new AddCustomerAddressResponse(savedAddress.getAddressId());
		} catch (DataAccessException e) {
			throw new GeneralException(CustomerErrorStatus.ADDRESS_ADD_FAILED);
		}
	}

	@Transactional
	public UpdateCustomerAddressResponse updateAddress(UUID addressId, UpdateCustomerAddressRequest req, Authentication authentication) {
		String userIdStr = tokenPrincipalParser.getUserId(authentication);
		Long userId = Long.parseLong(userIdStr);

		UserAddress addressToUpdate = userAddressRepository.findById(addressId)
			.orElseThrow(() -> new GeneralException(UserErrorStatus.ADDRESS_NOT_FOUND));

		if (addressToUpdate.getDeletedAt() != null) {
			throw new GeneralException(UserErrorStatus.ADDRESS_ALREADY_DELETED);
		}

		if (!addressToUpdate.getUser().getUserId().equals(userId)) {
			throw new GeneralException(UserErrorStatus.ADDRESS_ACCESS_DENIED);
		}

		UserAddress updatedAddress = addressToUpdate.update(req);
		return UpdateCustomerAddressResponse.from(updatedAddress);
	}

	@Transactional
	public String deleteAddress(UUID addressId, Authentication authentication) {
		String userIdStr = tokenPrincipalParser.getUserId(authentication);
		Long userId = Long.parseLong(userIdStr);

		UserAddress addressToDelete = userAddressRepository.findById(addressId)
			.orElseThrow(() -> new GeneralException(UserErrorStatus.ADDRESS_NOT_FOUND));

		if (!addressToDelete.getUser().getUserId().equals(userId)) {
			throw new GeneralException(UserErrorStatus.ADDRESS_ACCESS_DENIED);
		}

		if (addressToDelete.isDefault()) {
			throw new GeneralException(UserErrorStatus.CANNOT_DELETE_DEFAULT_ADDRESS);
		}

		userAddressRepository.delete(addressToDelete);
		return "Address deleted successfully";
	}
}
