package app.domain.user.event;

public class UserSignedUpEvent {
	private final Long userId;

	public UserSignedUpEvent(Long userId) {
		this.userId = userId;
	}

	public Long getUserId() {
		return userId;
	}
}
