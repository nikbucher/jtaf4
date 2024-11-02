package ch.jtaf.domain;

import ch.jtaf.db.tables.Series;
import ch.jtaf.db.tables.records.SeriesRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static ch.jtaf.db.tables.Series.SERIES;

@Repository
public class SeriesRepository extends JooqRepository<Series, SeriesRecord, Long> {

    public SeriesRepository(DSLContext dslContext) {
        super(dslContext, SERIES);
    }
}
