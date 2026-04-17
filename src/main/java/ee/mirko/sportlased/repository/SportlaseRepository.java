package ee.mirko.sportlased.repository;

import ee.mirko.sportlased.entity.Sportlane;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SportlaseRepository extends JpaRepository<Sportlane, Long> {

    @Query(
	    value = """
		    select s
		    from Sportlane s
		    left join s.tulemused t
		    where (:riik is null or lower(s.riik) = lower(:riik))
		    group by s
		    order by
			case when :sortDirection = 'asc' then coalesce(sum(t), 0) end asc,
			case when :sortDirection = 'desc' then coalesce(sum(t), 0) end desc,
			s.id asc
		    """,
	    countQuery = """
		    select count(s)
		    from Sportlane s
		    where (:riik is null or lower(s.riik) = lower(:riik))
		    """
    )
    Page<Sportlane> leiaLehekulg(
	    @Param("riik") String riik,
	    @Param("sortDirection") String sortDirection,
	    Pageable pageable
    );
}