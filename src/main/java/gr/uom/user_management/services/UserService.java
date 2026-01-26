package gr.uom.user_management.services;

import gr.uom.user_management.controllers.dto.ResetPasswordRequest;
import gr.uom.user_management.models.*;
import gr.uom.user_management.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import javax.transaction.Transactional;
import java.util.*;
import java.security.SecureRandom;

@Service
public class UserService {

    @Value("${frontend.url}")
    private String frontEndURL;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SkillRepository skillRepository;

    @Autowired
    OccupationRepository occupationRepository;

    @Autowired
    SystemConfigurationRepository systemConfigurationRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MailSendingService mailSendingService;

    SecureRandom secureRandom = new SecureRandom();

    public User createUser(User user, String installation, String organization) {
        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());
        if (!userOptional.isPresent()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles("SIMPLE");
            user.setInstallation(installation);
            if(organization!=null && !organization.isEmpty()){
                Optional<Organization> optionalOrganization = organizationRepository.findByName(organization);
                optionalOrganization.ifPresent(user::setOrganization);
            }
            SystemConfiguration systemConfiguration = new SystemConfiguration();
            user.setConfigurations(systemConfiguration);
            systemConfiguration.setUser(user);

            userRepository.save(user);
            return user;
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Email is used from another user");
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Transactional
    public User updateUser(String auth_id, String id, String country, String streetAddress, String portfolio) {
        System.out.println(UUID.fromString(id));
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with email " + id + " doesn't exist!"
                ));
        if(!user.getId().toString().equals(auth_id)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if(country!=null && !country.isEmpty()){
            user.setCountry(country);
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

    @Transactional
    public List<Skill> updateUserSkillYears(String auth_id, String id, String skillId, Integer newYears) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with email " + id + " doesn't exist!"
                ));
        if(!user.getId().equals(UUID.fromString(auth_id))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Skill skillToUpdate = user.getSkillList().stream()
                .filter(skill -> skill.getSkillId().equals(skillId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Skill with id " + skillId + " doesn't exist for this user!"
                ));
        skillToUpdate.setYears(newYears);

        return user.getSkillList();
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

    @Transactional
    public List<Skill> deleteUserSkill(String auth_id, String id, String skillId) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with email " + id + " doesn't exist!"
                ));
        if(!user.getId().equals(UUID.fromString(auth_id))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Skill skillToRemove = user.getSkillList().stream()
                .filter(skill -> skill.getSkillId().equals(skillId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Skill with id " + skillId + " doesn't exist for this user!"
                ));
        user.getSkillList().remove(skillToRemove);
        skillRepository.delete(skillToRemove);

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

    @Transactional
    public void resetPasswordRequest(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with email " + userEmail + " doesn't exist!"
                ));

        String passwordResetCode = generateCode(100);
        user.setPassResetCode(passwordResetCode);

        mailSendingService.sendPasswordResetEmail(
                user.getEmail(),
                user.getPassResetCode(),
                user.getId(),
                frontEndURL
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String resetToken = resetPasswordRequest.getToken();
        String newPassword = resetPasswordRequest.getPassword();
        UUID id = resetPasswordRequest.getUuid();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with id " + id + " doesn't exist!"
                ));

        if(new Date(System.currentTimeMillis() - 300000).after(user.getPassResetIssuedDate())) { //5 minutes
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Password Reset has expired. Please try again.");
        }
        if(!resetToken.equals(user.getPassResetCode())){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid password reset token.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPassResetCode(null);
    }


    private String generateCode(int length) {
        byte[] buffer = new byte[length];
        secureRandom.nextBytes(buffer);

        String encodedBuffer = Base64.getEncoder().encodeToString(buffer);
        String code = encodedBuffer.substring(0, length);

        return UriUtils.encode(code, "UTF-8");
    }
}
