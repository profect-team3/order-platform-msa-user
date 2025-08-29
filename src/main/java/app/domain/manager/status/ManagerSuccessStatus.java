package app.domain.manager.status;

import org.springframework.http.HttpStatus;

import app.commonUtil.apiPayload.code.BaseCode;
import app.commonUtil.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ManagerSuccessStatus implements BaseCode {

	MANAGER_GET_CUSTOMER_OK(HttpStatus.OK, "MANAGER200", "관리자의 유저 목록 조회가 성공했습니다."),
	MANAGER_GET_CUSTOMER_DETAIL_OK(HttpStatus.OK, "MANAGER201", "관리자의 유저 상세 조회가 성공했습니다."),
	MANAGER_SEARCH_CUSTOMER_OK(HttpStatus.OK, "MANAGER203", "관리자의 유저 검색이 성공했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ReasonDTO getReason() {
		return ReasonDTO.builder()
			.isSuccess(true)
			.message(message)
			.code(code)
			.build();
	}

	@Override
	public ReasonDTO getReasonHttpStatus() {
		return ReasonDTO.builder()
			.isSuccess(true)
			.message(message)
			.code(code)
			.httpStatus(httpStatus)
			.build();
	}
}
