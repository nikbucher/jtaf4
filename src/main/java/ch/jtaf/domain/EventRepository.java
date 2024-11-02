package ch.jtaf.domain;

import ch.jtaf.db.tables.Event;
import ch.jtaf.db.tables.records.EventRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static ch.jtaf.db.tables.Event.EVENT;

@Repository
public class EventRepository extends JooqRepository<Event, EventRecord, Long> {

    public EventRepository(DSLContext dslContext) {
        super(dslContext, EVENT);
    }
}
