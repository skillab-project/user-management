package gr.uom.user_management.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class SystemConfiguration {
    @Id
    @GeneratedValue
    private Long id;
    private String filterDemandDataSources = "OJA";
    private Integer filterDemandDataLimit = 30000;
    private String filterDemandOccupations = "";
    private String filterSupplyProfilesDataSources = "";
    private Integer filterSupplyProfilesDataLimit = 30000;
    private String filterSupplyCoursesDataSources = "";
    private Integer filterSupplyCoursesDataLimit = 30000;
    @ElementCollection
    @CollectionTable(name = "lis_job_sources", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "list")
    private List<String> listJobSources = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "lis_profile_sources", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "list")
    private List<String> listProfileSources = new ArrayList<>();
    @OneToOne
    @JsonIgnore
    private User user;

    public SystemConfiguration() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getListJobSources() {
        return listJobSources;
    }

    public void setListJobSources(List<String> listJobSources) {
        this.listJobSources = listJobSources;
    }

    public List<String> getListProfileSources() {
        return listProfileSources;
    }

    public void setListProfileSources(List<String> listProfileSources) {
        this.listProfileSources = listProfileSources;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFilterDemandDataSources() {
        return filterDemandDataSources;
    }

    public void setFilterDemandDataSources(String filterDemandDataSources) {
        this.filterDemandDataSources = filterDemandDataSources;
    }

    public Integer getFilterDemandDataLimit() {
        return filterDemandDataLimit;
    }

    public void setFilterDemandDataLimit(Integer filterDemandDataLimit) {
        this.filterDemandDataLimit = filterDemandDataLimit;
    }

    public String getFilterDemandOccupations() {
        return filterDemandOccupations;
    }

    public void setFilterDemandOccupations(String filterDemandOccupations) {
        this.filterDemandOccupations = filterDemandOccupations;
    }

    public String getFilterSupplyProfilesDataSources() {
        return filterSupplyProfilesDataSources;
    }

    public void setFilterSupplyProfilesDataSources(String filterSupplyProfilesDataSources) {
        this.filterSupplyProfilesDataSources = filterSupplyProfilesDataSources;
    }

    public Integer getFilterSupplyProfilesDataLimit() {
        return filterSupplyProfilesDataLimit;
    }

    public void setFilterSupplyProfilesDataLimit(Integer filterSupplyProfilesDataLimit) {
        this.filterSupplyProfilesDataLimit = filterSupplyProfilesDataLimit;
    }

    public String getFilterSupplyCoursesDataSources() {
        return filterSupplyCoursesDataSources;
    }

    public void setFilterSupplyCoursesDataSources(String filterSupplyCoursesDataSources) {
        this.filterSupplyCoursesDataSources = filterSupplyCoursesDataSources;
    }

    public Integer getFilterSupplyCoursesDataLimit() {
        return filterSupplyCoursesDataLimit;
    }

    public void setFilterSupplyCoursesDataLimit(Integer filterSupplyCoursesDataLimit) {
        this.filterSupplyCoursesDataLimit = filterSupplyCoursesDataLimit;
    }
}
