package app.domain.user.grpc;

import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import app.commonUtil.security.config.JwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Primary
@Slf4j
@RequiredArgsConstructor
@Component
public class GrpcAuthenticationManagerImpl implements AuthenticationManager {

	private final JwtAuthenticationConverter jwtAuthenticationConverter;
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		try{
			log.info("grpc authentication manager");
			if(authentication.getPrincipal() instanceof Jwt){
				return authentication;
			}
			else{
				return jwtAuthenticationConverter.convert((Jwt)authentication.getPrincipal());
			}
		} catch (Exception e){
			log.error("grpc metadata 인증 처리 중 예상치 못한 오류 발생",e);
		}

		return authentication;
	}
}
