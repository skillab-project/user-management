package gr.uom.user_management.services;

import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserPrivilegedServiceTest {

    private UserPrivilegedService userPrivilegedService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);

        userPrivilegedService = new UserPrivilegedService();
        userPrivilegedService.userRepository = userRepository;
    }

    @Test
    void givePrivilegeToUser_success() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setRoles("SIMPLE");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        User updatedUser = userPrivilegedService.givePrivilegeToUser("user@example.com");

        assertEquals("SIMPLE,PRIVILEGED", updatedUser.getRoles());
    }

    @Test
    void givePrivilegeToUser_alreadyPrivileged() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setRoles("SIMPLE,PRIVILEGED");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userPrivilegedService.givePrivilegeToUser("user@example.com");
        });

        assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getStatus());
    }

    @Test
    void deleteUser_success() {
        User user = new User();
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        userPrivilegedService.deleteUser("user@example.com");

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userPrivilegedService.deleteUser("notfound@example.com");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
