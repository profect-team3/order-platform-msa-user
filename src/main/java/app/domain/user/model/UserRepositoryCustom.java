package app.domain.user.model;

import java.util.Optional;

import app.domain.user.model.entity.User;

public interface UserRepositoryCustom {
	Optional<User> findFirstByUniqueFields(String username, String email, String nickname, String phoneNumber);
}
