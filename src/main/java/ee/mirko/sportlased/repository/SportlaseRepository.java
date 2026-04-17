package ee.mirko.sportlased.repository;

import ee.mirko.sportlased.entity.Sportlane;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SportlaseRepository extends JpaRepository<Sportlane, Long> {

    @Query(
        nativeQuery = true,
        value = """
            SELECT s.* FROM sportlane s
            LEFT JOIN sportlane_tulemused st ON st.sportlane_id = s.id
            WHERE (:riik IS NULL OR LOWER(s.riik) = LOWER(:riik))
            GROUP BY s.id
            ORDER BY COALESCE(SUM(st.tulemus), 0) ASC, s.id ASC
            """,
        countQuery = """
            SELECT COUNT(*) FROM sportlane s
            WHERE (:riik IS NULL OR LOWER(s.riik) = LOWER(:riik))
            """
    )
    Page<Sportlane> leiaLehekuljAsc(@Param("riik") String riik, Pageable pageable);

    @Query(
        nativeQuery = true,
        value = """
            SELECT s.* FROM sportlane s
            LEFT JOIN sportlane_tulemused st ON st.sportlane_id = s.id
            WHERE (:riik IS NULL OR LOWER(s.riik) = LOWER(:riik))
            GROUP BY s.id
            ORDER BY COALESCE(SUM(st.tulemus), 0) DESC, s.id ASC
            """,
        countQuery = """
            SELECT COUNT(*) FROM sportlane s
            WHERE (:riik IS NULL OR LOWER(s.riik) = LOWER(:riik))
            """
    )
    Page<Sportlane> leiaLehekuljDesc(@Param("riik") String riik, Pageable pageable);
}