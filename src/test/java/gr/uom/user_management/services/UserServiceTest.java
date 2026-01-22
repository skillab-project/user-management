package gr.uom.user_management.services;

import gr.uom.user_management.models.Occupation;
import gr.uom.user_management.models.Skill;
import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.OccupationRepository;
import gr.uom.user_management.repositories.SkillRepository;
import gr.uom.user_management.repositories.SystemConfigurationRepository;
import gr.uom.user_management.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private SkillRepository skillRepository;
    private OccupationRepository occupationRepository;
    private SystemConfigurationRepository systemConfigurationRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        skillRepository = mock(SkillRepository.class);
        occupationRepository = mock(OccupationRepository.class);
        systemConfigurationRepository = mock(SystemConfigurationRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        userService = new UserService();
        userService.userRepository = userRepository;
        userService.skillRepository = skillRepository;
        userService.occupationRepository = occupationRepository;
        userService.systemConfigurationRepository = systemConfigurationRepository;
        userService.passwordEncoder = passwordEncoder;
    }

    @Test
    void createUser_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("plainpassword");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainpassword")).thenReturn("encodedpassword");

        User createdUser = userService.createUser(user, "citizen","");

        assertEquals("SIMPLE", createdUser.getRoles());
        assertEquals("encodedpassword", createdUser.getPassword());

        verify(userRepository).save(user);
    }

    @Test
    void createUser_emailAlreadyExists() {
        User existingUser = new User();
        existingUser.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(existingUser, "citizen","");
        });

        assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatus());
    }

    @Test
    void getUser_notFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.getUser(id.toString());
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateUser_success() {
        UUID userId = UUID.randomUUID();
        String authId = userId.toString();
        User user = new User();
        user.setId(userId);
        user.setStreetAddress("Old Address");
        user.setPortfolio("Old Portfolio");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User updatedUser = userService.updateUser(authId, userId.toString(), "Greece" ,"New Address", "New Portfolio");

        assertEquals("New Address", updatedUser.getStreetAddress());
        assertEquals("New Portfolio", updatedUser.getPortfolio());
    }

    @Test
    void updateUser_userNotFound() {
        UUID userId = UUID.randomUUID();
        String authId = UUID.randomUUID().toString(); // different ID for auth user

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(authId, userId.toString(),"Greece", "New Address", "New Portfolio");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateUser_forbidden() {
        UUID userId = UUID.randomUUID();
        String authId = UUID.randomUUID().toString(); // different ID for auth user
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(authId, userId.toString(), "Greece", "New Address", "New Portfolio");
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }


    @Test
    void updateUserOccupation_success() {
        UUID userId = UUID.randomUUID();
        String authId = userId.toString();
        User user = new User();
        user.setId(userId);
        Occupation occupation = new Occupation();
        occupation.setOccupationLabel("Software Engineer");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User updatedUser = userService.updateUserOccupation(authId, userId.toString(), occupation);

        assertEquals("Software Engineer", updatedUser.getTargetOccupation().getOccupationLabel());
        verify(occupationRepository).save(occupation);
    }

    @Test
    void updateUserOccupation_userNotFound() {
        UUID userId = UUID.randomUUID();
        String authId = UUID.randomUUID().toString(); // different ID for auth user
        Occupation occupation = new Occupation();
        occupation.setOccupationLabel("Software Engineer");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUserOccupation(authId, userId.toString(), occupation);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateUserOccupation_forbidden() {
        UUID userId = UUID.randomUUID();
        String authId = UUID.randomUUID().toString(); // different ID for auth user
        User user = new User();
        user.setId(userId);
        Occupation occupation = new Occupation();
        occupation.setOccupationLabel("Software Engineer");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUserOccupation(authId, userId.toString(), occupation);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }


    @Test
    void updateUserSkills_success() {
        UUID userId = UUID.randomUUID();
        String authId = userId.toString();
        User user = new User();
        user.setId(userId);
        user.setSkillList(new ArrayList<>());

        // Mocking a skill to add to the user
        Skill skill = new Skill("1", "Java", 5, user);

        // Mock user repository
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Execute the service method
        User updatedUser = userService.updateUserSkills(authId, userId.toString(), skill);

        // Assert that the skill has been added to the user
        assertTrue(updatedUser.getSkillList().contains(skill)); // Assuming `getSkillList()` is available

        // Assert that the skill has been saved in the skill repository
        verify(skillRepository).save(skill);
    }

    @Test
    void updateUserSkills_userNotFound() {
        UUID userId = UUID.randomUUID();
        String authId = UUID.randomUUID().toString(); // different ID for auth user
        Skill skill = new Skill("1", "Java", 5, null); // Skill has no user yet

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Assert that the user was not found
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUserSkills(authId, userId.toString(), skill);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateUserSkills_forbidden() {
        UUID userId = UUID.randomUUID();
        String authId = UUID.randomUUID().toString(); // different ID for auth user
        User user = new User();
        user.setId(userId);
        Skill skill = new Skill("1", "Java", 5, user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Assert that the user is not authorized
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUserSkills(authId, userId.toString(), skill);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

}