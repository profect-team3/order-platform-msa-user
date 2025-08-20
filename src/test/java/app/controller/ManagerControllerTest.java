package app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.manager.ManagerController;
import app.domain.manager.ManagerService;
import app.domain.manager.dto.response.GetCustomerDetailResponse;
import app.domain.manager.dto.response.GetCustomerListResponse;
import app.domain.manager.status.ManagerSuccessStatus;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

@WebMvcTest(ManagerController.class)
@DisplayName("ManagerController 테스트")
@WithMockUser(roles = "MANAGER")
class ManagerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ManagerService managerService;

	@Nested
	@DisplayName("전체 사용자 목록 조회 API [/manager/customer] 테스트")
	class GetAllCustomerTest {

		@Test
		@DisplayName("성공: 전체 사용자 목록을 페이지와 함께 정상적으로 조회한다.")
		void getAllCustomer_Success() throws Exception {
			// given
			GetCustomerListResponse customerResponse = GetCustomerListResponse.builder()
				.id(1L)
				.name("김테스트")
				.email("test@naver.com")
				.createdAt(LocalDateTime.now())
				.build();
			Page<GetCustomerListResponse> page = new PageImpl<>(List.of(customerResponse));
			PagedResponse<GetCustomerListResponse> mockResponse = PagedResponse.from(page);

			given(managerService.getAllCustomer(any(Pageable.class))).willReturn(mockResponse);

			// when
			ResultActions resultActions = mockMvc.perform(get("/manager/customer")
				.param("page", "0")
				.param("size", "10")
				.with(csrf()));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(ManagerSuccessStatus.MANAGER_GET_CUSTOMER_OK.getCode()))
				.andExpect(jsonPath("$.result.content[0].id").value(1L))
				.andExpect(jsonPath("$.result.content[0].name").value("김테스트"))
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("선택한 유저 정보 조회 API [/manager/customer/{userId}] 테스트")
	class GetUsersDetailByIdTest {

		@Test
		@DisplayName("성공: 특정 사용자 ID로 상세 정보를 정상적으로 조회한다.")
		void getUsersDetailById_Success() throws Exception {
			// given
			long userId = 1L;
			GetCustomerDetailResponse mockResponse = GetCustomerDetailResponse.builder()
				.userId(userId)
				.name("김테스트")
				.nickName("testnick")
				.address(Collections.emptyList())
				.build();
			given(managerService.getCustomerDetailById(anyLong())).willReturn(mockResponse);

			// when
			ResultActions resultActions = mockMvc.perform(get("/manager/customer/{userId}", userId)
				.with(csrf()));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(ManagerSuccessStatus.MANAGER_GET_CUSTOMER_DETAIL_OK.getCode()))
				.andExpect(jsonPath("$.result.userId").value(userId))
				.andExpect(jsonPath("$.result.name").value("김테스트"))
				.andDo(print());
		}

		@Test
		@DisplayName("실패: 존재하지 않는 사용자 ID로 조회 시 404 Not Found를 반환한다.")
		void getUsersDetailById_Fail_NotFound() throws Exception {
			// given
			long nonExistentUserId = 999L;
			given(managerService.getCustomerDetailById(anyLong()))
				.willThrow(new GeneralException(ErrorStatus.USER_NOT_FOUND));

			// when
			ResultActions resultActions = mockMvc.perform(get("/manager/customer/{userId}", nonExistentUserId)
				.with(csrf()));

			// then
			resultActions
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value(ErrorStatus.USER_NOT_FOUND.getCode()))
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("사용자 검색 API [/manager/customer/search] 테스트")
	class SearchCustomerTest {

		@Test
		@DisplayName("성공: 키워드로 사용자를 검색하고 결과를 페이지와 함께 정상적으로 조회한다.")
		void searchCustomer_Success() throws Exception {
			// given
			String keyWord = "테스트";
			GetCustomerListResponse customerResponse = GetCustomerListResponse.builder()
				.id(1L)
				.name("김테스트")
				.email("test@naver.com")
				.createdAt(LocalDateTime.now())
				.build();
			Page<GetCustomerListResponse> page = new PageImpl<>(List.of(customerResponse));
			PagedResponse<GetCustomerListResponse> mockResponse = PagedResponse.from(page);

			given(managerService.searchCustomer(anyString(), any(Pageable.class))).willReturn(mockResponse);

			// when
			ResultActions resultActions = mockMvc.perform(get("/manager/customer/search")
				.param("keyWord", keyWord)
				.with(csrf()));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(ManagerSuccessStatus.MANAGER_SEARCH_CUSTOMER_OK.getCode()))
				.andExpect(jsonPath("$.result.content[0].id").value(1L))
				.andExpect(jsonPath("$.result.content[0].name").value("김테스트"))
				.andDo(print());
		}
	}
}