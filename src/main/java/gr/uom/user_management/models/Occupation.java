package gr.uom.user_management.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Occupation {

    @Id
    @GeneratedValue
    private Long id;
    private String occupationId;
    private String occupationLabel;
    @OneToOne
    @JsonIgnore
    private User user;

    public Occupation(String occupationId, String occupationLabel, User user) {
        this.occupationId = occupationId;
        this.occupationLabel = occupationLabel;
        this.user = user;
    }

    public Occupation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(String occupationId) {
        this.occupationId = occupationId;
    }

    public String getOccupationLabel() {
        return occupationLabel;
    }

    public void setOccupationLabel(String occupationLabel) {
        this.occupationLabel = occupationLabel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
