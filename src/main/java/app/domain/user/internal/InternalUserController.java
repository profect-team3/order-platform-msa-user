package app.domain.user.internal;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import app.domain.user.status.UserSuccessStatus;
import app.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InternalUserController {
	private final InternalUserService internalUserService;

	@GetMapping("internal/user/{userId}/exists")
	public ApiResponse<Boolean> isUserExists(@PathVariable Long userId) {
		Boolean exists = internalUserService.isUserExists(userId);
		return ApiResponse.onSuccess(UserSuccessStatus.USER_EXISTS,exists);
	}

	@GetMapping("internal/user/{userId}/name")
	public ApiResponse<String> getUserName(@PathVariable Long userId) {
		String name=internalUserService.getUserName(userId);
		return ApiResponse.onSuccess(UserSuccessStatus.USER_NAME_FETCHED,name);
	}
}
