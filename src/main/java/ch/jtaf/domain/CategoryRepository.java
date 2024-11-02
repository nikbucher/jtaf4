package ch.jtaf.domain;

import ch.jtaf.db.tables.Category;
import ch.jtaf.db.tables.records.CategoryRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static ch.jtaf.db.tables.Category.CATEGORY;

@Repository
public class CategoryRepository extends JooqRepository<Category, CategoryRecord, Long> {

    public CategoryRepository(DSLContext dslContext) {
        super(dslContext, CATEGORY);
    }
}
