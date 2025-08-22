package gr.uom.user_management.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uom.user_management.controllers.dto.ResetPasswordRequest;
import gr.uom.user_management.models.Occupation;
import gr.uom.user_management.models.Skill;
import gr.uom.user_management.models.SystemConfiguration;
import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.UserRepository;
import gr.uom.user_management.services.SystemConfigurationService;
import gr.uom.user_management.services.UserService;
import gr.uom.user_management.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    SystemConfigurationService systemConfigurationService;

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
    User updateUser(HttpServletRequest request, @PathVariable String id, @RequestParam(required = false) String country,
                    @RequestParam(required = false) String streetAddress, @RequestParam(required = false) String portfolio) {
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return userService.updateUser(decodedJWT.getClaim("id").asString(), id, country, streetAddress, portfolio);
    }

    @PutMapping("/{id}/skills")
    User updateUserSkills(HttpServletRequest request, @PathVariable String id, @RequestBody Skill skill) {
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return userService.updateUserSkills(decodedJWT.getClaim("id").asString(), id, skill);
    }

    @PutMapping("/{id}/skills/skill")
    List<Skill> updateUserSkillYears(HttpServletRequest request, @PathVariable String id, @RequestParam String skillId, @RequestParam Integer years) {
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return userService.updateUserSkillYears(decodedJWT.getClaim("id").asString(), id, skillId, years);
    }

    @PutMapping("/{id}/occupation")
    User updateUserOccupation(HttpServletRequest request, @PathVariable String id, @RequestBody Occupation occupation) {
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return userService.updateUserOccupation(decodedJWT.getClaim("id").asString(), id, occupation);
    }

    @GetMapping("/{id}/skills")
    List<Skill> getUserSkills(HttpServletRequest request, @PathVariable String id){
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return userService.getUserSkills(decodedJWT.getClaim("id").asString(), id);
    }

    @DeleteMapping("/{id}/skills")
    List<Skill> deleteUserSkill(HttpServletRequest request, @PathVariable String id, @RequestParam String skillId){
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return userService.deleteUserSkill(decodedJWT.getClaim("id").asString(), id, skillId);
    }

    @GetMapping("/{id}/configurations")
    SystemConfiguration getUserSystemConfigurations(HttpServletRequest request, @PathVariable String id){
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return systemConfigurationService.getUserSystemConfigurations(decodedJWT.getClaim("id").asString(), id);
    }

    @PutMapping("/{id}/configurations")
    SystemConfiguration updateUserSystemConfigurations(HttpServletRequest request, @PathVariable String id,
                                                       @RequestParam(required = false) String filterDemandDataSources,
                                                       @RequestParam(required = false) Integer filterDemandDataLimit,
                                                       @RequestParam(required = false) String filterDemandOccupations,
                                                       @RequestParam(required = false) String filterSupplyProfilesDataSources,
                                                       @RequestParam(required = false) Integer filterSupplyProfilesDataLimit,
                                                       @RequestParam(required = false) String filterSupplyCoursesDataSources,
                                                       @RequestParam(required = false) Integer filterSupplyCoursesDataLimit){
        DecodedJWT decodedJWT= TokenUtil.getDecodedJWTfromToken(request.getHeader(AUTHORIZATION));
        return systemConfigurationService.updateUserSystemConfigurations(decodedJWT.getClaim("id").asString(), id,
                filterDemandDataSources, filterDemandDataLimit, filterDemandOccupations, filterSupplyProfilesDataSources,
                filterSupplyProfilesDataLimit, filterSupplyCoursesDataSources, filterSupplyCoursesDataLimit);
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

    @PostMapping("/reset-password/request")
    public ResponseEntity<?> resetPassword(@RequestParam String userEmail){
        userService.resetPasswordRequest(userEmail);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest){
        userService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok().build();
    }
}
