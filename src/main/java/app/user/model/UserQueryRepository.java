package app.user.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import app.user.model.entity.User;

public interface UserQueryRepository {

	Page<User> searchUser(String keyWord, Pageable pageable);
}
