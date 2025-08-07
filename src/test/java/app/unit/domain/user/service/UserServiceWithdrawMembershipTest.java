package app.unit.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import app.user.UserService;
import app.user.model.UserRepository;
import app.user.model.entity.User;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService.withdrawMembership 테스트")
class UserServiceWithdrawMembershipTest {

	@Spy
	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@BeforeEach
	void setUp() {
		mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
		given(SecurityContextHolder.getContext()).willReturn(securityContext);
		given(securityContext.getAuthentication()).willReturn(authentication);
	}

	@AfterEach
	void tearDown() {
		mockedSecurityContextHolder.close();
	}

	@Nested
	@DisplayName("성공 케이스")
	class SuccessCase {

		@Test
		@DisplayName("정상적으로 회원 탈퇴를 처리하고 로그아웃을 호출한다.")
		void withdrawMembership_Success() {
			// given
			Long userId = 1L;
			User userToWithdraw = spy(User.builder().userId(userId).build());

			given(authentication.getName()).willReturn(String.valueOf(userId));
			given(userRepository.findById(userId)).willReturn(Optional.of(userToWithdraw));
			willDoNothing().given(userRepository).delete(any(User.class));
			willDoNothing().given(userService).logout();

			// when
			userService.withdrawMembership();

			// then
			then(userRepository).should().findById(userId);
			then(userToWithdraw).should().anonymizeForWithdrawal();
			then(userRepository).should().delete(userToWithdraw);
			then(userService).should().logout();
		}
	}

	@Nested
	@DisplayName("실패 케이스")
	class FailureCase {

		@Test
		@DisplayName("존재하지 않는 사용자로 탈퇴 시도 시 USER_NOT_FOUND 예외가 발생한다.")
		void withdrawMembership_UserNotFound_ThrowsException() {
			// given
			Long userId = 99L;
			given(authentication.getName()).willReturn(String.valueOf(userId));
			given(userRepository.findById(userId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userService.withdrawMembership())
				.isInstanceOf(GeneralException.class)
				.extracting("code")
				.isEqualTo(ErrorStatus.USER_NOT_FOUND);

			then(userRepository).should(never()).delete(any(User.class));
			then(userService).should(never()).logout();
		}

		@Test
		@DisplayName("DB에서 사용자 삭제 실패 시, 예외가 발생하고 로그아웃은 호출되지 않는다.")
		void withdrawMembership_DeleteFails_ThrowsExceptionAndDoesNotLogout() {
			// given
			Long userId = 1L;
			User userToWithdraw = spy(User.builder().userId(userId).build());

			given(authentication.getName()).willReturn(String.valueOf(userId));
			given(userRepository.findById(userId)).willReturn(Optional.of(userToWithdraw));
			willThrow(new DataAccessException("DB delete failed") {
			}).given(userRepository).delete(userToWithdraw);

			// when & then
			assertThatThrownBy(() -> userService.withdrawMembership())
				.isInstanceOf(DataAccessException.class);

			then(userToWithdraw).should().anonymizeForWithdrawal();
			then(userRepository).should().delete(userToWithdraw);
			then(userService).should(never()).logout();
		}
	}
}