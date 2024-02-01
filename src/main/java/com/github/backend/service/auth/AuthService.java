package com.github.backend.service.auth;

import com.amazonaws.services.kms.model.NotFoundException;
import com.github.backend.config.security.JwtTokenProvider;
import com.github.backend.config.security.util.SecurityUtil;
import com.github.backend.repository.AuthRepository;
import com.github.backend.repository.RefreshTokenRepository;
import com.github.backend.repository.RolesRepository;
import com.github.backend.web.dto.users.*;
import com.github.backend.web.entity.RefreshToken;
import com.github.backend.web.entity.RolesEntity;
import com.github.backend.web.entity.UserEntity;
import com.github.backend.web.entity.custom.CustomUserDetails;
import com.github.backend.web.entity.enums.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final AuthRepository authRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RolesRepository rolesRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

  private final String BEARER_TYPE = "Bearer";
  private final Long ACCESS_TOKEN_EXPIRED_TIME = 60 * 60000 * 1000L;
    /**
     * UsernamePasswordAuthenticationToken을 통한 Spring Security인증 진행
     * 이후 tokenService에 userId값을 전달하여 토큰 생성
     * @param requestDTO
     * @return TokenDTO
     */

    @Transactional
    public TokenDto login(RequestLoginDto requestDTO) {
        UserEntity users = authRepository.findByUserId(requestDTO.getUserId()).orElseThrow(() -> new NotFoundException("아이디 혹은 비밀번호를 틀리셨습니다."));
        String isDeleted = users.getIsDeleted();
        if(isDeleted != null && isDeleted.equals("deleted"))
          throw new NotFoundException("이미 탈퇴한 계정입니다.");

        if(!passwordEncoder.matches(requestDTO.getPassword(), users.getPassword()))
          throw new NotFoundException("아이디 혹은 비밀번호가 틀렸습니다.");

        // 1. ID(email)/PW 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = requestDTO.toAuthentication();
        // 2. 실제 검증 로직(사용자 비밀번호 체크)
        // authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(authentication, users.getRoles().getRolesName());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, users.getRoles().getRolesName());


        // 4. RefreshToken 저장
        RefreshToken refreshTokenResult = RefreshToken.builder()
                .key(authentication.getName())
                .value(refreshToken)
                .build();
        refreshTokenRepository.save(refreshTokenResult);

        TokenDto tokenDto = TokenDto.builder()
                .tokenType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .duration(Duration.ofMillis(ACCESS_TOKEN_EXPIRED_TIME))
                .build();
        // 5. 토큰 발급
        return tokenDto;
    }

    @Transactional
    public void signup(RequestUserDto requestDto) {
        if(authRepository.existsByUserId(requestDto.getUserId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        if(!rolesRepository.existsById(requestDto.getRoles()))
          throw new RuntimeException("권한이 존재하지 않습니다.");

        RolesEntity roles = rolesRepository.findById(requestDto.getRoles()).orElseThrow(() -> new NotFoundException("권한이 존재하지 않습니다."));

        Gender gender;
        if(requestDto.getGender().equals("남자"))
          gender = Gender.MEN;
        else
          gender = Gender.WOMEN;

        UserEntity user = UserEntity.builder()
                .userId(requestDto.getUserId())
                .password(requestDto.getPassword())
                .email(requestDto.getEmail())
                .nickname(requestDto.getNickname())
                .phoneNumber(requestDto.getPhoneNumber())
                .address(requestDto.getAddress())
                .gender(gender)
                .roles(roles)
                .isDeleted(null)
                .build();


        log.info("[build] user = " + user);
        authRepository.save(user);
        // 프로필 이미지가 있다면 추가
  //      if(multipartFile != null) {
  //          ProfileImageEntity uploadImages = imageUploadService.profileUploadImage(multipartFile);
  //          user.setProfileImage(uploadImages);
  //          authRepository.save(user);
  //          log.info("[profileImage] 유저프로필 이미지가 추가되었습니다. uploadedImages = " + uploadImages);
  //      }
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public TokenDto refresh(RequestTokenDto requestTokenDto, CustomUserDetails customUserDetails) {
        // 1. Refresh Token 검증 (validateToken() : 토큰 검증)
        if(!jwtTokenProvider.validationToken(requestTokenDto.getRefreshToken())) {
          throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        if(customUserDetails.getUser() == null){
          throw new RuntimeException("접속시간이 만료되었습니다.");
        }

        // 2. Access Token에서 Studio ID 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(requestTokenDto.getAccessToken());

        // 3. 저장소에서 Studio ID를 기반으로 Refresh Token값 가져오기
        RefreshToken findRefreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치 여부
        if (!findRefreshToken.getValue().equals(requestTokenDto.getRefreshToken())) {
          throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(authentication, customUserDetails.getUser().getRoles().getRolesName());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, customUserDetails.getUser().getRoles().getRolesName());

        // 6. 저장소 정보 업데이트
        RefreshToken newRefreshToken = findRefreshToken.updateValue(refreshToken);
        refreshTokenRepository.save(newRefreshToken);

        TokenDto tokenDto = TokenDto.builder().accessToken(accessToken).refreshToken(refreshToken).build();
        // 토큰 발급
        return tokenDto;
    }

    public String userIdCheck(String userEmail) {
        UserEntity users = authRepository.findByUserId(userEmail).orElseThrow(() -> new NotFoundException("사용불가한 이메일 입니다."));
        return "사용 가능한 이메일입니다.";
    }

}