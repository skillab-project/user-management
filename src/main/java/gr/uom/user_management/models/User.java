package gr.uom.user_management.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String email;
    private String password;
    private String roles;
    private String streetAddress;
    private String portfolio;
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Skill> skillList;
    private String targetOccupation;
    @OneToOne(mappedBy = "user")
    private SystemConfiguration configurations;

    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public void addSkill(Skill skill){
        skillList.add(skill);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }


    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public List<Skill> getSkillList() {
        return skillList;
    }

    public void setSkillList(List<Skill> skillList) {
        this.skillList = skillList;
    }

    public String getTargetOccupation() {
        return targetOccupation;
    }

    public void setTargetOccupation(String targetOccupation) {
        this.targetOccupation = targetOccupation;
    }
}
