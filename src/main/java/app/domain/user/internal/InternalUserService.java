package app.domain.user.internal;

import java.util.UUID;

import org.springframework.stereotype.Service;

import app.commonUtil.apiPayload.code.status.ErrorStatus;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.domain.user.model.UserRepository;
import app.domain.user.model.dto.response.GetUserInfoResponse;
import app.domain.user.model.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternalUserService {

	private final UserRepository userRepository;

	public Boolean isUserExists(Long userId) {
		boolean exists = userRepository.existsById(userId);
		return exists;
	}

	public String getUserName(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
		return user.getUsername();
	}

	public GetUserInfoResponse getUserInfo(Long userId){
		User user=userRepository.findById(userId)
			.orElseThrow(()->new GeneralException(ErrorStatus.USER_NOT_FOUND));
		return GetUserInfoResponse.from(user);
	}
}
