package app.domain.user.model.dto.response;

import java.time.LocalDate;

import app.domain.user.model.entity.User;
import app.domain.user.model.entity.enums.UserSex;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetUserInfoResponse {

	private Long userId;
	private String username;
	private String email;
	private String nickname;
	private String realName;
	private String phoneNumber;
	private UserSex usersex;
	private LocalDate birthdate;
	private String userRole;

	public GetUserInfoResponse() {
	}

	public GetUserInfoResponse(Long userId, String username, String email, String nickname, String realName, String phoneNumber, UserSex usersex, LocalDate birthdate, String userRole) {
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.nickname = nickname;
		this.realName = realName;
		this.phoneNumber = phoneNumber;
		this.usersex = usersex;
		this.birthdate = birthdate;
		this.userRole = userRole;
	}

	public static GetUserInfoResponse from(User user) {
		return GetUserInfoResponse.builder()
			.userId(user.getUserId())
			.username(user.getUsername())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.realName(user.getRealName())
			.phoneNumber(user.getPhoneNumber())
			.usersex(user.getUsersex())
			.birthdate(user.getBirthdate())
			.userRole(String.valueOf(user.getUserRole()))
			.build();
	}
}
