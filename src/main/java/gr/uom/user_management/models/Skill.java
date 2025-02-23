package gr.uom.user_management.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Skill {
    @Id
    @GeneratedValue
    private Long id;
    private String skillId;
    private String skillLabel;
    private Integer years;
    @ManyToOne
    @JsonIgnore
    private User user;

    public Skill() {
    }

    public Skill(String skillId, String skillLabel, Integer years, User user) {
        this.skillId = skillId;
        this.skillLabel = skillLabel;
        this.years = years;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public String getSkillLabel() {
        return skillLabel;
    }

    public void setSkillLabel(String skillLabel) {
        this.skillLabel = skillLabel;
    }

    public Integer getYears() {
        return years;
    }

    public void setYears(Integer years) {
        this.years = years;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
