package org.agard.InventoryManagement.config;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.config.DbUserDetails;
import org.agard.InventoryManagement.domain.User;
import org.agard.InventoryManagement.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("userDetailsService")
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    throw new UsernameNotFoundException("Could not find user with provided credentials");
                });

        return new DbUserDetails(user);
    }
}
