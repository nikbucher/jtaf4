package ch.jtaf.domain;

import ch.jtaf.db.tables.Athlete;
import ch.jtaf.db.tables.records.AthleteRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static ch.jtaf.db.tables.Athlete.ATHLETE;

@Repository
public class AthleteRepository extends JooqRepository<Athlete, AthleteRecord, Long> {

    public AthleteRepository(DSLContext dslContext) {
        super(dslContext, ATHLETE);
    }
}
