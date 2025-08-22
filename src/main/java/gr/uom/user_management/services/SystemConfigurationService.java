package gr.uom.user_management.services;

import gr.uom.user_management.models.Skill;
import gr.uom.user_management.models.SystemConfiguration;
import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.SystemConfigurationRepository;
import gr.uom.user_management.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class SystemConfigurationService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    SystemConfigurationRepository systemConfigurationRepository;

    public SystemConfiguration getUserSystemConfigurations(String auth_id, String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with email " + id + " doesn't exist!"
                ));
        if(!user.getId().equals(UUID.fromString(auth_id))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return user.getConfigurations();
    }

    public SystemConfiguration updateUserSystemConfigurations(String auth_id, String id, String filterDemandDataSources,
                               Integer filterDemandDataLimit, String filterDemandOccupations, String filterSupplyProfilesDataSources,
                               Integer filterSupplyProfilesDataLimit, String filterSupplyCoursesDataSources, Integer filterSupplyCoursesDataLimit) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with email " + id + " doesn't exist!"
                ));
        if(!user.getId().equals(UUID.fromString(auth_id))){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        SystemConfiguration config = user.getConfigurations();

        // Update only if value is provided
        if (filterDemandDataSources != null && !filterDemandDataSources.trim().isEmpty()) {
            config.setFilterDemandDataSources(filterDemandDataSources);
        }
        if (filterDemandDataLimit != null) {
            config.setFilterDemandDataLimit(filterDemandDataLimit);
        }
        if (filterDemandOccupations != null && !filterDemandOccupations.trim().isEmpty()) {
            config.setFilterDemandOccupations(filterDemandOccupations);
        }
        if (filterSupplyProfilesDataSources != null && !filterSupplyProfilesDataSources.trim().isEmpty()) {
            config.setFilterSupplyProfilesDataSources(filterSupplyProfilesDataSources);
        }
        if (filterSupplyProfilesDataLimit != null) {
            config.setFilterSupplyProfilesDataLimit(filterSupplyProfilesDataLimit);
        }
        if (filterSupplyCoursesDataSources != null && !filterSupplyCoursesDataSources.trim().isEmpty()) {
            config.setFilterSupplyCoursesDataSources(filterSupplyCoursesDataSources);
        }
        if (filterSupplyCoursesDataLimit != null) {
            config.setFilterSupplyCoursesDataLimit(filterSupplyCoursesDataLimit);
        }

        systemConfigurationRepository.save(config);
        return config;
    }

}
