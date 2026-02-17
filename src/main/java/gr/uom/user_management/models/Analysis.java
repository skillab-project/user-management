package gr.uom.user_management.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Analysis {
    @Id
    @GeneratedValue
    private UUID id;
    private Boolean finished;
    private String sessionId;
    private String filterOccupation;
    private String filterSources;
    private Integer limitData;
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String descriptiveResult;
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String descriptiveLocationResult;
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String exploratoryResult;
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String trendResult;


    public Analysis() {
    }

    public Analysis(String sessionId, Boolean finished, String filterOccupation, String filterSources, Integer limitData) {
        this.sessionId = sessionId;
        this.finished = finished;
        this.filterOccupation = filterOccupation;
        this.filterSources = filterSources;
        this.limitData = limitData;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getFilterSources() {
        return filterSources;
    }

    public void setFilterSources(String filterSources) {
        this.filterSources = filterSources;
    }

    public Integer getLimitData() {
        return limitData;
    }

    public void setLimitData(Integer limitData) {
        this.limitData = limitData;
    }

    public String getDescriptiveResult() {
        return descriptiveResult;
    }

    public void setDescriptiveResult(String descriptiveResult) {
        this.descriptiveResult = descriptiveResult;
    }

    public String getDescriptiveLocationResult() {
        return descriptiveLocationResult;
    }

    public void setDescriptiveLocationResult(String descriptiveLocationResult) {
        this.descriptiveLocationResult = descriptiveLocationResult;
    }

    public String getExploratoryResult() {
        return exploratoryResult;
    }

    public void setExploratoryResult(String exploratoryResult) {
        this.exploratoryResult = exploratoryResult;
    }

    public String getTrendResult() {
        return trendResult;
    }

    public void setTrendResult(String trendResult) {
        this.trendResult = trendResult;
    }
}
