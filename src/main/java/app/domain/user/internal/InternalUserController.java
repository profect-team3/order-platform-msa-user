package app.domain.user.internal;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.commonUtil.apiPayload.ApiResponse;
import app.commonUtil.security.TokenPrincipalParser;
import app.domain.user.model.dto.response.GetUserInfoResponse;
import app.domain.user.status.UserSuccessStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/user")
public class InternalUserController {
	private final InternalUserService internalUserService;
	private final TokenPrincipalParser tokenPrincipalParser;
	@GetMapping("/exists")
	public ApiResponse<Boolean> isUserExists(Authentication authentication) {
		Boolean exists = internalUserService.isUserExists(Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(UserSuccessStatus.USER_EXISTS,exists);
	}

	@GetMapping("/name")
	public ApiResponse<String> getUserName(Authentication authentication) {
		String name=internalUserService.getUserName(Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(UserSuccessStatus.USER_NAME_FETCHED,name);
	}

	@GetMapping("/info")
	public ApiResponse<GetUserInfoResponse> getUserInfo(Authentication authentication){
		GetUserInfoResponse getUserInfoResponse= internalUserService.getUserInfo(Long.parseLong(tokenPrincipalParser.getUserId(authentication)));
		return ApiResponse.onSuccess(UserSuccessStatus.USER_INFO_FETCHED,getUserInfoResponse);
	}
}
