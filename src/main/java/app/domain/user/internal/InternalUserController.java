package app.domain.user.internal;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.commonUtil.apiPayload.ApiResponse;
import app.domain.user.model.dto.response.GetUserInfoResponse;
import app.domain.user.status.UserSuccessStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("internal/user")
public class InternalUserController {
	private final InternalUserService internalUserService;

	@GetMapping("/{userId}/exists")
	public ApiResponse<Boolean> isUserExists(@PathVariable Long userId) {
		Boolean exists = internalUserService.isUserExists(userId);
		return ApiResponse.onSuccess(UserSuccessStatus.USER_EXISTS,exists);
	}

	@GetMapping("/{userId}/name")
	public ApiResponse<String> getUserName(@PathVariable Long userId) {
		String name=internalUserService.getUserName(userId);
		return ApiResponse.onSuccess(UserSuccessStatus.USER_NAME_FETCHED,name);
	}

	@GetMapping("/{userId}/info")
	public ApiResponse<GetUserInfoResponse> getUserInfo(@PathVariable Long userId){
		GetUserInfoResponse getUserInfoResponse= internalUserService.getUserInfo(userId);
		return ApiResponse.onSuccess(UserSuccessStatus.USER_INFO_FETCHED,getUserInfoResponse);
	}
}
