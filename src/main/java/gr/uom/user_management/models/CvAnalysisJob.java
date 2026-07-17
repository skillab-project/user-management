package gr.uom.user_management.models;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class CvAnalysisJob {

    @Id
    @GeneratedValue
    private UUID id;

    private String userId;

    // PENDING | DONE | FAILED
    private String status;

    @Column(columnDefinition = "TEXT")
    private String skillsJson;

    @Column(columnDefinition = "TEXT")
    private String error;

    public CvAnalysisJob() {
    }

    public CvAnalysisJob(String userId) {
        this.userId = userId;
        this.status = "PENDING";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSkillsJson() {
        return skillsJson;
    }

    public void setSkillsJson(String skillsJson) {
        this.skillsJson = skillsJson;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
