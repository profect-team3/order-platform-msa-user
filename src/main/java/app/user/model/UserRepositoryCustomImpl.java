package app.user.model;

import static app.user.model.entity.QUser.*;

import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import app.user.model.entity.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<User> findFirstByUniqueFields(String username, String email, String nickname, String phoneNumber) {
		BooleanExpression predicate = user.username.eq(username)
			.or(user.email.eq(email))
			.or(user.nickname.eq(nickname))
			.or(user.phoneNumber.eq(phoneNumber));

		User foundUser = queryFactory
			.selectFrom(user)
			.where(predicate)
			.fetchFirst();

		return Optional.ofNullable(foundUser);
	}
}