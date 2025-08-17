package app.domain.user.listener;

import feign.FeignException;
import app.domain.user.client.InternalOrderClient;
import app.domain.user.event.UserSignedUpEvent;
import app.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Recover;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventsListener {

	private final InternalOrderClient internalOrderClient;

	@Async
	@EventListener
	@Retryable(
		value = { feign.FeignException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 2000)
	)
	public void handleUserSignedUpEvent(UserSignedUpEvent event) {
		log.info("회원가입 이벤트 수신 (비동기 처리 시작): userId={}", event.getUserId());

		try {
			ApiResponse<String> response = internalOrderClient.createCart(event.getUserId());

			if (response == null || !response.isSuccess()) {
				log.error("신규 회원 장바구니 생성 실패: userId={}, 응답: {}", event.getUserId(), response);
			} else {
				log.info("신규 회원 장바구니 생성 완료: userId={}", event.getUserId());
			}
		} catch (Exception e) {
			log.error("장바구니 생성 API 호출 중 예외 발생: userId={}", event.getUserId(), e);
		}
	}

	@Recover
	public void recover(feign.FeignException e, UserSignedUpEvent event) {
		log.error("최종 실패 - 장바구니 생성 실패: userId={}, error={}", event.getUserId(), e.getMessage());
	}
}