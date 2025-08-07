package app.unit.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.TimeUnit;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import app.user.UserService;
import app.user.status.UserErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.jwt.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService.logout 테스트")
class UserServiceLogoutTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@BeforeEach
	void setUp() {
		mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
		given(SecurityContextHolder.getContext()).willReturn(securityContext);
	}

	@AfterEach
	void tearDown() {
		mockedSecurityContextHolder.close();
	}

	@Nested
	@DisplayName("성공 케이스")
	class SuccessCase {
		@Test
		@DisplayName("정상 로그아웃: RefreshToken을 삭제하고 유효한 AccessToken을 블랙리스트에 추가한다.")
		void logout_Success_DeletesRefreshTokenAndBlacklistsAccessToken() {
			// given
			String userId = "1";
			String accessToken = "valid-access-token";
			String refreshTokenKey = "RT:" + userId;
			long expiration = 1000L;

			given(securityContext.getAuthentication()).willReturn(authentication);
			given(authentication.getName()).willReturn(userId);
			given(authentication.getCredentials()).willReturn(accessToken);
			given(authentication.isAuthenticated()).willReturn(true);
			given(redisTemplate.hasKey(refreshTokenKey)).willReturn(true);
			given(jwtTokenProvider.getExpiration(accessToken)).willReturn(expiration);
			given(redisTemplate.opsForValue()).willReturn(valueOperations);

			// when
			userService.logout();

			// then
			then(redisTemplate).should().delete(refreshTokenKey);
			then(valueOperations).should().set(
				"BL:" + accessToken,
				"logout",
				expiration,
				TimeUnit.MILLISECONDS
			);
		}

		@Test
		@DisplayName("RefreshToken이 없어도 AccessToken을 블랙리스트에 추가하며 정상 처리된다.")
		void logout_Success_WhenRefreshTokenNotExists() {
			// given
			String userId = "1";
			String accessToken = "valid-access-token";
			String refreshTokenKey = "RT:" + userId;
			long expiration = 1000L;

			given(securityContext.getAuthentication()).willReturn(authentication);
			given(authentication.getName()).willReturn(userId);
			given(authentication.getCredentials()).willReturn(accessToken);
			given(authentication.isAuthenticated()).willReturn(true);
			given(redisTemplate.hasKey(refreshTokenKey)).willReturn(false);
			given(jwtTokenProvider.getExpiration(accessToken)).willReturn(expiration);
			given(redisTemplate.opsForValue()).willReturn(valueOperations);

			// when
			userService.logout();

			// then
			then(redisTemplate).should(never()).delete(refreshTokenKey);
			then(valueOperations).should().set(
				"BL:" + accessToken,
				"logout",
				expiration,
				TimeUnit.MILLISECONDS
			);
		}

		@Test
		@DisplayName("AccessToken이 이미 만료된 경우, RefreshToken만 삭제하고 블랙리스트에는 추가하지 않는다.")
		void logout_Success_WhenAccessTokenIsExpired() {
			// given
			String userId = "1";
			String accessToken = "expired-access-token";
			String refreshTokenKey = "RT:" + userId;
			long expiration = 0L;

			given(securityContext.getAuthentication()).willReturn(authentication);
			given(authentication.getName()).willReturn(userId);
			given(authentication.getCredentials()).willReturn(accessToken);
			given(authentication.isAuthenticated()).willReturn(true);
			given(redisTemplate.hasKey(refreshTokenKey)).willReturn(true);
			given(jwtTokenProvider.getExpiration(accessToken)).willReturn(expiration);

			// when
			userService.logout();

			// then
			then(redisTemplate).should().delete(refreshTokenKey);
			then(redisTemplate).should(never()).opsForValue();
		}
	}

	@Nested
	@DisplayName("실패 케이스")
	class FailureCase {

		@Test
		@DisplayName("인증 객체가 없는 경우(비로그인 상태), AUTHENTICATION_NOT_FOUND 예외를 던진다.")
		void logout_Fail_WhenAuthenticationIsNull() {
			// given
			given(securityContext.getAuthentication()).willReturn(null);

			// when & then
			assertThatThrownBy(() -> userService.logout())
				.isInstanceOf(GeneralException.class)
				.extracting("code")
				.isEqualTo(UserErrorStatus.AUTHENTICATION_NOT_FOUND);
		}
	}
}