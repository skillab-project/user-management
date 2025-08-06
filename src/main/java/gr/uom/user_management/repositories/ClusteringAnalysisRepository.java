package gr.uom.user_management.repositories;

import gr.uom.user_management.models.ClusteringAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusteringAnalysisRepository extends JpaRepository<ClusteringAnalysis, Long> {
}
