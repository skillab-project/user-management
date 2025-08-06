package gr.uom.user_management.models;

import javax.persistence.*;

@Entity
public class ClusteringAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int numberOfClusters;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String clusteringResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private Analysis analysis;

    public ClusteringAnalysis() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumberOfClusters() {
        return numberOfClusters;
    }

    public void setNumberOfClusters(int numberOfClusters) {
        this.numberOfClusters = numberOfClusters;
    }

    public String getClusteringResult() {
        return clusteringResult;
    }

    public void setClusteringResult(String clusteringResult) {
        this.clusteringResult = clusteringResult;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }
}
