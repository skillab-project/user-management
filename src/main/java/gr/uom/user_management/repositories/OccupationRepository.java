package gr.uom.user_management.repositories;

import gr.uom.user_management.models.Occupation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OccupationRepository extends JpaRepository<Occupation, Long> {
}
