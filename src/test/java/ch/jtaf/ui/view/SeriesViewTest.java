package ch.jtaf.ui.view;

import ch.jtaf.db.tables.records.AthleteRecord;
import ch.jtaf.db.tables.records.CategoryRecord;
import ch.jtaf.db.tables.records.CompetitionRecord;
import ch.jtaf.db.tables.records.EventRecord;
import ch.jtaf.db.tables.records.SeriesRecord;
import ch.jtaf.ui.KaribuTest;
import com.github.mvysny.kaributesting.v10.GridKt;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.github.mvysny.kaributesting.v10.LocatorJ._assert;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.pro.ConfirmDialogKt._fireConfirm;
import static org.assertj.core.api.Assertions.assertThat;

class SeriesViewTest extends KaribuTest {

    @BeforeEach
    public void login() {
        login("simon@martinelli.ch", "", List.of("ADMIN"));
        UI.getCurrent().getPage().reload();

        Grid<SeriesRecord> seriesGrid = navigateToSeriesList();
        GridKt._clickItem(seriesGrid, 0);

        TextField name = _get(TextField.class);
        assertThat(name.getValue()).isEqualTo("CIS 2018");
    }

    @Test
    void add_competition() {
        Grid<CompetitionRecord> competitionsGrid = _get(Grid.class, spec -> spec.withId("competitions-grid"));
        assertThat(GridKt._size(competitionsGrid)).isEqualTo(2);

        assertThat(GridKt._get(competitionsGrid, 0).getName()).isEqualTo("1. CIS Twann");

        _get(Button.class, spec -> spec.withId("add-button")).click();
        _assert(Dialog.class, 1);

        _get(TextField.class, spec -> spec.withCaption("Name").withValue("")).setValue("Test");
        _get(DatePicker.class, spec -> spec.withCaption("Date")).setValue(LocalDate.now());
        _get(Button.class, spec -> spec.withId("edit-save")).click();

        assertThat(GridKt._size(competitionsGrid)).isEqualTo(3);

        assertThat(GridKt._get(competitionsGrid, 2).getName()).isEqualTo("Test");

        GridKt._getCellComponent(competitionsGrid, 2, "edit-column").getChildren()
            .filter(component -> component instanceof Button).findFirst().map(component -> (Button) component)
            .ifPresent(Button::click);

        ConfirmDialog confirmDialog = _get(ConfirmDialog.class);
        assertThat(confirmDialog.isOpened()).isTrue();

        _fireConfirm(confirmDialog);

        assertThat(GridKt._size(competitionsGrid)).isEqualTo(2);
    }

    @Test
    void add_category_and_assign_event() {
        Tabs tabs = _get(Tabs.class);
        Tab categories = _get(Tab.class, spec -> spec.withText("Categories"));
        tabs.setSelectedTab(categories);

        Grid<CategoryRecord> categoriesGrid = _get(Grid.class, spec -> spec.withId("categories-grid"));
        assertThat(GridKt._size(categoriesGrid)).isEqualTo(12);

        assertThat(GridKt._get(categoriesGrid, 0).getAbbreviation()).isEqualTo("A");

        // Create Category
        _get(Button.class, spec -> spec.withId("add-button")).click();
        _assert(Dialog.class, 1);

        _get(TextField.class, spec -> spec.withCaption("Abbreviation").withValue("")).setValue("1");
        _get(TextField.class, spec -> spec.withCaption("Name").withValue("")).setValue("Test");
        _get(Select.class, spec -> spec.withCaption("Gender")).setValue("M");
        _get(TextField.class, spec -> spec.withCaption("Year from")).setValue("1999");
        _get(TextField.class, spec -> spec.withCaption("Year to")).setValue("2000");
        _get(Button.class, spec -> spec.withId("edit-save")).click();

        assertThat(GridKt._size(categoriesGrid)).isEqualTo(13);

        assertThat(GridKt._get(categoriesGrid, 0).getAbbreviation()).isEqualTo("1");

        // Assign Event
        GridKt._clickItem(categoriesGrid, 0);

        _get(Button.class, spec -> spec.withId("add-event")).click();

        Grid<EventRecord> eventsGrid = _get(Grid.class, spec -> spec.withId("events-grid"));

        ((Button) GridKt._getCellComponent(eventsGrid, 0, "assign-column")).click();

        _get(Dialog.class, spec -> spec.withId("search-event-dialog")).close();

        // Remove Event from Category
        Grid<EventRecord> categoryEventsGrid = _get(Grid.class, spec -> spec.withId("category-events-grid"));
        assertThat(GridKt._size(categoryEventsGrid)).isEqualTo(1);

        GridKt._getCellComponent(categoryEventsGrid, 0, "edit-column").getChildren()
            .filter(component -> component instanceof Button).findFirst().map(component -> (Button) component)
            .ifPresent(Button::click);

        ConfirmDialog confirmDialog = _get(ConfirmDialog.class);
        assertThat(confirmDialog.isOpened()).isTrue();

        _fireConfirm(confirmDialog);

        assertThat(GridKt._size(categoryEventsGrid)).isZero();

        // Remove Category
        GridKt._getCellComponent(categoriesGrid, 0, "edit-column").getChildren()
            .filter(component -> component instanceof Button).findFirst().map(component -> (Button) component)
            .ifPresent(Button::click);

        confirmDialog = _get(ConfirmDialog.class);
        assertThat(confirmDialog.isOpened()).isTrue();

        _fireConfirm(confirmDialog);

        assertThat(GridKt._size(categoriesGrid)).isEqualTo(12);
    }

    @Test
    void assign_athelete() {
        Tabs tabs = _get(Tabs.class);
        Tab athletes = _get(Tab.class, spec -> spec.withText("Athletes"));
        tabs.setSelectedTab(athletes);

        Grid<AthleteRecord> athletesGrid = _get(Grid.class, spec -> spec.withId("athletes-grid"));
        assertThat(GridKt._size(athletesGrid)).isEqualTo(85);

        assertThat(GridKt._get(athletesGrid, 0).getLastName()).isEqualTo("Scholer");

        Button assignAthlete = _get(Button.class, spec -> spec.withId("assign-athlete"));
        assignAthlete.click();

        _assert(Dialog.class, 1);

        _get(TextField.class, spec -> spec.withCaption("Filter").withValue("")).setValue("z");

        Grid<AthleteRecord> searchAthletesGrid = _get(Grid.class, spec -> spec.withId("search-athletes-grid"));
        assertThat(GridKt._size(searchAthletesGrid)).isEqualTo(1);

        assertThat(GridKt._get(searchAthletesGrid, 0).getLastName()).isEqualTo("Zumstein");

        GridKt._getCellComponent(searchAthletesGrid, 0, "edit-column").getChildren()
            .filter(component -> component instanceof Button).findFirst().map(component -> (Button) component)
            .ifPresent(Button::click);

        assertThat(GridKt._size(athletesGrid)).isEqualTo(86);

        assertThat(GridKt._get(athletesGrid, 40).getLastName()).isEqualTo("Zumstein");

        GridKt._getCellComponent(athletesGrid, 40, "remove-column").getChildren()
            .filter(component -> component instanceof Button).findFirst().map(component -> (Button) component)
            .ifPresent(Button::click);

        ConfirmDialog confirmDialog = _get(ConfirmDialog.class);
        assertThat(confirmDialog.isOpened()).isTrue();

        _fireConfirm(confirmDialog);

        assertThat(GridKt._size(athletesGrid)).isEqualTo(85);
    }

}
