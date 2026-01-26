package gr.uom.user_management.services;

import gr.uom.user_management.models.Organization;
import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.OrganizationRepository;
import gr.uom.user_management.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.text.html.Option;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


@Service
public class UserPrivilegedService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Transactional
    public User givePrivilegeToUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User with email "+ email +" doesn't exist!"
        ));
        String roles=user.getRoles();
        if(roles.equals("SIMPLE,PRIVILEGED")){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "This user is Privileged");
        }
        user.setRoles(roles+",PRIVILEGED");
        return user;
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User with email "+ email +" doesn't exist!"
        ));
        userRepository.delete(user);
    }

    @Transactional
    public User changeUserOrganization(String email, String name) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User with email "+ email +" doesn't exist!"
        ));
        Organization organization = organizationRepository.findByName(name).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Organization with name "+name+" doesn't exist!"
        ));
        user.setOrganization(organization);
        return user;
    }

    public Organization createOrganization(String name) {
        Optional<Organization> optionalOrganization = organizationRepository.findByName(name);
        if(optionalOrganization.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Organization with this name exists!");
        }

        Organization organization = new Organization(name);
        organizationRepository.save(organization);
        return organization;
    }

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

}
