package gr.uom.user_management.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uom.user_management.models.Skill;
import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.UserRepository;
import gr.uom.user_management.services.UserService;
import gr.uom.user_management.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @PostMapping
    User createUser(@RequestBody User user){
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    User getUser(@PathVariable String id){
        return userService.getUser(id);
    }

    @PutMapping("/{id}")
    User updateUser(HttpServletRequest request, @PathVariable String id, @RequestParam(required = false) String streetAddress,
                    @RequestParam(required = false) String portfolio, @RequestParam(required = false) String targetOccupation) {
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return userService.updateUser(decodedJWT.getClaim("id").asString(), id, streetAddress, portfolio, targetOccupation);
    }

    @PutMapping("/{id}/skills")
    User updateUserSkills(HttpServletRequest request, @PathVariable String id, @RequestBody Skill skill) {
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return userService.updateUserSkills(decodedJWT.getClaim("id").asString(), id, skill);
    }

    @GetMapping("/{id}/skills")
    List<Skill> getUserSkills(HttpServletRequest request, @PathVariable String id){
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return userService.getUserSkills(decodedJWT.getClaim("id").asString(), id);
    }

    @GetMapping("/all")
    List<User> getAllUser(){
        return userService.getAllUsers();
    }

    @GetMapping("/token/refresh")
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            try {
                DecodedJWT decodedJWT = TokenUtil.getDecodedJWTfromToken(authorizationHeader);
                String email = decodedJWT.getSubject();

                //get user in order to get roles
                Optional<User> user = userRepository.findByEmail(email);

                List<String> roles = stream(user.get().getRoles().split(",")).collect(Collectors.toList());
                String name = user.get().getName();
                String id = user.get().getId().toString();

                //Generate tokens
                String accessToken = TokenUtil.generateAccessToken(email, request.getRequestURL(), id, name, roles);
                String refreshToken = TokenUtil.generateRefreshToken(email, request.getRequestURL());

                Map<String,String> tokens= new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", refreshToken);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            }
            catch (Exception e){
                System.err.println("Error with token: "+e.getMessage());
                response.setHeader("error",e.getMessage());
                response.setStatus(FORBIDDEN.value());
                //response.sendError(FORBIDDEN.value());
                Map<String,String> error= new HashMap<>();
                error.put("error_message", e.getMessage());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        }
        else {
            throw new RuntimeException("Refresh token is missing");
        }
    }
}
