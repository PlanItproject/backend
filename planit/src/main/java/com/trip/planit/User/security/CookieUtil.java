package com.trip.planit.User.security;

import com.trip.planit.User.entity.CookieRule;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieUtil {

    public String resolveTokenFromCookie(Cookie[] cookies, CookieRule cookieRule) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieRule.getValue()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse("");
    }
}
