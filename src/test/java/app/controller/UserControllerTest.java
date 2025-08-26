package app.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.user.UserController;
import app.domain.user.UserService;
import app.domain.user.model.dto.request.CreateUserRequest;
import app.domain.user.model.dto.response.CreateUserResponse;
import app.domain.user.model.dto.response.GetUserInfoResponse;
import app.domain.user.model.entity.enums.UserRole;
import app.domain.user.status.UserErrorStatus;
import app.domain.user.status.UserSuccessStatus;
import app.global.apiPayload.code.status.ErrorStatus;

import app.global.apiPayload.exception.GeneralException;

@WebMvcTest(UserController.class)
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

	@Nested
	@DisplayName("회원가입 API [/user/signup] 테스트")
	class CreateUserTest {

		@Test
		@DisplayName("성공: 유효한 정보로 회원가입을 요청하면 201 Created와 생성된 사용자 정보를 반환한다.")
		@WithMockUser(roles ="CUSTOMER")
		void createUser_Success() throws Exception {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);
			CreateUserResponse mockResponse = CreateUserResponse.builder()
				.userId(1L)
				.build();
			given(userService.createUser(any(CreateUserRequest.class))).willReturn(mockResponse);

			mockMvc.perform(post("/user/signup")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result.userId").value(mockResponse.getUserId()));
		}

		@Test
		@DisplayName("실패(유효성 검증): 아이디가 누락된 요청은 400 Bad Request를 반환한다.")
		@WithMockUser(roles ="CUSTOMER")
		void createUser_Fail_Validation() throws Exception {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);
			req.setUsername(" ");

			// when
			ResultActions resultActions = mockMvc.perform(post("/user/signup")
				.with(csrf())// 올바른 API 경로
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)));

			// then
			resultActions
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(
					jsonPath("$.code").value(ErrorStatus._BAD_REQUEST.getCode()))
				.andExpect(jsonPath("$.message").value(
					ErrorStatus._BAD_REQUEST.getMessage()))
				.andExpect(jsonPath("$.result.username").exists()) // 유효성 검증 실패 필드 확인
				.andDo(print());
		}

		@Test
		@DisplayName("실패(비즈니스 로직): 이미 존재하는 아이디로 회원가입을 요청하면 409 Conflict를 반환한다.")
		@WithMockUser(roles ="CUSTOMER")
		void createUser_Fail_DuplicateUsername() throws Exception {
			// given
			CreateUserRequest req = createValidUserReq(UserRole.CUSTOMER);
			// 💡 서비스가 GeneralException을 던지도록 설정합니다.
			given(userService.createUser(any(CreateUserRequest.class)))
				.willThrow(new GeneralException(UserErrorStatus.USER_ALREADY_EXISTS));

			// when
			ResultActions resultActions = mockMvc.perform(post("/user/signup") // 올바른 API 경로
				.with(csrf())
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
	@DisplayName("회원 탈퇴 API [/user/withdraw] 테스트")
	@WithMockUser(roles ="CUSTOMER")
	class WithdrawTest {

		@Test
		@DisplayName("성공: 회원 탈퇴 요청 시 200 OK를 반환한다.")
		void withdraw_Success() throws Exception {
			// given
			willDoNothing().given(userService).withdrawMembership(any());

			// when
			ResultActions resultActions = mockMvc.perform(delete("/user/withdraw").with(csrf()));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(UserSuccessStatus.WITHDRAW_SUCCESS.getCode()))
				.andDo(print());

			// 서비스 메서드가 호출되었는지 검증
			then(userService).should().withdrawMembership(any());
		}
	}

	@Nested
	@DisplayName("회원 정보 조회 API [/user/info] 테스트")
	@WithMockUser(roles ="CUSTOMER")
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
			given(userService.getUserInfo(any())).willReturn(mockResponse);

			// when
			ResultActions resultActions = mockMvc.perform(get("/user/info").with(csrf()));

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
		@WithMockUser(roles ="CUSTOMER")
		void getUserInfo_Fail_Unauthorized() throws Exception {
			// given
			given(userService.getUserInfo(any()))
				.willThrow(new GeneralException(ErrorStatus._UNAUTHORIZED));

			// when
			ResultActions resultActions = mockMvc.perform(get("/user/info").with(csrf()));

			// then
			resultActions
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value(ErrorStatus._UNAUTHORIZED.getCode()))
				.andDo(print());
		}
	}

}