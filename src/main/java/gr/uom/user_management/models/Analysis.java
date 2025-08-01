package gr.uom.user_management.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class Analysis {
    @Id
    @GeneratedValue
    private UUID id;
    private String userId;
    private Boolean finished;
    private String sessionId;
    private String filterOccupation;
    private String filterMinDate;
    private String filterMaxDate;
    private String filterSources;

    public Analysis() {
    }

    public Analysis(String userId, String sessionId, Boolean finished, String filterOccupation, String filterMinDate, String filterMaxDate, String filterSources) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.finished = finished;
        this.filterOccupation = filterOccupation;
        this.filterMinDate = filterMinDate;
        this.filterMaxDate = filterMaxDate;
        this.filterSources = filterSources;
    }

    public Analysis(UUID id, String userId, String sessionId, Boolean finished, String filterOccupation, String filterMinDate, String filterMaxDate, String filterSources) {
        this.id = id;
        this.userId = userId;
        this.sessionId = sessionId;
        this.finished = finished;
        this.filterOccupation = filterOccupation;
        this.filterMinDate = filterMinDate;
        this.filterMaxDate = filterMaxDate;
        this.filterSources = filterSources;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public String getFilterOccupation() {
        return filterOccupation;
    }

    public void setFilterOccupation(String filterOccupation) {
        this.filterOccupation = filterOccupation;
    }

    public String getFilterMinDate() {
        return filterMinDate;
    }

    public void setFilterMinDate(String filterMinDate) {
        this.filterMinDate = filterMinDate;
    }

    public String getFilterMaxDate() {
        return filterMaxDate;
    }

    public void setFilterMaxDate(String filterMaxDate) {
        this.filterMaxDate = filterMaxDate;
    }

    public String getFilterSources() {
        return filterSources;
    }

    public void setFilterSources(String filterSources) {
        this.filterSources = filterSources;
    }
}
