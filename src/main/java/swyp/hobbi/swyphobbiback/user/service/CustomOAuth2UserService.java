package swyp.hobbi.swyphobbiback.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import swyp.hobbi.swyphobbiback.user.domain.OauthLogin;
import swyp.hobbi.swyphobbiback.user.oauth.OAuth2UserInfo;
import swyp.hobbi.swyphobbiback.user.oauth.OAuth2UserInfoFactory;
import swyp.hobbi.swyphobbiback.user.repository.OAuthLoginRepository;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService{

    private final OAuthLoginRepository oauthLoginRepository;
    private final ObjectProvider<HttpServletRequest> requestProvider;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId(); //어떤 소셜로그인인지 확인 "google", "kakao"

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());
        String oauthEmail = userInfo.getEmail();

        // 기존 attribute 복사 + 추가 데이터 삽입
        Map<String, Object> modifiedAttributes = new HashMap<>(oAuth2User.getAttributes());
        modifiedAttributes.put("email", oauthEmail);
        modifiedAttributes.put("provider", provider);

        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("SERVER_ERROR", "요청 정보를 가져올 수 없습니다.", null));
        }

        HttpSession session = request.getSession();
        session.setAttribute("pendingSocialEmail", oauthEmail);
        session.setAttribute("pendingSocialProvider", provider);

        // 연동 확인
        OauthLogin oauthLogin = oauthLoginRepository.findByProviderEmailAndProvider(oauthEmail, provider)
                .orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error("USER_NOT_FOUND", "연동되지 않은 사용자입니다.", null)));

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                modifiedAttributes,
                "email"
        );

    }
}
