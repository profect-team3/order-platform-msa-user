package app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import app.domain.manager.ManagerService;
import app.domain.manager.dto.response.GetCustomerDetailResponse;
import app.domain.manager.dto.response.GetCustomerListResponse;
import app.domain.user.model.UserAddressRepository;
import app.domain.user.model.UserQueryRepository;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.UserAddress;
import app.domain.user.model.entity.enums.UserRole;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerService Test")
public class ManagerServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserQueryRepository userQueryRepository;

	@Mock
	private UserAddressRepository userAddressRepository;

	@InjectMocks
	private ManagerService managerService;

	@Nested
	@DisplayName("전체 고객 목록 조회 (getAllCustomer)")
	class GetAllCustomer {

		@Test
		@DisplayName("성공 - 여러 고객 조회")
		void getAllCustomer_shouldReturnMultipleCustomer() {
			// given
			User user1 = User.builder().userId(1L).email("user1@example.com").username("테스트1").build();
			User user2 = User.builder().userId(2L).email("user2@example.com").username("테스트2").build();
			Page<User> page = new PageImpl<>(List.of(user1, user2));
			Pageable pageable = PageRequest.of(0, 10);

			when(userRepository.findAllByUserRole(UserRole.CUSTOMER, pageable)).thenReturn(page);

			// when
			PagedResponse<GetCustomerListResponse> result = managerService.getAllCustomer(pageable);

			// then
			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getContent())
				.extracting(GetCustomerListResponse::getEmail)
				.containsExactly("user1@example.com", "user2@example.com");
		}

		@Test
		@DisplayName("성공 - 페이지네이션")
		void getAllCustomer_withPagination_shouldLimitResults() {
			// given
			List<User> users = IntStream.range(1, 16)
				.mapToObj(i -> User.builder()
					.userId((long)i)
					.username("test" + i)
					.email("test" + i + "@mail.com")
					.password("password")
					.build())
				.toList();
			Pageable pageable = PageRequest.of(0, 10);
			Page<User> page = new PageImpl<>(users.subList(0, 10), pageable, 15);

			when(userRepository.findAllByUserRole(eq(UserRole.CUSTOMER), eq(pageable))).thenReturn(page);

			// when
			PagedResponse<GetCustomerListResponse> result = managerService.getAllCustomer(pageable);

			// then
			assertThat(result.getContent()).hasSize(10);
			assertThat(result.getTotalElements()).isEqualTo(15);
		}

		@Test
		@DisplayName("성공 - 정렬")
		void getAllCustomer_withSorting_shouldReturnSorted() {
			// given
			User newUser = User.builder().userId(1L).email("test1@example.com").username("테스트1").build();
			User oldUser = User.builder().userId(2L).email("test2@example.com").username("테스트2").build();
			ReflectionTestUtils.setField(newUser, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(oldUser, "createdAt", LocalDateTime.now().minusDays(1));

			Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
			Page<User> page = new PageImpl<>(List.of(newUser, oldUser), pageable, 2);

			when(userRepository.findAllByUserRole(eq(UserRole.CUSTOMER), eq(pageable))).thenReturn(page);

			// when
			PagedResponse<GetCustomerListResponse> result = managerService.getAllCustomer(pageable);

			// then
			assertThat(result.getContent().get(0).getEmail()).isEqualTo("test1@example.com");
		}
	}

	@Nested
	@DisplayName("고객 상세 정보 조회 (getCustomerDetailById)")
	class GetCustomerDetailById {

		@Test
		@DisplayName("성공 - 고객 정보와 주소 목록 조회")
		void getCustomerDetail_shouldReturnCustomerAndAddressList() {
			// given
			User user = User.builder().userId(1L).realName("홍길동").email("test@example.com").build();
			List<UserAddress> addresses = List.of(
				UserAddress.builder().user(user).alias("집").address("서울시 마포구").addressDetail("101호").isDefault(true).build(),
				UserAddress.builder().user(user).alias("회사").address("서울시 강남구").addressDetail("202호").isDefault(false).build()
			);

			when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
			when(userAddressRepository.findAllByUserUserId(user.getUserId())).thenReturn(addresses);

			// when
			GetCustomerDetailResponse result = managerService.getCustomerDetailById(user.getUserId());

			// then
			assertThat(result.getName()).isEqualTo("홍길동");
			assertThat(result.getAddress()).hasSize(2);
		}

		@Test
		@DisplayName("성공 - 주소 목록이 없을 경우 빈 리스트 반환")
		void getCustomerDetail_noAddresses_shouldReturnEmptyList() {
			// given
			Long userId = 1L;
			User user = User.builder().userId(userId).realName("홍길동").email("hong@test.com").build();

			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(userAddressRepository.findAllByUserUserId(userId)).thenReturn(Collections.emptyList());

			// when
			GetCustomerDetailResponse result = managerService.getCustomerDetailById(userId);

			// then
			assertThat(result.getAddress()).isEmpty();
			assertThat(result.getName()).isEqualTo("홍길동");
			assertThat(result.getEmail()).isEqualTo("hong@test.com");
			verify(userRepository, times(1)).findById(userId);
			verify(userAddressRepository, times(1)).findAllByUserUserId(userId);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 사용자")
		void getUserDetail_userNotFound_shouldThrowException() {
			// given
			Long invalidUserId = 999L;
			when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

			// when
			GeneralException ex = catchThrowableOfType(
				() -> managerService.getCustomerDetailById(invalidUserId),
				GeneralException.class
			);

			// then
			assertThat(ex.getErrorReasonHttpStatus().getCode()).isEqualTo(ErrorStatus.USER_NOT_FOUND.getCode());
		}
	}

	@Nested
	@DisplayName("고객 검색 (searchCustomer)")
	class SearchCustomer {

		@Test
		@DisplayName("성공 - 키워드로 필터링된 고객 조회")
		void searchCustomer_withKeyword_shouldReturnFilteredUsers() {
			// given
			String keyword = "test";
			Pageable pageable = PageRequest.of(0, 10);
			User user1 = User.builder().userId(1L).username("testUser1").email("test1@mail.com").build();
			Page<User> page = new PageImpl<>(List.of(user1), pageable, 1);

			when(userQueryRepository.searchUser(eq(keyword), any(Pageable.class))).thenReturn(page);

			// when
			PagedResponse<GetCustomerListResponse> result = managerService.searchCustomer(keyword, pageable);

			// then
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0).getName()).isEqualTo("testUser1");
		}

		@Test
		@DisplayName("성공 - 검색 결과가 없으면 빈 페이지 반환")
		void searchCustomer_noResults_shouldReturnEmptyList() {
			// given
			String keyword = "unknown";
			Pageable pageable = PageRequest.of(0, 10);
			Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

			when(userQueryRepository.searchUser(eq(keyword), any(Pageable.class))).thenReturn(emptyPage);

			// when
			PagedResponse<GetCustomerListResponse> result = managerService.searchCustomer(keyword, pageable);

			// then
			assertThat(result.getContent()).isEmpty();
			assertThat(result.getTotalElements()).isZero();
		}
	}
}