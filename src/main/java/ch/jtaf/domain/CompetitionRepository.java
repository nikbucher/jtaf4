package ch.jtaf.domain;

import ch.jtaf.db.tables.Competition;
import ch.jtaf.db.tables.records.CompetitionRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static ch.jtaf.db.tables.Competition.COMPETITION;

@Repository
public class CompetitionRepository extends JooqRepository<Competition, CompetitionRecord, Long> {

    public CompetitionRepository(DSLContext dslContext) {
        super(dslContext, COMPETITION);
    }

    public List<CompetitionRecord> findBySeriesId(Long seriesId) {
        return dslContext
            .selectFrom(COMPETITION)
            .where(COMPETITION.SERIES_ID.eq(seriesId))
            .orderBy(COMPETITION.COMPETITION_DATE)
            .fetch();
    }
}
