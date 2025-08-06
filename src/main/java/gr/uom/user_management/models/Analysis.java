package gr.uom.user_management.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Analysis {
    @Id
    @GeneratedValue
    private UUID id;
    private String userId;
    private Boolean finished;
    private String completeSessionId;
    private String sessionId;
    private String filterOccupation;
    private String filterMinDate;
    private String filterMaxDate;
    private String filterSources;
    private Integer limitData;
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String firstDescriptiveAnalysis;
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String secondDescriptiveAnalysis;
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String exploratoryAnalysis;
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String trendAnalysis;
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ClusteringAnalysis> clusteringAnalysis = new ArrayList<>();


    public Analysis() {
    }

    public Analysis(String userId, String sessionId, Boolean finished, String filterOccupation, String filterMinDate, String filterMaxDate, String filterSources, Integer limitData) {
        this.userId = userId;
        if(sessionId.equals("profiles") || sessionId.equals("courses")){
            this.completeSessionId = sessionId + "-sources-" + filterSources + "-limit-" + limitData;
        }
        else {
            this.completeSessionId = sessionId + "-occupation-" + filterOccupation.replaceAll("[^a-zA-Z0-9.-]", "_")
                    + "-minDate-" + filterMinDate + "-maxDate-" + filterMaxDate + "-sources-" + filterSources  + "-limit-" + limitData;
        }
        System.out.println("completeSessionId: " + completeSessionId);
        this.sessionId = sessionId;
        this.finished = finished;
        this.filterOccupation = filterOccupation;
        this.filterMinDate = filterMinDate;
        this.filterMaxDate = filterMaxDate;
        this.filterSources = filterSources;
        this.limitData = limitData;
    }

    public Analysis(UUID id, String userId, String completeSessionId, String sessionId, Boolean finished, String filterOccupation, String filterMinDate, String filterMaxDate, String filterSources, Integer limitData) {
        this.id = id;
        this.userId = userId;
        this.completeSessionId = completeSessionId;
        this.sessionId = sessionId;
        this.finished = finished;
        this.filterOccupation = filterOccupation;
        this.filterMinDate = filterMinDate;
        this.filterMaxDate = filterMaxDate;
        this.filterSources = filterSources;
        this.limitData = limitData;
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

    public String getCompleteSessionId() {
        return completeSessionId;
    }

    public void setCompleteSessionId(String completeSessionId) {
        this.completeSessionId = completeSessionId;
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

    public Integer getLimitData() {
        return limitData;
    }

    public void setLimitData(Integer limitData) {
        this.limitData = limitData;
    }

    public String getFirstDescriptiveAnalysis() {
        return firstDescriptiveAnalysis;
    }

    public void setFirstDescriptiveAnalysis(String firstDescriptiveAnalysis) {
        this.firstDescriptiveAnalysis = firstDescriptiveAnalysis;
    }

    public String getSecondDescriptiveAnalysis() {
        return secondDescriptiveAnalysis;
    }

    public void setSecondDescriptiveAnalysis(String secondDescriptiveAnalysis) {
        this.secondDescriptiveAnalysis = secondDescriptiveAnalysis;
    }

    public String getExploratoryAnalysis() {
        return exploratoryAnalysis;
    }

    public void setExploratoryAnalysis(String exploratoryAnalysis) {
        this.exploratoryAnalysis = exploratoryAnalysis;
    }

    public String getTrendAnalysis() {
        return trendAnalysis;
    }

    public void setTrendAnalysis(String trendAnalysis) {
        this.trendAnalysis = trendAnalysis;
    }

    public List<ClusteringAnalysis> getClusteringAnalysis() {
        return clusteringAnalysis;
    }

    public void setClusteringAnalysis(List<ClusteringAnalysis> clusteringAnalysis) {
        this.clusteringAnalysis = clusteringAnalysis;
    }
}
