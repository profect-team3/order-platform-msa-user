package app.unit.domain.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.user.UserController;
import app.user.UserService;
import app.user.model.dto.request.CreateUserRequest;
import app.user.model.dto.request.LoginRequest;
import app.user.model.dto.response.CreateUserResponse;
import app.user.model.dto.response.GetUserInfoResponse;
import app.user.model.dto.response.LoginResponse;
import app.user.model.entity.enums.UserRole;
import app.user.status.UserErrorStatus;
import app.user.status.UserSuccessStatus;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.config.MockSecurityConfig;

@WebMvcTest(controllers = UserController.class)
@Import(MockSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController 테스트")
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
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

	private LoginRequest createValidLoginReq() {
		LoginRequest req = new LoginRequest();
		req.setUsername("testuser");
		req.setPassword("password123!");
		return req;
	}

	@Nested
	@DisplayName("회원가입 API [/user/signup] 테스트")
	class CreateUserTest {

		@Test
		@DisplayName("성공: 유효한 정보로 회원가입을 요청하면 201 Created와 생성된 사용자 정보를 반환한다.")
		void createUser_Success() throws Exception {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);
			CreateUserResponse mockResponse = CreateUserResponse.builder()
				.userId(1L)
				.build();
			given(userService.createUser(any(CreateUserRequest.class))).willReturn(mockResponse);

			// when
			ResultActions resultActions = mockMvc.perform(post("/user/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result.userId").value(mockResponse.getUserId()))
				.andDo(print());
		}

		@Test
		@DisplayName("실패(유효성 검증): 아이디가 누락된 요청은 400 Bad Request를 반환한다.")
		void createUser_Fail_Validation() throws Exception {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);
			req.setUsername(" ");

			// when
			ResultActions resultActions = mockMvc.perform(post("/user/signup") // 올바른 API 경로
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)));

			// then
			resultActions
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(
					jsonPath("$.code").value(app.global.apiPayload.code.status.ErrorStatus._BAD_REQUEST.getCode()))
				.andExpect(jsonPath("$.message").value(
					app.global.apiPayload.code.status.ErrorStatus._BAD_REQUEST.getMessage()))
				.andExpect(jsonPath("$.result.username").exists()) // 유효성 검증 실패 필드 확인
				.andDo(print());
		}

		@Test
		@DisplayName("실패(비즈니스 로직): 이미 존재하는 아이디로 회원가입을 요청하면 409 Conflict를 반환한다.")
		void createUser_Fail_DuplicateUsername() throws Exception {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);
			// 💡 서비스가 GeneralException을 던지도록 설정합니다.
			given(userService.createUser(any(CreateUserRequest.class)))
				.willThrow(new GeneralException(UserErrorStatus.USER_ALREADY_EXISTS));

			// when
			ResultActions resultActions = mockMvc.perform(post("/user/signup") // 올바른 API 경로
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)));

			// then
			resultActions
				.andExpect(status().isConflict()) // HTTP 409 Conflict 검증
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(
					jsonPath("$.code").value(UserErrorStatus.USER_ALREADY_EXISTS.getCode())) // 'resultCode' -> 'code'
				.andExpect(jsonPath("$.message").value(UserErrorStatus.USER_ALREADY_EXISTS.getMessage()))
				.andExpect(jsonPath("$.result").doesNotExist()) // 실패 시 result는 없음
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("로그인 API [/user/login] 테스트")
	class LoginTest {

		@Test
		@DisplayName("성공: 올바른 아이디와 비밀번호로 로그인하면 200 OK와 토큰 정보를 반환한다.")
		void login_Success() throws Exception {
			// given
			LoginRequest req = createValidLoginReq();
			LoginResponse mockResponse = LoginResponse.builder()
				.accessToken("dummy-access-token")
				.refreshToken("dummy-refresh-token")
				.build();
			given(userService.login(any(LoginRequest.class))).willReturn(mockResponse);

			// when
			ResultActions resultActions = mockMvc.perform(post("/user/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(UserSuccessStatus.LOGIN_SUCCESS.getCode()))
				.andExpect(jsonPath("$.result.accessToken").value(mockResponse.getAccessToken()))
				.andDo(print());
		}

		@Test
		@DisplayName("실패(유효성 검증): 비밀번호가 누락된 요청은 400 Bad Request를 반환한다.")
		void login_Fail_Validation() throws Exception {
			// given
			LoginRequest req = createValidLoginReq();
			req.setPassword(null); // 유효하지 않은 값

			// when
			ResultActions resultActions = mockMvc.perform(post("/user/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)));

			// then
			resultActions
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value(ErrorStatus._BAD_REQUEST.getCode()))
				.andExpect(jsonPath("$.result.password").exists())
				.andDo(print());
		}

		@Test
		@DisplayName("실패(비즈니스 로직): 존재하지 않는 사용자로 로그인을 요청하면 404 Not Found를 반환한다.")
		void login_Fail_UserNotFound() throws Exception {
			// given
			LoginRequest req = createValidLoginReq();
			given(userService.login(any(LoginRequest.class)))
				.willThrow(new GeneralException(ErrorStatus.USER_NOT_FOUND));

			// when
			ResultActions resultActions = mockMvc.perform(post("/user/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)));

			// then
			resultActions
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value(ErrorStatus.USER_NOT_FOUND.getCode()))
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("로그아웃 API [/user/logout] 테스트")
	class LogoutTest {

		@Test
		@DisplayName("성공: 로그아웃 요청 시 200 OK를 반환한다.")
		void logout_Success() throws Exception {
			// given
			// userService.logout()은 반환값이 없으므로, 예외가 발생하지 않도록 설정
			willDoNothing().given(userService).logout();

			// when
			ResultActions resultActions = mockMvc.perform(post("/user/logout"));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(UserSuccessStatus.LOGOUT_SUCCESS.getCode()))
				.andDo(print());

			// 서비스 메서드가 호출되었는지 검증
			then(userService).should().logout();
		}
	}

	@Nested
	@DisplayName("회원 탈퇴 API [/user/withdraw] 테스트")
	class WithdrawTest {

		@Test
		@DisplayName("성공: 회원 탈퇴 요청 시 200 OK를 반환한다.")
		void withdraw_Success() throws Exception {
			// given
			willDoNothing().given(userService).withdrawMembership();

			// when
			ResultActions resultActions = mockMvc.perform(delete("/user/withdraw"));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(UserSuccessStatus.WITHDRAW_SUCCESS.getCode()))
				.andDo(print());

			// 서비스 메서드가 호출되었는지 검증
			then(userService).should().withdrawMembership();
		}
	}

	@Nested
	@DisplayName("회원 정보 조회 API [/user/info] 테스트")
	class GetUserInfoTest {

		@Test
		@DisplayName("성공: 회원 정보 조회 요청 시 200 OK와 사용자 정보를 반환한다.")
		void getUserInfo_Success() throws Exception {
			// given
			GetUserInfoResponse mockResponse = GetUserInfoResponse.builder()
				.userId(1L)
				.username("testuser")
				.email("test@example.com")
				.build();
			given(userService.getUserInfo()).willReturn(mockResponse);

			// when
			ResultActions resultActions = mockMvc.perform(get("/user/info"));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(UserSuccessStatus.USER_PROFILE_FETCHED.getCode()))
				.andExpect(jsonPath("$.result.userId").value(mockResponse.getUserId()))
				.andExpect(jsonPath("$.result.username").value(mockResponse.getUsername()))
				.andDo(print());
		}

		@Test
		@DisplayName("실패(비즈니스 로직): 인증 정보가 없는 상태로 요청 시 401 Unauthorized를 반환한다.")
		void getUserInfo_Fail_Unauthorized() throws Exception {
			// given
			given(userService.getUserInfo())
				.willThrow(new GeneralException(ErrorStatus._UNAUTHORIZED));

			// when
			ResultActions resultActions = mockMvc.perform(get("/user/info"));

			// then
			resultActions
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value(ErrorStatus._UNAUTHORIZED.getCode()))
				.andDo(print());
		}
	}

}