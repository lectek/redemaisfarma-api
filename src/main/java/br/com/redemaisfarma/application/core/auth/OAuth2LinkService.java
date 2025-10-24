/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
 *  org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
 *  org.springframework.security.oauth2.core.OAuth2AuthenticationException
 *  org.springframework.security.oauth2.core.user.OAuth2User
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.application.core.auth;

import br.com.redemaisfarma.application.core.auth.SocialCustomerService;
import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuth2LinkService
extends DefaultOAuth2UserService {
    private final SocialCustomerService socialCustomerService;

    public OAuth2LinkService(SocialCustomerService socialCustomerService) {
        this.socialCustomerService = socialCustomerService;
    }

    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map a = oauth.getAttributes();
        String email = this.extractEmail(provider, a);
        String name = this.extractName(provider, a);
        String userId = this.extractId(provider, a);
        String avatar = this.extractAvatar(provider, a);
        boolean emailVerified = this.extractEmailVerified(provider, a);
        this.socialCustomerService.linkOrCreate(provider, userId, email, name, avatar, emailVerified);
        return oauth;
    }

    private String extractEmail(String provider, Map<String, Object> a) {
        Object v = a.get("email");
        return v != null ? v.toString() : null;
    }

    private boolean extractEmailVerified(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            Object v = a.get("email_verified");
            if (v instanceof Boolean) {
                Boolean b = (Boolean)v;
                return b;
            }
            if (v != null) {
                return Boolean.parseBoolean(v.toString());
            }
        }
        return false;
    }

    private String extractName(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            Object v = a.get("name");
            if (v != null) {
                return v.toString();
            }
            v = a.get("given_name");
            if (v != null) {
                return v.toString();
            }
            v = a.get("family_name");
            if (v != null) {
                return v.toString();
            }
            return "Cliente";
        }
        Object v = a.get("name");
        return v != null ? v.toString() : "Cliente";
    }

    private String extractId(String provider, Map<String, Object> a) {
        if ("google".equals(provider)) {
            Object v = a.get("sub");
            return v != null ? v.toString() : null;
        }
        Object v = a.get("id");
        return v != null ? v.toString() : null;
    }

    private String extractAvatar(String provider, Map<String, Object> a) {
        Map d;
        Object url;
        Map m;
        Object data;
        if ("google".equals(provider)) {
            Object v = a.get("picture");
            return v != null ? v.toString() : null;
        }
        Object pic = a.get("picture");
        if (pic instanceof Map && (data = (m = (Map)pic).get("data")) instanceof Map && (url = (d = (Map)data).get("url")) != null) {
            return url.toString();
        }
        return null;
    }
}

