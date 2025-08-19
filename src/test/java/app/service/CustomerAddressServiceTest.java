package app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.core.Authentication;

import app.commonSecurity.TokenPrincipalParser;
import app.domain.customer.CustomerAddressService;
import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.request.UpdateCustomerAddressRequest;
import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.domain.customer.dto.response.UpdateCustomerAddressResponse;
import app.domain.customer.status.CustomerErrorStatus;
import app.domain.user.model.UserAddressRepository;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.UserAddress;
import app.domain.user.status.UserErrorStatus;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerAddressService Test")
public class CustomerAddressServiceTest {

	@InjectMocks
	private CustomerAddressService customerAddressService;

	@Mock
	private UserAddressRepository userAddressRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TokenPrincipalParser tokenPrincipalParser;

	@Mock
	private Authentication authentication;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = User.builder().userId(1L).username("tester").build();
		lenient().when(tokenPrincipalParser.getUserId(authentication)).thenReturn(String.valueOf(testUser.getUserId()));
		lenient().when(userRepository.findByUserId(testUser.getUserId())).thenReturn(Optional.of(testUser));
	}

	@Nested
	@DisplayName("주소 목록 조회 (getCustomerAddresses)")
	class GetCustomerAddresses {

		@Test
		@DisplayName("성공 - 사용자의 주소 목록을 정상적으로 조회")
		void success_ReturnsAddressList() {
			// given
			List<UserAddress> mockAddressList = List.of(
				UserAddress.builder().alias("집").build(),
				UserAddress.builder().alias("회사").build()
			);
			when(userAddressRepository.findAllByUserUserId(testUser.getUserId())).thenReturn(mockAddressList);

			// when
			List<GetCustomerAddressListResponse> result = customerAddressService.getCustomerAddresses(authentication);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).getAlias()).isEqualTo("집");
			verify(userAddressRepository).findAllByUserUserId(testUser.getUserId());
		}

		@Test
		@DisplayName("실패 - DB 조회 오류")
		void failure_DbError_ThrowsException() {
			// given
			when(userAddressRepository.findAllByUserUserId(testUser.getUserId()))
				.thenThrow(new DataAccessResourceFailureException("DB error"));

			// when & then
			assertThatThrownBy(() -> customerAddressService.getCustomerAddresses(authentication))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(CustomerErrorStatus.ADDRESS_READ_FAILED.getCode());
		}
	}

	@Nested
	@DisplayName("주소 추가 (addCustomerAddress)")
	class AddCustomerAddress {

		@Test
		@DisplayName("성공 - 첫 주소 추가 시 자동으로 기본 주소로 설정")
		void success_FirstAddressBecomesDefault() {
			// given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest("집", "서울", "101호", false);
			when(userAddressRepository.findAllByUserUserId(testUser.getUserId())).thenReturn(Collections.emptyList());
			ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
			UserAddress savedAddress = UserAddress.builder().addressId(UUID.randomUUID()).build();
			when(userAddressRepository.save(any(UserAddress.class))).thenReturn(savedAddress);

			// when
			customerAddressService.addCustomerAddress(request, authentication);

			// then
			verify(userAddressRepository).save(captor.capture());
			assertThat(captor.getValue().isDefault()).isTrue();
		}

		@Test
		@DisplayName("실패 - 이미 존재하는 주소")
		void failure_AddressAlreadyExists_ThrowsException() {
			// given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest("집", "서울", "101호", false);
			when(userAddressRepository.existsByUserAndAddressAndAddressDetail(testUser, request.getAddress(), request.getAddressDetail()))
				.thenReturn(true);

			// when & then
			assertThatThrownBy(() -> customerAddressService.addCustomerAddress(request, authentication))
				.isInstanceOf(GeneralException.class)
				.extracting("code")
				.isEqualTo(CustomerErrorStatus.ADDRESS_ALREADY_EXISTS);
		}
	}

	@Nested
	@DisplayName("주소 수정 (updateAddress)")
	class UpdateAddress {

		@Test
		@DisplayName("성공 - 주소 정보 업데이트")
		void success_UpdatesAddress() {
			// given
			UUID addressId = UUID.randomUUID();
			UpdateCustomerAddressRequest request = new UpdateCustomerAddressRequest("새로운 별칭", "새로운 주소", "102호", true);
			UserAddress existingAddress = spy(UserAddress.builder().addressId(addressId).user(testUser).build());
			when(userAddressRepository.findById(addressId)).thenReturn(Optional.of(existingAddress));

			// when
			UpdateCustomerAddressResponse response = customerAddressService.updateAddress(addressId, request, authentication);

			// then
			verify(existingAddress).update(request);
			assertThat(response.getAddress()).isEqualTo("새로운 주소");
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 주소")
		void failure_AddressNotFound_ThrowsException() {
			// given
			UUID addressId = UUID.randomUUID();
			UpdateCustomerAddressRequest request = new UpdateCustomerAddressRequest("별칭", "주소", "상세", false);
			when(userAddressRepository.findById(addressId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> customerAddressService.updateAddress(addressId, request, authentication))
				.isInstanceOf(GeneralException.class)
				.extracting("code")
				.isEqualTo(UserErrorStatus.ADDRESS_NOT_FOUND);
		}

		@Test
		@DisplayName("실패 - 다른 사용자의 주소에 접근")
		void failure_AccessDenied_ThrowsException() {
			// given
			UUID addressId = UUID.randomUUID();
			User anotherUser = User.builder().userId(2L).build();
			UserAddress anotherUsersAddress = UserAddress.builder().addressId(addressId).user(anotherUser).build();
			UpdateCustomerAddressRequest request = new UpdateCustomerAddressRequest("별칭", "주소", "상세", false);
			when(userAddressRepository.findById(addressId)).thenReturn(Optional.of(anotherUsersAddress));

			// when & then
			assertThatThrownBy(() -> customerAddressService.updateAddress(addressId, request, authentication))
				.isInstanceOf(GeneralException.class)
				.extracting("code")
				.isEqualTo(UserErrorStatus.ADDRESS_ACCESS_DENIED);
		}
	}

	@Nested
	@DisplayName("주소 삭제 (deleteAddress)")
	class DeleteAddress {

		@Test
		@DisplayName("성공 - 주소 삭제")
		void success_DeletesAddress() {
			// given
			UUID addressId = UUID.randomUUID();
			UserAddress addressToDelete = UserAddress.builder().addressId(addressId).user(testUser).isDefault(false).build();
			when(userAddressRepository.findById(addressId)).thenReturn(Optional.of(addressToDelete));

			// when
			String result = customerAddressService.deleteAddress(addressId, authentication);

			// then
			verify(userAddressRepository).delete(addressToDelete);
			assertThat(result).isEqualTo("Address deleted successfully");
		}

		@Test
		@DisplayName("실패 - 기본 주소는 삭제 불가")
		void failure_CannotDeleteDefaultAddress_ThrowsException() {
			// given
			UUID addressId = UUID.randomUUID();
			UserAddress defaultAddress = UserAddress.builder().addressId(addressId).user(testUser).isDefault(true).build();
			when(userAddressRepository.findById(addressId)).thenReturn(Optional.of(defaultAddress));

			// when & then
			assertThatThrownBy(() -> customerAddressService.deleteAddress(addressId, authentication))
				.isInstanceOf(GeneralException.class)
				.extracting("code")
				.isEqualTo(UserErrorStatus.CANNOT_DELETE_DEFAULT_ADDRESS);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 주소")
		void failure_AddressNotFound_ThrowsException() {
			// given
			UUID addressId = UUID.randomUUID();
			when(userAddressRepository.findById(addressId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> customerAddressService.deleteAddress(addressId, authentication))
				.isInstanceOf(GeneralException.class)
				.extracting("code")
				.isEqualTo(UserErrorStatus.ADDRESS_NOT_FOUND);
		}
	}
}