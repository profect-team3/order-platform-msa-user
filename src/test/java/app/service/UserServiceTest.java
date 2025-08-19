package app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import app.commonSecurity.TokenPrincipalParser;
import app.domain.user.UserService;
import app.domain.user.client.InternalAuthClient;
import app.domain.user.model.UserRepository;
import app.domain.user.model.dto.request.CreateUserRequest;
import app.domain.user.model.dto.response.CreateUserResponse;
import app.domain.user.model.dto.response.GetUserInfoResponse;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.enums.UserRole;
import app.domain.user.status.UserErrorStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.code.status.SuccessStatus;
import app.global.apiPayload.exception.GeneralException;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Test")
public class UserServiceTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private InternalAuthClient internalAuthClient;
	@Mock
	private TokenPrincipalParser tokenPrincipalParser;
	@Mock
	private Authentication authentication;

	@InjectMocks
	private UserService userService;

	private CreateUserRequest createValidUserReq(UserRole role) {
		CreateUserRequest req = new CreateUserRequest();
		req.setUsername("testuser");
		req.setPassword("password123!");
		req.setEmail("test@example.com");
		req.setNickname("testnick");
		req.setRealName("김테스트");
		req.setPhoneNumber("01012345678");
		req.setUserRole(role);
		return req;
	}

	private void givenNoDuplicatesFound(CreateUserRequest req) {
		given(userRepository.findFirstByUniqueFields(
			req.getUsername(),
			req.getEmail(),
			req.getNickname(),
			req.getPhoneNumber()
		)).willReturn(Optional.empty());
	}

	@Nested
	@DisplayName("성공 케이스")
	class SuccessCase {

		@Test
		@DisplayName("고객 계정 생성 성공")
		void createUser_ValidInput_Success() {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);
			User user = User.builder()
				.userId(1L)
				.username(req.getUsername())
				.password("encodedPassword")
				.userRole(req.getUserRole())
				.build();

			givenNoDuplicatesFound(req);
			given(passwordEncoder.encode(req.getPassword())).willReturn("encodedPassword");
			given(userRepository.save(any(User.class))).willReturn(user);

			// when
			CreateUserResponse res = userService.createUser(req);

			// then
			ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(userArgumentCaptor.capture());
			User capturedUser = userArgumentCaptor.getValue();

			assertThat(capturedUser.getUsername()).isEqualTo(req.getUsername());
			assertThat(capturedUser.getPassword()).isEqualTo("encodedPassword");
			assertThat(capturedUser.getEmail()).isEqualTo(req.getEmail());
			assertThat(capturedUser.getNickname()).isEqualTo(req.getNickname());
			assertThat(capturedUser.getUserRole()).isEqualTo(UserRole.CUSTOMER);
			assertThat(res.getUserId()).isEqualTo(1L);
		}

		@Test
		@DisplayName("점주 계정 생성 성공")
		void createOwner_Success() {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.OWNER);
			User user = User.builder()
				.userId(2L)
				.username(req.getUsername())
				.password("encodedPassword")
				.userRole(req.getUserRole())
				.build();

			givenNoDuplicatesFound(req);
			given(passwordEncoder.encode(req.getPassword())).willReturn("encodedPassword");
			given(userRepository.save(any(User.class))).willReturn(user);

			// when
			CreateUserResponse res = userService.createUser(req);

			// then
			ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(userArgumentCaptor.capture());
			User capturedUser = userArgumentCaptor.getValue();

			assertThat(capturedUser.getUserRole()).isEqualTo(UserRole.OWNER);
			assertThat(res.getUserId()).isEqualTo(2L);
		}
	}

	@Nested
	@DisplayName("중복 검증 실패")
	class ValidationFailure {

		@Test
		@DisplayName("중복된 아이디로 가입 시 예외 발생")
		void duplicateUsername_ThrowsException() {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);

			User existingUser = User.builder().username(req.getUsername()).build();
			given(userRepository.findFirstByUniqueFields(
				req.getUsername(), req.getEmail(), req.getNickname(), req.getPhoneNumber()
			)).willReturn(Optional.of(existingUser));

			// when & then
			assertThatThrownBy(() -> userService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(UserErrorStatus.USER_ALREADY_EXISTS.getCode());

			verify(userRepository, never()).save(any(User.class));
		}

		@Test
		@DisplayName("중복된 이메일로 가입 시 예외 발생")
		void duplicateEmail_ThrowsException() {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);

			User existingUser = User.builder()
				.username("anotherUser")
				.email(req.getEmail())
				.build();

			given(userRepository.findFirstByUniqueFields(
				req.getUsername(), req.getEmail(), req.getNickname(), req.getPhoneNumber()
			)).willReturn(Optional.of(existingUser));

			// when & then
			assertThatThrownBy(() -> userService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(UserErrorStatus.EMAIL_ALREADY_EXISTS.getCode());

			verify(userRepository, never()).save(any(User.class));
		}

		@Test
		@DisplayName("중복된 닉네임으로 가입 시 예외 발생")
		void duplicateNickname_ThrowsException() {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);

			User existingUser = User.builder()
				.username("anotherUser")
				.email("another@example.com")
				.nickname(req.getNickname())
				.build();

			given(userRepository.findFirstByUniqueFields(
				req.getUsername(), req.getEmail(), req.getNickname(), req.getPhoneNumber()
			)).willReturn(Optional.of(existingUser));

			// when & then
			assertThatThrownBy(() -> userService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(UserErrorStatus.NICKNAME_ALREADY_EXISTS.getCode());

			verify(userRepository, never()).save(any(User.class));
		}

		@Test
		@DisplayName("중복된 휴대폰 번호로 가입 시 예외 발생")
		void duplicatePhoneNumber_ThrowsException() {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);

			User existingUser = User.builder()
				.username("anotherUser")
				.email("another@example.com")
				.nickname("anotherNickname")
				.phoneNumber(req.getPhoneNumber())
				.build();

			given(userRepository.findFirstByUniqueFields(
				req.getUsername(), req.getEmail(), req.getNickname(), req.getPhoneNumber()
			)).willReturn(Optional.of(existingUser));

			// when & then
			assertThatThrownBy(() -> userService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(UserErrorStatus.PHONE_NUMBER_ALREADY_EXISTS.getCode());

			verify(userRepository, never()).save(any(User.class));
		}
	}

	@Nested
	@DisplayName("시스템 예외 상황")
	class SystemException {

		@Test
		@DisplayName("데이터베이스 저장 실패 시 예외 발생")
		void databaseError_OnSave_ThrowsException() {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);

			givenNoDuplicatesFound(req);
			given(userRepository.save(any(User.class))).willThrow(new DataAccessException("DB connection failed") {
			});

			// when & then
			assertThatThrownBy(() -> userService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(ErrorStatus._INTERNAL_SERVER_ERROR.getCode());
		}

		@Test
		@DisplayName("비밀번호 인코딩 실패 시 예외 발생")
		void passwordEncoding_Fail_ThrowsException() {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);

			givenNoDuplicatesFound(req);
			given(passwordEncoder.encode(req.getPassword())).willThrow(new RuntimeException("Encoding failed"));

			// when & then
			assertThatThrownBy(() -> userService.createUser(req))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Encoding failed");

			verify(userRepository, never()).save(any(User.class));
		}
	}

	@Nested
	@DisplayName("회원 정보 조회")
	class GetUserInfoTest {

		@Test
		@DisplayName("성공")
		void getUserInfo_Success() {
			// given
			Long userId = 1L;
			User user = User.builder()
				.userId(userId)
				.username("testuser")
				.email("test@example.com")
				.nickname("testnick")
				.realName("김테스트")
				.phoneNumber("01012345678")
				.userRole(UserRole.CUSTOMER)
				.build();

			given(tokenPrincipalParser.getUserId(authentication)).willReturn(String.valueOf(userId));
			given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));

			// when
			GetUserInfoResponse res = userService.getUserInfo(authentication);

			// then
			assertThat(res.getUserId()).isEqualTo(userId);
			assertThat(res.getUsername()).isEqualTo(user.getUsername());
			assertThat(res.getEmail()).isEqualTo(user.getEmail());
			assertThat(res.getNickname()).isEqualTo(user.getNickname());
		}

		@Test
		@DisplayName("실패 - 사용자를 찾을 수 없음")
		void getUserInfo_UserNotFound_ThrowsException() {
			// given
			Long userId = 1L;
			given(tokenPrincipalParser.getUserId(authentication)).willReturn(String.valueOf(userId));
			given(userRepository.findByUserId(userId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userService.getUserInfo(authentication))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(ErrorStatus.USER_NOT_FOUND.getCode());
		}
	}

	@Nested
	@DisplayName("회원 탈퇴")
	class WithdrawMembershipTest {

		@Test
		@DisplayName("성공")
		void withdrawMembership_Success() {
			// given
			Long userId = 1L;
			User user = mock(User.class); // Use mock to verify method call on it

			given(tokenPrincipalParser.getUserId(authentication)).willReturn(String.valueOf(userId));
			given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));
			given(internalAuthClient.logout()).willReturn(ApiResponse.onSuccess(SuccessStatus._OK,null));

			// when
			userService.withdrawMembership(authentication);

			// then
			verify(user).anonymizeForWithdrawal();
			verify(userRepository).delete(user);
			verify(internalAuthClient).logout();
		}

		@Test
		@DisplayName("실패 - 사용자를 찾을 수 없음")
		void withdrawMembership_UserNotFound_ThrowsException() {
			// given
			Long userId = 1L;
			given(tokenPrincipalParser.getUserId(authentication)).willReturn(String.valueOf(userId));
			given(userRepository.findByUserId(userId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userService.withdrawMembership(authentication))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(ErrorStatus.USER_NOT_FOUND.getCode());

			verify(userRepository, never()).delete(any(User.class));
			verify(internalAuthClient, never()).logout();
		}

		@Test
		@DisplayName("실패 - 로그아웃 실패(Auth서버 통신 오류)")
		void withdrawMembership_LogoutFailed_ThrowsException() {
			// given
			Long userId = 1L;
			User user = mock(User.class);

			given(tokenPrincipalParser.getUserId(authentication)).willReturn(String.valueOf(userId));
			given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));
			given(internalAuthClient.logout()).willThrow(
				new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Auth Server Error", new byte[0], null));

			// when & then
			assertThatThrownBy(() -> userService.withdrawMembership(authentication))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(UserErrorStatus.LOGOUT_FAILED.getCode());

			// anonymize and delete are called before logout
			verify(user).anonymizeForWithdrawal();
			verify(userRepository).delete(user);
		}
	}
}
