package app.user;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import app.domain.cart.model.entity.Cart;
//import app.domain.cart.model.repository.CartRepository;
import app.user.client.InternalOrderClient;
import app.user.model.UserRepository;
import app.user.model.dto.request.CreateUserRequest;
import app.user.model.dto.response.CreateUserResponse;
import app.user.model.dto.response.GetUserInfoResponse;
import app.user.model.entity.User;
import app.user.status.UserErrorStatus;
import app.global.SecurityUtil;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final InternalOrderClient internalOrderClient;
	private final RedisTemplate<String, String> redisTemplate;
	private final SecurityUtil securityUtil;
	private static final String REFRESH_TOKEN_PREFIX = "RT:";
	private static final String BLACKLIST_PREFIX = "BL:";

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
			internalOrderClient.createCart(savedUser.getUserId());
			return CreateUserResponse.from(savedUser);
		} catch (DataAccessException e) {
			log.error("데이터베이스에 사용자 등록을 실패했습니다.", e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}


	@Transactional
	public void withdrawMembership() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Long currentUserId = Long.parseLong(authentication.getName());

		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

		user.anonymizeForWithdrawal();

		userRepository.delete(user);

		//logout();
	}

	@Transactional
	public GetUserInfoResponse getUserInfo() {
		User currentUser = securityUtil.getCurrentUser();
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