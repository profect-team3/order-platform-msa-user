package app.unit.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import app.user.UserService;
import app.user.model.dto.response.GetUserInfoResponse;
import app.user.model.entity.User;
import app.user.model.entity.enums.UserRole;
import app.global.SecurityUtil;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService.getUserInfo 테스트")
class UserServiceGetUserInfoTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private SecurityUtil securityUtil;

	@Nested
	@DisplayName("성공 케이스")
	class SuccessCase {
		@Test
		@DisplayName("현재 로그인된 사용자의 정보를 성공적으로 조회한다.")
		void getUserInfo_Success() {
			// given
			User mockUser = User.builder()
				.userId(1L)
				.username("testuser")
				.email("test@example.com")
				.nickname("testnick")
				.realName("김테스트")
				.phoneNumber("01012345678")
				.userRole(UserRole.CUSTOMER)
				.build();

			given(securityUtil.getCurrentUser()).willReturn(mockUser);

			// when
			GetUserInfoResponse response = userService.getUserInfo();

			// then
			assertThat(response).isNotNull();
			assertThat(response.getUserId()).isEqualTo(mockUser.getUserId());
			assertThat(response.getUsername()).isEqualTo(mockUser.getUsername());
			assertThat(response.getEmail()).isEqualTo(mockUser.getEmail());
			assertThat(response.getNickname()).isEqualTo(mockUser.getNickname());
			assertThat(response.getRealName()).isEqualTo(mockUser.getRealName());
			assertThat(response.getPhoneNumber()).isEqualTo(mockUser.getPhoneNumber());
			assertThat(response.getUserRole()).isEqualTo(mockUser.getUserRole());

			then(securityUtil).should().getCurrentUser();
		}
	}

	@Nested
	@DisplayName("실패 케이스")
	class FailureCase {
		@Test
		@DisplayName("인증 정보가 없어 사용자를 찾을 수 없을 때, GeneralException(_UNAUTHORIZED)을 던진다.")
		void getUserInfo_Fail_Unauthorized() {
			// given
			given(securityUtil.getCurrentUser()).willThrow(new GeneralException(ErrorStatus._UNAUTHORIZED));

			// when & then
			assertThatThrownBy(() -> userService.getUserInfo())
				.isInstanceOf(GeneralException.class)
				.extracting("code")
				.isEqualTo(ErrorStatus._UNAUTHORIZED);
		}

		@Test
		@DisplayName("토큰은 유효하지만 DB에 해당 사용자가 없을 때, GeneralException(USER_NOT_FOUND)을 던진다.")
		void getUserInfo_Fail_UserNotFoundInDb() {
			// given
			given(securityUtil.getCurrentUser()).willThrow(new GeneralException(ErrorStatus.USER_NOT_FOUND));

			// when & then
			assertThatThrownBy(() -> userService.getUserInfo())
				.isInstanceOf(GeneralException.class)
				.extracting("code")
				.isEqualTo(ErrorStatus.USER_NOT_FOUND);
		}

		@Test
		@DisplayName("DB 조회 중 예외 발생 시, 해당 예외가 그대로 전파된다.")
		void getUserInfo_Fail_DatabaseError() {
			// given
			given(securityUtil.getCurrentUser()).willThrow(new DataAccessException("DB connection failed") {
			});

			// when & then
			assertThatThrownBy(() -> userService.getUserInfo())
				.isInstanceOf(DataAccessException.class)
				.hasMessage("DB connection failed");
		}
	}
}