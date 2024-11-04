package ch.jtaf.domain;

import ch.jtaf.db.tables.Competition;
import ch.jtaf.db.tables.records.CompetitionRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    public Optional<Record2<String, String>> findProjectionById(long competitionId) {
        return dslContext
            .select(COMPETITION.series().NAME, COMPETITION.NAME)
            .from(COMPETITION)
            .where(COMPETITION.ID.eq(competitionId))
            .fetchOptional();
    }
}
