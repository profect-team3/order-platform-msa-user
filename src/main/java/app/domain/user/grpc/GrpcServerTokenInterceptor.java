package app.domain.user.grpc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;

import app.commonUtil.security.config.JwtAuthenticationConverter;
import io.grpc.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class GrpcServerTokenInterceptor {

	private final JwtDecoder jwtDecoder;
	private final JwtAuthenticationConverter jwtAuthenticationConverter;

	private static final Metadata.Key<String> SERVER_AUTH_KEY =
		Metadata.Key.of("Server-Authorization",Metadata.ASCII_STRING_MARSHALLER);

	@Bean
	public GrpcAuthenticationReader grpcAuthenticationReader() {
		List<GrpcAuthenticationReader> readers = new ArrayList<>();

		readers.add((context, headers) -> {
			String authHeader = headers.get(SERVER_AUTH_KEY);
			if (authHeader == null || !authHeader.startsWith("Server ")) {
				log.error("서버 인증 헤더가 없거나 잘못된 형식입니다");
				throw new AuthenticationException("Missing or invalid Server-Authorization header") {};
			}
			
			try {
				String token = authHeader.substring(7);
				Jwt jwt = jwtDecoder.decode(token);
				return jwtAuthenticationConverter.convert(jwt);
			} catch (Exception e) {
				log.error("서버 토큰 인증 실패: {}", e.getMessage());
				throw new AuthenticationException("Token validation failed: " + e.getMessage()) {};
			}
		});

		return new CompositeGrpcAuthenticationReader(readers);
	}
}
