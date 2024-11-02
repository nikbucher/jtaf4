package ch.jtaf.domain;

import ch.jtaf.db.tables.Club;
import ch.jtaf.db.tables.records.ClubRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static ch.jtaf.db.tables.Club.CLUB;

@Repository
public class ClubRepository extends JooqRepository<Club, ClubRecord, Long> {

    public ClubRepository(DSLContext dslContext) {
        super(dslContext, CLUB);
    }
}
