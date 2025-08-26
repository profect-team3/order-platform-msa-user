package app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.customer.CustomerAddressController;
import app.domain.customer.CustomerAddressService;
import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.request.UpdateCustomerAddressRequest;
import app.domain.customer.dto.response.AddCustomerAddressResponse;
import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.domain.customer.dto.response.UpdateCustomerAddressResponse;
import app.domain.customer.status.CustomerSuccessStatus;
import app.global.apiPayload.code.status.ErrorStatus;

@WebMvcTest(CustomerAddressController.class)
@DisplayName("CustomerAddressController 테스트")
@WithMockUser(roles = "CUSTOMER")
class CustomerAddressControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CustomerAddressService customerAddressService;

	@Nested
	@DisplayName("사용자 주소지 목록 조회 API [/address/list] 테스트")
	class GetCustomerAddressesTest {

		@Test
		@DisplayName("성공: 인증된 사용자의 주소 목록을 정상적으로 조회한다.")
		void getCustomerAddresses_Success() throws Exception {
			// given
			GetCustomerAddressListResponse addressResponse = GetCustomerAddressListResponse.builder()
				.address("경기도")
				.alias("집")
				.build();
			List<GetCustomerAddressListResponse> mockResponse = List.of(addressResponse);
			given(customerAddressService.getCustomerAddresses(any(Authentication.class))).willReturn(mockResponse);

			// when
			mockMvc.perform(get("/user/address/list").with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(CustomerSuccessStatus.ADDRESS_LIST_FOUND.getCode()))
				.andExpect(jsonPath("$.result[0].alias").value("집"))
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("사용자 주소지 등록 API [/address/add] 테스트")
	class AddCustomerAddressTest {

		@Test
		@DisplayName("성공: 유효한 정보로 주소를 등록한다.")
		void addCustomerAddress_Success() throws Exception {
			// given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest("집", "서울시 강남구", "101호",true);
			AddCustomerAddressResponse mockResponse = AddCustomerAddressResponse.builder()
				.address_id(UUID.randomUUID())
				.build();
			given(customerAddressService.addCustomerAddress(any(AddCustomerAddressRequest.class), any(Authentication.class)))
				.willReturn(mockResponse);

			// when
				 mockMvc.perform(post("/user/address/add")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(CustomerSuccessStatus.ADDRESS_ADDED.getCode()))
				.andExpect(jsonPath("$.result.address_id").exists())
				.andDo(print());
		}

		@Test
		@DisplayName("실패: 주소 별칭이 비어있으면 400 Bad Request를 반환한다.")
		void addCustomerAddress_Fail_BlankAlias() throws Exception {
			// given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(null, "서울시 강남구", "101호",true);

			// when
			ResultActions resultActions = mockMvc.perform(post("/user/address/add")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

			// then
			resultActions
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value(ErrorStatus._BAD_REQUEST.getCode()))
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("사용자 주소지 수정 API [/address/update/{addressId}] 테스트")
	class UpdateCustomerAddressTest {

		@Test
		@DisplayName("성공: 유효한 정보로 주소를 수정한다.")
		void updateCustomerAddress_Success() throws Exception {
			// given
			UUID addressId = UUID.randomUUID();
			UpdateCustomerAddressRequest request = new UpdateCustomerAddressRequest("회사", "경기도", "고양시",true);
			UpdateCustomerAddressResponse mockResponse = UpdateCustomerAddressResponse.builder()
				.alias("회사")
				.address("경기도")
				.addressDetail("고양시")
				.isDefault(true)
				.build();
			given(customerAddressService.updateAddress(any(UUID.class), any(UpdateCustomerAddressRequest.class), any(Authentication.class)))
				.willReturn(mockResponse);

			// when
			mockMvc.perform(put("/user/address/update/{addressId}", addressId)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(CustomerSuccessStatus.ADDRESS_UPDATE.getCode()))
				.andExpect(jsonPath("$.result.address").value("경기도"))
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("사용자 주소지 삭제 API [/address/{addressId}] 테스트")
	class DeleteCustomerAddressTest {

		@Test
		@DisplayName("성공: 특정 주소를 정상적으로 삭제한다.")
		void deleteCustomerAddress_Success() throws Exception {
			// given
			UUID addressId = UUID.randomUUID();
			String successMessage = "주소가 삭제되었습니다.";
			given(customerAddressService.deleteAddress(any(UUID.class), any(Authentication.class)))
				.willReturn(successMessage);

			// when
			ResultActions resultActions = mockMvc.perform(get("/user/address/{addressId}", addressId)
				.with(csrf()));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(CustomerSuccessStatus.ADDRESS_DELETE.getCode()))
				.andExpect(jsonPath("$.result").value(successMessage))
				.andDo(print());
		}
	}
}