package gr.uom.user_management.config;

import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.UserRepository;
import gr.uom.user_management.services.UserPrivilegedService;
import gr.uom.user_management.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

@Configuration
@Profile("!test")
public class Config {
    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository, UserService userService,
                                        UserPrivilegedService userPrivilegedService){
        return args -> {
            //Create admin user
            Optional<User> userOptional = userRepository.findByEmail("admin@skillab.eu");
            if(!userOptional.isPresent()){
                User admin = new User("Admin","admin@skillab.eu","adminskillab");
                userService.createUser(admin);
                userPrivilegedService.givePrivilegeToUser("admin@skillab.eu");
            }

        };
    }
}
