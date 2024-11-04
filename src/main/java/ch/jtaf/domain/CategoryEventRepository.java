package ch.jtaf.domain;

import ch.jtaf.db.tables.CategoryEvent;
import ch.jtaf.db.tables.records.CategoryEventRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static ch.jtaf.db.tables.CategoryEvent.CATEGORY_EVENT;
import static ch.jtaf.db.tables.Event.EVENT;

@Repository
public class CategoryEventRepository extends JooqRepository<CategoryEvent, CategoryEventRecord, CategoryEventId> {

    public CategoryEventRepository(DSLContext dslContext) {
        super(dslContext,
            CategoryEvent.CATEGORY_EVENT);
    }

    public List<CategoryEventVO> findAllByCategoryId(long categoryId) {
        return dslContext
            .select(EVENT.ABBREVIATION, EVENT.NAME, EVENT.GENDER, EVENT.EVENT_TYPE, EVENT.A, EVENT.B, EVENT.C, CATEGORY_EVENT.POSITION,
                CATEGORY_EVENT.CATEGORY_ID, CATEGORY_EVENT.EVENT_ID)
            .from(CATEGORY_EVENT)
            .join(EVENT).on(EVENT.ID.eq(CATEGORY_EVENT.EVENT_ID))
            .where(CATEGORY_EVENT.CATEGORY_ID.eq(categoryId))
            .orderBy(CATEGORY_EVENT.POSITION)
            .fetchInto(CategoryEventVO.class);
    }
}
