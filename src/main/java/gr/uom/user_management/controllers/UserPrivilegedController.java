package gr.uom.user_management.controllers;

import gr.uom.user_management.models.Organization;
import gr.uom.user_management.models.User;
import gr.uom.user_management.services.UserPrivilegedService;
import gr.uom.user_management.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin")
public class UserPrivilegedController {

    @Autowired
    UserPrivilegedService userPrivilegedService;

    @Autowired
    UserService userService;

    @PutMapping("/users/authorize")
    User givePrivilegeToUser(@RequestParam String email){
        return userPrivilegedService.givePrivilegeToUser(email);
    }

    @DeleteMapping("/users/delete")
    void deleteUser(@RequestParam String email) {
        userPrivilegedService.deleteUser(email);
    }

    @GetMapping("/users/all")
    List<User> getAllUser(){
        return userService.getAllUsers();
    }

    @PostMapping("/users/create")
    User createUser(@RequestBody User user, @RequestParam String installation, @RequestParam String organization){
        return userService.createUser(user, installation, organization);
    }

    @PutMapping("/users/organization")
    User changeUserOrganization(@RequestParam String email, @RequestParam String name){
        return userPrivilegedService.changeUserOrganization(email, name);
    }

    @PostMapping("/organization")
    Organization createOrganization(@RequestParam String name){
        return userPrivilegedService.createOrganization(name);
    }

    @GetMapping("/organization")
    List<Organization> getAllOrganizations(){
        return userPrivilegedService.getAllOrganizations();
    }
}
