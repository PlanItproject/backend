package com.trip.planit.User.service;

import com.trip.planit.User.entity.User;
import com.trip.planit.User.repository.UserRepository;
import com.trip.planit.User.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // 여기만 바꿔주면 나머지 코드는 전혀 손 댈 필요 없습니다.
        return new CustomUserDetails(user);
    }
}
