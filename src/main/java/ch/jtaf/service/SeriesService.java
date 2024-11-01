package ch.jtaf.service;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ch.jtaf.db.tables.Category.CATEGORY;
import static ch.jtaf.db.tables.CategoryEvent.CATEGORY_EVENT;
import static ch.jtaf.db.tables.Series.SERIES;

@Service
public class SeriesService {

    private final DSLContext dslContext;

    public SeriesService(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Transactional
    public void copyCategories(Long seriesIdToCopy, Long currentSeriesId) {
        dslContext.selectFrom(CATEGORY)
            .where(CATEGORY.SERIES_ID.eq(seriesIdToCopy))
            .fetch()
            .forEach(category -> {
                var copyCategory = category.copy();
                copyCategory.setSeriesId(currentSeriesId);
                dslContext.attach(copyCategory);
                copyCategory.store();
                dslContext.selectFrom(CATEGORY_EVENT)
                    .where(CATEGORY_EVENT.CATEGORY_ID.eq(category.getId()))
                    .fetch()
                    .forEach(categoryEvent -> {
                        var copyCategoryEvent = categoryEvent.copy();
                        copyCategoryEvent.setCategoryId(copyCategory.getId());
                        copyCategoryEvent.setEventId(categoryEvent.getEventId());
                        dslContext.attach(copyCategoryEvent);
                        copyCategoryEvent.store();
                    });
            });
    }

    @Transactional
    public void deleteSeries(long seriesId) {
        dslContext.deleteFrom(CATEGORY_EVENT)
            .where(CATEGORY_EVENT.category().SERIES_ID.eq(seriesId))
            .execute();

        dslContext.deleteFrom(CATEGORY)
            .where(CATEGORY.SERIES_ID.eq(seriesId))
            .execute();

        dslContext.deleteFrom(SERIES)
            .where(SERIES.ID.eq(seriesId))
            .execute();
    }
}
