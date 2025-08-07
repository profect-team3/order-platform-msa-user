package app.user.model;

import java.util.Optional;

import app.user.model.entity.User;

public interface UserRepositoryCustom {
	Optional<User> findFirstByUniqueFields(String username, String email, String nickname, String phoneNumber);
}
