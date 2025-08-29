package app.domain.user.status;

import org.springframework.http.HttpStatus;

import app.commonUtil.apiPayload.code.BaseCode;
import app.commonUtil.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseCode {
	USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER001", "이미 존재하는 유저입니다."),
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER002", "이미 사용 중인 이메일입니다."),
	NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER003", "이미 사용 중인 닉네임입니다."),
	PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER004", "이미 사용 중인 전화번호입니다."),

	AUTHENTICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "USER005", "인증 정보를 찾을 수 없습니다."),
	LOGOUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "USER006","로그아웃에 실패하여 회원탈퇴를 실패했습니다."),
	CREATE_USER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "USER007", "사용자 생성에 실패했습니다."),
	ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS001", "해당하는 주소를 찾을 수 없습니다."),
	ADDRESS_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ADDRESS002", "사용자 권한이 없는 주소입니다."),
	CANNOT_DELETE_DEFAULT_ADDRESS(HttpStatus.CONFLICT, "ADDRESS003", "기본 주소는 삭제할 수 없습니다. 기본 주소를 변경해주세요."),
	ADDRESS_ALREADY_DELETED(HttpStatus.GONE, "ADDRESS_410_1", "이미 삭제된 주소는 수정할 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ReasonDTO getReason() {
		return ReasonDTO.builder()
			.message(message)
			.code(code)
			.build();
	}

	@Override
	public ReasonDTO getReasonHttpStatus() {
		return ReasonDTO.builder()
			.isSuccess(false)
			.message(message)
			.code(code)
			.httpStatus(httpStatus)
			.build();
	}
}
