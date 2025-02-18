package gr.uom.user_management.config;

import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.UserRepository;
import gr.uom.user_management.services.UserPrivilegedService;
import gr.uom.user_management.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class Config {
    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository, UserService userService,
                                        UserPrivilegedService userPrivilegedService){
        return args -> {
            //Create admin user
            Optional<User> userOptional = userRepository.findByEmail("admin@gr");
            if(!userOptional.isPresent()){
                User admin = new User("Admin","admin@gr","admin");
                userService.createUser(admin);
                userPrivilegedService.verifyUser("admin@gr");
                userPrivilegedService.givePrivilegeToUser("admin@gr");
            }

        };
    }
}
