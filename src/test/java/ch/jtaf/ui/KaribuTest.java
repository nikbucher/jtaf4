package ch.jtaf.ui;

import ch.jtaf.db.tables.records.OrganizationRecord;
import ch.jtaf.db.tables.records.SeriesRecord;
import ch.jtaf.ui.view.OrganizationsView;
import com.github.mvysny.kaributesting.mockhttp.MockRequest;
import com.github.mvysny.kaributesting.v10.GridKt;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.SpringServlet;
import kotlin.jvm.functions.Function0;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class KaribuTest {

    private static Routes routes;

    @Autowired
    protected ApplicationContext ctx;

    @BeforeAll
    public static void discoverRoutes() {
        Locale.setDefault(Locale.ENGLISH);
        routes = new Routes().autoDiscoverViews("ch.jtaf.ui");
    }

    @BeforeEach
    public void setup() {
        final Function0<UI> uiFactory = UI::new;
        final SpringServlet servlet = new MockSpringServlet(routes, ctx, uiFactory);
        MockVaadin.setup(uiFactory, servlet);
    }

    @AfterEach
    public void tearDown() {
        logout();
        MockVaadin.tearDown();
    }

    protected void login(String user, String pass, final List<String> roles) {
        // taken from https://www.baeldung.com/manually-set-user-authentication-spring-security
        // also see https://github.com/mvysny/karibu-testing/issues/47 for more details.
        final List<SimpleGrantedAuthority> authorities =
            roles.stream().map(it -> new SimpleGrantedAuthority("ROLE_" + it)).collect(Collectors.toList());
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(user, pass, authorities);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(authReq);

        // however, you also need to make sure that ViewAccessChecker works properly;
        // that requires a correct MockRequest.userPrincipal and MockRequest.isUserInRole()
        final MockRequest request = (MockRequest) VaadinServletRequest.getCurrent().getRequest();
        request.setUserPrincipalInt(authReq);
        request.setUserInRole((principal, role) -> roles.contains(role));
    }

    protected void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
        if (VaadinServletRequest.getCurrent() != null) {
            final MockRequest request = (MockRequest) VaadinServletRequest.getCurrent().getRequest();
            request.setUserPrincipalInt(null);
            request.setUserInRole((principal, role) -> false);
        }
    }

    protected Grid<SeriesRecord> navigateToSeriesList() {
        UI.getCurrent().navigate(OrganizationsView.class);

        H1 h1 = _get(H1.class, spec -> spec.withId("view-title"));
        assertThat(h1.getText()).isEqualTo("Organizations");

        Grid<OrganizationRecord> organizationsGrid = _get(Grid.class, spec -> spec.withId("organizations-grid"));
        assertThat(GridKt._size(organizationsGrid)).isEqualTo(2);

        HorizontalLayout edit = (HorizontalLayout) GridKt._getCellComponent(organizationsGrid, 0, "Edit");
        Button select = (Button) edit.getChildren().filter(component -> component instanceof Button).findFirst().get();
        select.click();

        h1 = _get(H1.class, spec -> spec.withId("view-title"));
        assertThat(h1.getText()).isEqualTo("Series");

        Grid<SeriesRecord> seriesGrid = _get(Grid.class, spec -> spec.withId("series-grid"));
        assertThat(GridKt._size(seriesGrid)).isEqualTo(2);

        SeriesRecord seriesRecord = GridKt._get(seriesGrid, 0);
        assertThat(seriesRecord.getName()).isEqualTo("CIS 2018");

        return seriesGrid;
    }
}