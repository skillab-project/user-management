package gr.uom.user_management.services;

import gr.uom.user_management.models.SecurityUser;
import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional= userRepository.findByEmail(username);
        if(userOptional.isPresent()) {
            return new SecurityUser(userOptional.get());
        }

        throw new UsernameNotFoundException("Email not found: " + username);
    }
}