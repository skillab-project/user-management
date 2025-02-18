package gr.uom.user_management.models;

import javax.persistence.*;
import java.util.List;

@Entity
public class SystemConfiguration {
    @Id
    @GeneratedValue
    private Long id;
    @ElementCollection
    @CollectionTable(name = "lis_job_sources", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "list")
    private List<String> listJobSources;
    @ElementCollection
    @CollectionTable(name = "lis_profile_sources", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "list")
    private List<String> listProfileSources;
    @OneToOne
    private User user;

    public SystemConfiguration(List<String> listJobSources, List<String> listProfileSources, User user) {
        this.listJobSources = listJobSources;
        this.listProfileSources = listProfileSources;
        this.user = user;
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
}
