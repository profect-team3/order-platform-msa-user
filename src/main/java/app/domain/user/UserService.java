package app.domain.user;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import app.domain.user.client.InternalAuthClient;
import app.domain.user.client.InternalOrderClient;
import app.domain.user.model.UserRepository;
import app.domain.user.model.dto.request.CreateUserRequest;
import app.domain.user.model.dto.response.CreateUserResponse;
import app.domain.user.model.dto.response.GetUserInfoResponse;
import app.domain.user.model.entity.User;
import app.domain.user.status.UserErrorStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final InternalAuthClient internalAuthClient;
	private final InternalOrderClient internalOrderClient;

	@Transactional
	public CreateUserResponse createUser(CreateUserRequest createUserRequest) {

		validateUserUniqueness(createUserRequest);

		String encryptedPassword = passwordEncoder.encode(createUserRequest.getPassword());

		User user = User.builder()
			.username(createUserRequest.getUsername())
			.password(encryptedPassword)
			.email(createUserRequest.getEmail())
			.nickname(createUserRequest.getNickname())
			.realName(createUserRequest.getRealName())
			.phoneNumber(createUserRequest.getPhoneNumber())
			.userRole(createUserRequest.getUserRole())
			.build();

		try {
			User savedUser = userRepository.save(user);
			ApiResponse<String> response =internalOrderClient.createCart(savedUser.getUserId());
			return CreateUserResponse.from(savedUser);
		} catch (DataAccessException e) {
			log.error("데이터베이스에 사용자 등록을 실패했습니다.", e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		} catch (HttpServerErrorException | HttpClientErrorException e) {
			log.error("CartService Error :{}" ,e.getResponseBodyAsString());
			throw new GeneralException(UserErrorStatus.CREATE_CART_FAILED);
		}
	}


	@Transactional
	public void withdrawMembership(Long userId) {

		User user = userRepository.findByUserId(userId)
				.orElseThrow(()->new GeneralException(ErrorStatus.USER_NOT_FOUND));

		user.anonymizeForWithdrawal();

		userRepository.delete(user);
		try {
			ApiResponse<String> response =internalAuthClient.logout();
		} catch (HttpServerErrorException | HttpClientErrorException e){
			log.error("AuthService Error: {}",e.getResponseBodyAsString());
			throw new GeneralException(UserErrorStatus.LOGOUT_FAILED);
		}
	}

	@Transactional
	public GetUserInfoResponse getUserInfo(Long userId) {
		User currentUser = userRepository.findByUserId(userId)
			.orElseThrow(()->new GeneralException(ErrorStatus.USER_NOT_FOUND));
		return GetUserInfoResponse.from(currentUser);
	}

	private void validateUserUniqueness(CreateUserRequest createUserRequest) {
		userRepository.findFirstByUniqueFields(
			createUserRequest.getUsername(),
			createUserRequest.getEmail(),
			createUserRequest.getNickname(),
			createUserRequest.getPhoneNumber()
		).ifPresent(user -> {
			if (user.getUsername().equals(createUserRequest.getUsername())) {
				throw new GeneralException(UserErrorStatus.USER_ALREADY_EXISTS);
			}
			if (user.getEmail().equals(createUserRequest.getEmail())) {
				throw new GeneralException(UserErrorStatus.EMAIL_ALREADY_EXISTS);
			}
			if (user.getNickname().equals(createUserRequest.getNickname())) {
				throw new GeneralException(UserErrorStatus.NICKNAME_ALREADY_EXISTS);
			}
			if (user.getPhoneNumber().equals(createUserRequest.getPhoneNumber())) {
				throw new GeneralException(UserErrorStatus.PHONE_NUMBER_ALREADY_EXISTS);
			}
		});
	}
}