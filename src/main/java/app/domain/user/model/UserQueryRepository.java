package app.domain.user.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import app.domain.user.model.entity.User;

public interface UserQueryRepository {

	Page<User> searchUser(String keyWord, Pageable pageable);
}
