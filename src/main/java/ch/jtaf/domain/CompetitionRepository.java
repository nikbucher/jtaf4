package ch.jtaf.domain;

import ch.jtaf.db.tables.Competition;
import ch.jtaf.db.tables.records.CompetitionRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static ch.jtaf.db.tables.Competition.COMPETITION;

@Repository
public class CompetitionRepository extends JooqRepository<Competition, CompetitionRecord, Long> {

    public CompetitionRepository(DSLContext dslContext) {
        super(dslContext, COMPETITION);
    }
}
