package gr.uom.user_management.repositories;

import gr.uom.user_management.models.CvAnalysisJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CvAnalysisJobRepository extends JpaRepository<CvAnalysisJob, UUID> {
}
