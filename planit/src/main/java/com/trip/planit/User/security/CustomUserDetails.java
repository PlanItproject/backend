package com.trip.planit.User.security;

import com.trip.planit.User.entity.User;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
  private final Long userId;

  public CustomUserDetails(User user) {
    super(
        user.getEmail(),               // username 으로 이메일
        user.getPassword(),            // password
        List.of(new SimpleGrantedAuthority(user.getRole().name())) // 권한
    );
    this.userId = user.getUserId();
  }
}
