package com.yilinglin10.authservice.config;

import com.yilinglin10.authservice.entity.UserProfile;
import com.yilinglin10.authservice.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserProfile> result = userProfileRepository.findByUsername(username);
        return result.map(CustomUserDetails::new).orElseThrow(()-> new UsernameNotFoundException("user not found with name: "+ username));
    }
}
