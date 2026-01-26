package gr.uom.user_management.config;

import gr.uom.user_management.models.Organization;
import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.OrganizationRepository;
import gr.uom.user_management.repositories.UserRepository;
import gr.uom.user_management.services.UserPrivilegedService;
import gr.uom.user_management.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

@Configuration
@Profile("!test")
public class Config {

    @Value("${app.installation}")
    private String installation;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.organization.name}")
    private String orgName;

    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository, UserService userService,
                                        UserPrivilegedService userPrivilegedService,
                                        OrganizationRepository organizationRepository){
        return args -> {
            //Create default organization
            Optional<Organization> optionalOrganization = organizationRepository.findByName(orgName);
            if(optionalOrganization.isEmpty()){
                Organization organization = new Organization(orgName);
                organizationRepository.save(organization);
            }

            //Create admin user
            Optional<User> userOptional = userRepository.findByEmail(adminEmail);
            if(!userOptional.isPresent()){
                User admin = new User("Admin",adminEmail,adminPassword);
                userService.createUser(admin, installation, orgName);
                userPrivilegedService.givePrivilegeToUser(adminEmail);
            }

        };
    }
}
