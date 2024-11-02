package ch.jtaf.domain;

import ch.jtaf.db.tables.Athlete;
import ch.jtaf.db.tables.records.AthleteRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static ch.jtaf.db.tables.Athlete.ATHLETE;
import static ch.jtaf.db.tables.Category.CATEGORY;
import static ch.jtaf.db.tables.CategoryAthlete.CATEGORY_ATHLETE;
import static org.jooq.impl.DSL.select;

@Repository
public class AthleteRepository extends JooqRepository<Athlete, AthleteRecord, Long> {

    public AthleteRepository(DSLContext dslContext) {
        super(dslContext, ATHLETE);
    }

    public List<AthleteRecord> findBySeriesId(Long seriesId) {
        return dslContext
            .select(CATEGORY_ATHLETE.athlete().fields())
            .from(CATEGORY_ATHLETE)
            .where(CATEGORY_ATHLETE.category().SERIES_ID.eq(seriesId))
            .orderBy(CATEGORY_ATHLETE.category().ABBREVIATION, CATEGORY_ATHLETE.athlete().LAST_NAME, CATEGORY_ATHLETE.athlete().FIRST_NAME)
            .fetchInto(ATHLETE);
    }

    public List<AthleteRecord> findByOrganizationIdAndSeriesId(long organizationId, long seriesId, Condition condition, int offset, int limit) {
        return dslContext
            .selectFrom(ATHLETE)
            .where(ATHLETE.ORGANIZATION_ID.eq(organizationId))
            .and(ATHLETE.ID.notIn(select(CATEGORY_ATHLETE.ATHLETE_ID)
                .from(CATEGORY_ATHLETE)
                .join(CATEGORY).on(CATEGORY.ID.eq(CATEGORY_ATHLETE.CATEGORY_ID))
                .where(CATEGORY.SERIES_ID.eq(seriesId))
            ))
            .and(condition)
            .orderBy(ATHLETE.LAST_NAME, ATHLETE.FIRST_NAME)
            .offset(offset).limit(limit)
            .fetch();
    }

    public int countByOrganizationIdAndSeriesId(Long organizationId, Long seriesId, Condition condition) {
        return dslContext
            .selectCount()
            .from(ATHLETE)
            .where(ATHLETE.ORGANIZATION_ID.eq(organizationId))
            .and(ATHLETE.ID.notIn(dslContext
                .select(CATEGORY_ATHLETE.ATHLETE_ID)
                .from(CATEGORY_ATHLETE)
                .join(CATEGORY).on(CATEGORY.ID.eq(CATEGORY_ATHLETE.CATEGORY_ID))
                .where(CATEGORY.SERIES_ID.eq(seriesId))
            ))
            .and(condition)
            .fetchOptionalInto(Integer.class).orElse(0);
    }
}
