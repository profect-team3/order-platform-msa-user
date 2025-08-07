package app.unit.domain.user.model;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import app.user.model.UserRepository;
import app.user.model.entity.User;
import app.user.model.entity.enums.UserRole;
import app.global.config.QueryDslConfig;
import app.global.config.TestJpaConfig;

@DataJpaTest
@Import({QueryDslConfig.class, TestJpaConfig.class})
@DisplayName("UserRepository 테스트")
class UserRepositoryCustomTest {

	@Autowired
	private UserRepository userRepository;

	private User existingUser;

	@BeforeEach
	void setUp() {
		userRepository.deleteAllInBatch();

		existingUser = User.builder()
			.username("testuser")
			.password("password")
			.email("test@example.com")
			.nickname("testnick")
			.realName("테스트유저")
			.phoneNumber("01012345678")
			.userRole(UserRole.CUSTOMER)
			.build();
		userRepository.save(existingUser);
	}

	@Nested
	@DisplayName("findFirstByUniqueFields 메서드")
	class FindFirstByUniqueFieldsTest {

		@Test
		@DisplayName("성공: 중복된 username으로 사용자를 조회한다")
		void findByUsername_Success() {
			// when
			Optional<User> foundUser = userRepository.findFirstByUniqueFields(
				"testuser", "new@email.com", "new_nick", "01099998888");

			// then
			assertThat(foundUser).isPresent();
			assertThat(foundUser.get().getUsername()).isEqualTo(existingUser.getUsername());
		}

		@Test
		@DisplayName("성공: 중복된 email로 사용자를 조회한다")
		void findByEmail_Success() {
			// when
			Optional<User> foundUser = userRepository.findFirstByUniqueFields(
				"new_user", "test@example.com", "new_nick", "01099998888");

			// then
			assertThat(foundUser).isPresent();
			assertThat(foundUser.get().getEmail()).isEqualTo(existingUser.getEmail());
		}

		@Test
		@DisplayName("성공: 중복된 nickname으로 사용자를 조회한다")
		void findByNickname_Success() {
			// when
			Optional<User> foundUser = userRepository.findFirstByUniqueFields(
				"new_user", "new@email.com", "testnick", "01099998888");

			// then
			assertThat(foundUser).isPresent();
			assertThat(foundUser.get().getNickname()).isEqualTo(existingUser.getNickname());
		}

		@Test
		@DisplayName("성공: 중복된 phoneNumber로 사용자를 조회한다")
		void findByPhoneNumber_Success() {
			// when
			Optional<User> foundUser = userRepository.findFirstByUniqueFields(
				"new_user", "new@email.com", "new_nick", "01012345678");

			// then
			assertThat(foundUser).isPresent();
			assertThat(foundUser.get().getPhoneNumber()).isEqualTo(existingUser.getPhoneNumber());
		}

		@Test
		@DisplayName("실패: 중복된 필드가 하나도 없을 경우, 빈 Optional을 반환한다")
		void findWithNoDuplicates_ShouldReturnEmpty() {
			// when
			Optional<User> foundUser = userRepository.findFirstByUniqueFields(
				"new_user", "new@email.com", "new_nick", "01099998888");

			// then
			assertThat(foundUser).isNotPresent();
		}

		@Test
		@DisplayName("성공: 여러 필드가 중복될 경우에도 사용자를 조회한다 (OR 조건 테스트)")
		void findWithMultipleDuplicates_ShouldReturnUser() {
			// given
			String duplicateUsername = "testuser";
			String newEmail = "new@email.com";
			String newNickname = "new_nick";
			String duplicatePhoneNumber = "01012345678";

			// when
			Optional<User> foundUser = userRepository.findFirstByUniqueFields(
				duplicateUsername, newEmail, newNickname, duplicatePhoneNumber
			);

			// then
			assertThat(foundUser).isPresent();
			assertThat(foundUser.get().getUserId()).isEqualTo(existingUser.getUserId());
		}
	}
}