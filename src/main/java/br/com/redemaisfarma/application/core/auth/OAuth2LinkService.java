package br.com.redemaisfarma.application.core.auth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OAuth2LinkService extends DefaultOAuth2UserService {

    private final SocialCustomerService socialCustomerService;

    public OAuth2LinkService(SocialCustomerService socialCustomerService) {
        this.socialCustomerService = socialCustomerService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google" | "facebook"
        Map<String, Object> a = oauth.getAttributes();

        String email   = extractEmail(provider, a);
        String name    = extractName(provider, a);
        String userId  = extractId(provider, a);
        String avatar  = extractAvatar(provider, a);
        boolean emailVerified = extractEmailVerified(provider, a);

        socialCustomerService.linkOrCreate(provider, userId, email, name, avatar, emailVerified);
        return oauth;
    }

    private String extractEmail(String provider, Map<String, Object> a) {
        Object v = a.get("email");
        return v != null ? v.toString() : null;
    }

    private boolean extractEmailVerified(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            Object v = a.get("email_verified");
            if (v instanceof Boolean b) return b;
            if (v != null) return Boolean.parseBoolean(v.toString());
        }
        return false;
    }

    private String extractName(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            Object v = a.get("name");
            if (v != null) return v.toString();
            v = a.get("given_name");
            if (v != null) return v.toString();
            v = a.get("family_name");
            if (v != null) return v.toString();
            return "Cliente";
        }
        Object v = a.get("name"); // facebook
        return v != null ? v.toString() : "Cliente";
    }

    private String extractId(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            Object v = a.get("sub"); // OIDC subject
            return v != null ? v.toString() : null;
        }
        Object v = a.get("id"); // facebook
        return v != null ? v.toString() : null;
    }

    private String extractAvatar(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            Object v = a.get("picture");
            return v != null ? v.toString() : null;
        }
        // facebook
        Object pic = a.get("picture");
        if (pic instanceof Map<?, ?> m) {
            Object data = m.get("data");
            if (data instanceof Map<?, ?> d) {
                Object url = d.get("url");
                if (url != null) return url.toString();
            }
        }
        return null;
    }
}
