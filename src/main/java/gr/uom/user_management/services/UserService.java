package gr.uom.user_management.services;

import gr.uom.user_management.models.Occupation;
import gr.uom.user_management.models.Skill;
import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.OccupationRepository;
import gr.uom.user_management.repositories.SkillRepository;
import gr.uom.user_management.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    SkillRepository skillRepository;

    @Autowired
    OccupationRepository occupationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());
        if (!userOptional.isPresent()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles("SIMPLE");
            userRepository.save(user);
            return user;
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Email is used from another user");
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Transactional
    public User updateUser(String auth_id, String id, String streetAddress, String portfolio) {
        System.out.println(UUID.fromString(id));
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with email " + id + " doesn't exist!"
                ));
        if(!user.getId().toString().equals(auth_id)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if(streetAddress!=null && !streetAddress.isEmpty()){
            user.setStreetAddress(streetAddress);
        }
        if(portfolio!=null && !portfolio.isEmpty()){
            user.setPortfolio(portfolio);
        }

        return user;
    }

    @Transactional
    public User updateUserSkills(String auth_id, String id, Skill skill) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with email " + id + " doesn't exist!"
                ));
        if(!user.getId().equals(UUID.fromString(auth_id))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        skill.setUser(user);
        skillRepository.save(skill);
        user.addSkill(skill);

        return user;
    }

    public List<Skill> getUserSkills(String auth_id, String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with email " + id + " doesn't exist!"
                ));
        if(!user.getId().equals(UUID.fromString(auth_id))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return user.getSkillList();
    }

    public User getUser(String id) {
        return userRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User with email " + id + " doesn't exist!"
            ));
    }

    @Transactional
    public User updateUserOccupation(String auth_id, String id, Occupation occupation) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with email " + id + " doesn't exist!"
                ));
        if(!user.getId().equals(UUID.fromString(auth_id))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        //Delete old
        if(user.getTargetOccupation()!=null){
            occupationRepository.delete(user.getTargetOccupation());
        }

        //Save new
        occupation.setUser(user);
        occupationRepository.save(occupation);
        user.setTargetOccupation(occupation);

        return user;
    }
}
