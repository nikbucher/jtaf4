package ch.jtaf.domain;

import ch.jtaf.db.tables.Organization;
import ch.jtaf.db.tables.records.OrganizationRecord;
import ch.martinelli.oss.jooqspring.JooqRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static ch.jtaf.db.tables.Organization.ORGANIZATION;

@Repository
public class OrganizationRepository extends JooqRepository<Organization, OrganizationRecord, Long> {

    public OrganizationRepository(DSLContext dslContext) {
        super(dslContext, ORGANIZATION);
    }
}
