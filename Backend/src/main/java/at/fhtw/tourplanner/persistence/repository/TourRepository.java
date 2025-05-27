package at.fhtw.tourplanner.persistence.repository;

import at.fhtw.tourplanner.persistence.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
}
