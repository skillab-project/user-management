package gr.uom.user_management.controllers;

import gr.uom.user_management.models.User;
import gr.uom.user_management.services.UserPrivilegedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
public class UserPrivilegedController {

    @Autowired
    UserPrivilegedService userPrivilegedService;

    @PutMapping("/authorize")
    User givePrivilegeToUser(@RequestParam String email){
        return userPrivilegedService.givePrivilegeToUser(email);
    }

    @DeleteMapping("delete/user")
    void deleteUser(@RequestParam String email) {
        userPrivilegedService.deleteUser(email);
    }

}
