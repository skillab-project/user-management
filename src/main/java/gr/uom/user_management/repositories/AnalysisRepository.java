package gr.uom.user_management.repositories;

import gr.uom.user_management.models.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    Optional<Analysis> findBySessionIdAndFilterOccupationAndFilterSourcesAndLimitData(
            String sessionId,
            String filterOccupation,
            String filterSources,
            Integer limitData
    );
}
