package app.domain.user.grpc;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import net.devh.boot.grpc.server.service.GrpcService;

import app.commonUtil.apiPayload.code.status.ErrorStatus;
import app.commonUtil.apiPayload.exception.GeneralException;
import app.commonUtil.security.TokenPrincipalParser;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@GrpcService
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserInfoServiceGrpcImpl extends UserInfoServiceGrpc.UserInfoServiceImplBase {

    private final UserRepository userRepository;
    private final TokenPrincipalParser tokenPrincipalParser;

    @Override
    public void getUserInfo(Empty request, StreamObserver<UserInfoProto.GetUserInfoResponse> responseObserver) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userIdStr = tokenPrincipalParser.getUserId(authentication);
            Long userId = Long.parseLong(userIdStr);

            User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

            UserInfoProto.GetUserInfoResponse response = UserInfoProto.GetUserInfoResponse.newBuilder()
                .setUserId(user.getUserId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setNickname(user.getNickname())
                .setRealName(user.getRealName())
                .setPhoneNumber(user.getPhoneNumber())
                // .setUserRole(user.getUserRole().name())
                // .setUserSex(UserInfoProto.UserSex.valueOf(user.getUsersex().name()))
                // .setBirthdate(user.getBirthdate().toString())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("getUserInfo 메서드 실행 중 오류 발생", e);
            responseObserver.onError(e);
        }
    }
}
