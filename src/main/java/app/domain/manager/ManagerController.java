package app.domain.manager;

import static org.springframework.data.domain.Sort.Direction.*;


import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.commonUtil.apiPayload.ApiResponse;
import app.commonUtil.apiPayload.PagedResponse;
import app.domain.manager.dto.response.GetCustomerDetailResponse;
import app.domain.manager.dto.response.GetCustomerListResponse;
import app.domain.manager.status.ManagerSuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user/manager")
@RequiredArgsConstructor
@Tag(name = "관리자 API", description = "관리자의 사용자 관리 API")
public class ManagerController {

	private final ManagerService managerService;

	@GetMapping("/customer")
	@Operation(
		summary = "전체 사용자 목록 조회",
		description = "가입한 사용자 목록을 페이지 별로 조회합니다. 생성일 또는 수정일 기준으로 정렬할 수 있습니다.")
	public ApiResponse<PagedResponse<GetCustomerListResponse>> getAllCustomer(
		@PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable
	) {
		return ApiResponse.onSuccess(ManagerSuccessStatus.MANAGER_GET_CUSTOMER_OK,managerService.getAllCustomer(pageable));
	}
	@GetMapping("/customer/{userId}")
	@Operation(
		summary = "선택한 유저 정보 조회",
		description = "선택한 유저의 자세한 정보와 등록한 주소를 확인 합니다."
	)
	public ApiResponse<GetCustomerDetailResponse> getUsersDetailById(
		@PathVariable("userId") Long userId
	) {
		return ApiResponse.onSuccess(ManagerSuccessStatus.MANAGER_GET_CUSTOMER_DETAIL_OK,managerService.getCustomerDetailById(userId));
	}


	@GetMapping("/customer/search")
	@Operation(
		summary = "사용자 검색",
		description = "키워드를 사용하여 가입한 사용자를 검색하고, 결과를 페이지 별로 조회합니다. 생성일 또는 수정일 기준으로 정렬할 수 있습니다.")
	public ApiResponse<PagedResponse<GetCustomerListResponse>> searchCustomer(
		Pageable pageable,
		@RequestParam String keyWord
	) {
		return ApiResponse.onSuccess(ManagerSuccessStatus.MANAGER_SEARCH_CUSTOMER_OK,managerService.searchCustomer(keyWord, pageable));
	}

}