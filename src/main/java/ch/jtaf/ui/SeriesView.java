package ch.jtaf.ui;

import ch.jtaf.configuration.security.OrganizationProvider;
import ch.jtaf.db.tables.records.*;
import ch.jtaf.domain.*;
import ch.jtaf.ui.dialog.*;
import ch.jtaf.ui.validator.NotEmptyValidator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.jooq.UpdatableRecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.jtaf.db.tables.Athlete.ATHLETE;
import static ch.jtaf.db.tables.Category.CATEGORY;
import static ch.jtaf.db.tables.Club.CLUB;
import static ch.jtaf.db.tables.Competition.COMPETITION;
import static ch.jtaf.db.tables.Series.SERIES;
import static ch.jtaf.ui.component.GridBuilder.addActionColumnAndSetSelectionListener;

@Route(layout = MainLayout.class)
public class SeriesView extends ProtectedView implements HasUrlParameter<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String BLANK = "_blank";

    private final transient CompetitionRepository competitionRepository;
    private final transient CategoryRepository categoryRepository;
    private final transient AthleteRepository athleteRepository;
    private final transient CategoryEventRepository categoryEventRepository;
    private final transient ClubRepository clubRepository;
    private final transient EventRepository eventRepository;
    private final transient SeriesRepository seriesRepository;
    private final transient CategoryAthleteRepository categoryAthleteRepository;
    private final transient NumberAndSheetsService numberAndSheetsService;

    private Button copyCategories;

    private SeriesRecord seriesRecord;

    private Grid<CompetitionRecord> competitionsGrid;
    private Grid<CategoryRecord> categoriesGrid;
    private Grid<AthleteRecord> athletesGrid;

    final Tabs sectionTabs = new Tabs();

    private final transient Binder<SeriesRecord> binder = new Binder<>();

    private Map<Long, ClubRecord> clubRecordMap;

    public SeriesView(CompetitionRepository competitionRepository, NumberAndSheetsService numberAndSheetsService,
                      OrganizationProvider organizationProvider, CategoryRepository categoryRepository,
                      CategoryEventRepository categoryEventRepository, AthleteRepository athleteRepository, ClubRepository clubRepository,
                      EventRepository eventRepository, SeriesRepository seriesRepository, CategoryAthleteRepository categoryAthleteRepository) {
        super(organizationProvider);
        this.competitionRepository = competitionRepository;
        this.numberAndSheetsService = numberAndSheetsService;
        this.categoryRepository = categoryRepository;
        this.categoryEventRepository = categoryEventRepository;
        this.athleteRepository = athleteRepository;
        this.clubRepository = clubRepository;
        this.eventRepository = eventRepository;
        this.seriesRepository = seriesRepository;
        this.categoryAthleteRepository = categoryAthleteRepository;

        createSeriesForm(organizationProvider, seriesRepository);

        sectionTabs.setWidthFull();
        add(sectionTabs);

        createCompetitionsSection();
        createCategoriesSection();
        createAthletesSection();

        var grids = new Div(competitionsGrid, categoriesGrid, athletesGrid);
        grids.setWidthFull();
        grids.setHeightFull();
        add(grids);

        var tabCompetitions = new Tab(getTranslation("Competitions"));
        sectionTabs.add(tabCompetitions);
        var tabCategories = new Tab(getTranslation("Categories"));
        sectionTabs.add(tabCategories);
        var tabAthletes = new Tab(getTranslation("Athletes"));
        sectionTabs.add(tabAthletes);

        var tabsToGrids = new HashMap<Tab, Grid<? extends UpdatableRecord<?>>>();
        tabsToGrids.put(tabCompetitions, competitionsGrid);
        tabsToGrids.put(tabCategories, categoriesGrid);
        tabsToGrids.put(tabAthletes, athletesGrid);

        categoriesGrid.setVisible(false);
        athletesGrid.setVisible(false);

        sectionTabs.addSelectedChangeListener(event -> {
            tabsToGrids.values().forEach(grid -> grid.setVisible(false));

            var selectedGrid = tabsToGrids.get(sectionTabs.getSelectedTab());
            selectedGrid.setVisible(true);
        });
    }

    private void createSeriesForm(OrganizationProvider organizationProvider, SeriesRepository seriesRepository) {
        var formLayout = new FormLayout();
        add(formLayout);

        var name = new TextField(getTranslation("Name"));
        name.setRequiredIndicatorVisible(true);
        formLayout.add(name);

        var buffer = new MultiFileMemoryBuffer();
        var upload = new Upload(buffer);
        upload.setId("logo-upload");
        upload.setMaxFiles(1);

        var uploadButton = new Button(getTranslation("Logo.upload"));
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(uploadButton);

        upload.setDropLabel(new Span(getTranslation("Logo.drop.here")));

        upload.addSucceededListener(event -> {
            try {
                var fileName = event.getFileName();
                var inputStream = buffer.getInputStream(fileName);
                SeriesRecord seriesRecord = binder.getBean();
                seriesRecord.setLogo(inputStream.readAllBytes());
                seriesRepository.save(seriesRecord);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        formLayout.add(upload);

        var checkboxes = new HorizontalLayout();
        add(checkboxes);

        binder.forField(name)
            .withValidator(new NotEmptyValidator(this))
            .bind(SeriesRecord::getName, SeriesRecord::setName);

        var hidden = new Checkbox(getTranslation("Hidden"));
        checkboxes.add(hidden);

        binder.forField(hidden)
            .withValidator((aBoolean, valueContext) -> ValidationResult.ok())
            .bind(SeriesRecord::getHidden, SeriesRecord::setHidden);

        var locked = new Checkbox(getTranslation("Locked"));
        checkboxes.add(locked);

        binder.forField(locked)
            .bind(SeriesRecord::getLocked, SeriesRecord::setLocked);

        var buttons = new HorizontalLayout();
        buttons.setPadding(false);
        add(buttons);

        var save = new Button(getTranslation("Save"));
        save.setId("save-series");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> {
            SeriesRecord seriesRecord = binder.getBean();
            seriesRepository.save(seriesRecord);

            Notification.show(getTranslation("Series.saved"), 6000, Notification.Position.TOP_END);
        });
        buttons.add(save);

        copyCategories = new Button(getTranslation("Copy.Categories"));
        copyCategories.setId("copy-categories");
        copyCategories.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        copyCategories.addClickListener(event -> {
            if (seriesRecord != null) {
                var dialog = new CopyCategoriesDialog(organizationProvider.getOrganization().getId(), seriesRecord.getId(), seriesRepository);
                dialog.addAfterCopyListener(e -> refreshAll());
                dialog.open();
            }
        });
        buttons.add(copyCategories);
    }

    @Override
    protected void refreshAll() {
        var competitionRecords = competitionRepository.findBySeriesId(seriesRecord.getId());
        competitionsGrid.setItems(competitionRecords);

        var categoryRecords = categoryRepository.findBySeriesId(seriesRecord.getId());
        categoriesGrid.setItems(categoryRecords);

        var clubs = clubRepository.findByOrganizationId(organizationProvider.getOrganization().getId());
        clubRecordMap = clubs.stream().collect(Collectors.toMap(ClubRecord::getId, clubRecord -> clubRecord));

        var athleteRecords = athleteRepository.findBySeriesId(seriesRecord.getId());
        athletesGrid.setItems(athleteRecords);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long seriesId) {
        if (seriesId == null) {
            organizationRecord = organizationProvider.getOrganization();

            seriesRecord = SERIES.newRecord();
            seriesRecord.setOrganizationId(organizationRecord.getId());
        } else {
            seriesRecord = seriesRepository.findById(seriesId).orElse(null);
        }
        binder.setBean(seriesRecord);

        if (seriesId == null) {
            // Series must be saved first
            copyCategories.setVisible(false);
        } else {
            if (categoryRepository.count(CATEGORY.SERIES_ID.eq(seriesId)) > 0) {
                // Copy is only possible if no categories are added
                copyCategories.setVisible(false);
            }
        }
    }

    @Override
    public String getPageTitle() {
        return getTranslation("Series");
    }

    private void createCompetitionsSection() {
        var dialog = new CompetitionDialog(getTranslation("Category"), competitionRepository);

        competitionsGrid = new Grid<>();
        competitionsGrid.setId("competitions-grid");
        competitionsGrid.setHeightFull();

        competitionsGrid.addColumn(CompetitionRecord::getName).setHeader(getTranslation("Name")).setSortable(true).setAutoWidth(true).setKey(COMPETITION.NAME.getName());
        competitionsGrid.addColumn(CompetitionRecord::getCompetitionDate).setHeader(getTranslation("Date")).setSortable(true).setAutoWidth(true).setKey(COMPETITION.COMPETITION_DATE.getName());
        competitionsGrid.addColumn(new ComponentRenderer<>(competition -> {
            var sheetsOrderedByAthlete = new Anchor(new StreamResource("sheets_orderby_athlete" + competition.getId() + ".pdf",
                () -> {
                    byte[] pdf = numberAndSheetsService.createSheets(competition.getSeriesId(), competition.getId(), getLocale(),
                        CATEGORY.ABBREVIATION, ATHLETE.LAST_NAME, ATHLETE.FIRST_NAME);
                    return new ByteArrayInputStream(pdf);
                }), getTranslation("Sheets"));
            sheetsOrderedByAthlete.setTarget(BLANK);

            var sheetsOrderedByClub = new Anchor(new StreamResource("sheets_orderby_club" + competition.getId() + ".pdf",
                () -> {
                    byte[] pdf = numberAndSheetsService.createSheets(competition.getSeriesId(), competition.getId(), getLocale(),
                        CLUB.ABBREVIATION, CATEGORY.ABBREVIATION, ATHLETE.LAST_NAME, ATHLETE.FIRST_NAME);
                    return new ByteArrayInputStream(pdf);
                }), getTranslation("Ordered.by.club"));
            sheetsOrderedByClub.setTarget(BLANK);

            var numbersOrderedByAthlete = new Anchor(new StreamResource("numbers_orderby_athlete" + competition.getId() + ".pdf",
                () -> {
                    byte[] pdf = numberAndSheetsService.createNumbers(competition.getSeriesId(), getLocale(),
                        CATEGORY.ABBREVIATION, ATHLETE.LAST_NAME, ATHLETE.FIRST_NAME);
                    return new ByteArrayInputStream(pdf);
                }), getTranslation("Numbers"));
            numbersOrderedByAthlete.setTarget(BLANK);

            var numbersOrderedByClub = new Anchor(new StreamResource("numbers_orderby_club" + competition.getId() + ".pdf",
                () -> {
                    byte[] pdf = numberAndSheetsService.createNumbers(competition.getSeriesId(), getLocale(),
                        CLUB.ABBREVIATION, CATEGORY.ABBREVIATION, ATHLETE.LAST_NAME, ATHLETE.FIRST_NAME);
                    return new ByteArrayInputStream(pdf);
                }), getTranslation("Ordered.by.club"));
            numbersOrderedByClub.setTarget(BLANK);

            return new HorizontalLayout(sheetsOrderedByAthlete, sheetsOrderedByClub, numbersOrderedByAthlete, numbersOrderedByClub);
        })).setAutoWidth(true);

        addActionColumnAndSetSelectionListener(competitionRepository, competitionsGrid, dialog, competitionRecord -> refreshAll(), () -> {
            var newRecord = COMPETITION.newRecord();
            newRecord.setMedalPercentage(0);
            newRecord.setSeriesId(seriesRecord.getId());
            return newRecord;
        }, this::refreshAll);
    }

    private void createCategoriesSection() {
        var dialog = new CategoryDialog(getTranslation("Category"), categoryRepository, categoryEventRepository, eventRepository,
            organizationProvider.getOrganization().getId());

        categoriesGrid = new Grid<>();
        categoriesGrid.setId("categories-grid");
        categoriesGrid.setHeightFull();

        categoriesGrid.addColumn(CategoryRecord::getAbbreviation).setHeader(getTranslation("Abbreviation")).setSortable(true).setAutoWidth(true).setKey(CATEGORY.ABBREVIATION.getName());
        categoriesGrid.addColumn(CategoryRecord::getName).setHeader(getTranslation("Name")).setSortable(true).setAutoWidth(true).setKey(CATEGORY.NAME.getName());
        categoriesGrid.addColumn(CategoryRecord::getYearFrom).setHeader(getTranslation("Year.From")).setSortable(true).setAutoWidth(true).setKey(CATEGORY.YEAR_FROM.getName());
        categoriesGrid.addColumn(CategoryRecord::getYearTo).setHeader(getTranslation("Year.To")).setSortable(true).setAutoWidth(true).setKey(CATEGORY.YEAR_TO.getName());
        categoriesGrid.addColumn(new ComponentRenderer<>(category -> {
            var sheet = new Anchor(new StreamResource("sheet" + category.getId() + ".pdf",
                () -> {
                    byte[] pdf = numberAndSheetsService.createEmptySheets(seriesRecord.getId(), category.getId(), getLocale());
                    return new ByteArrayInputStream(pdf);
                }), getTranslation("Sheets"));
            sheet.setTarget(BLANK);

            return new HorizontalLayout(sheet);
        })).setAutoWidth(true);

        addActionColumnAndSetSelectionListener(categoryRepository, categoriesGrid, dialog, categoryRecord -> refreshAll(), () -> {
            var newRecord = CATEGORY.newRecord();
            newRecord.setSeriesId(seriesRecord.getId());
            return newRecord;
        }, this::refreshAll);
    }

    private void createAthletesSection() {
        athletesGrid = new Grid<>();
        athletesGrid.setId("athletes-grid");
        athletesGrid.setHeightFull();

        athletesGrid.addColumn(AthleteRecord::getLastName).setHeader(getTranslation("Last.Name")).setSortable(true).setAutoWidth(true).setKey(ATHLETE.LAST_NAME.getName());
        athletesGrid.addColumn(AthleteRecord::getFirstName).setHeader(getTranslation("First.Name")).setSortable(true).setAutoWidth(true).setKey(ATHLETE.FIRST_NAME.getName());
        athletesGrid.addColumn(AthleteRecord::getGender).setHeader(getTranslation("Gender")).setSortable(true).setAutoWidth(true).setKey(ATHLETE.GENDER.getName());
        athletesGrid.addColumn(AthleteRecord::getYearOfBirth).setHeader(getTranslation("Year")).setSortable(true).setAutoWidth(true).setKey(ATHLETE.YEAR_OF_BIRTH.getName());
        athletesGrid.addColumn(athleteRecord -> athleteRecord.getClubId() == null ? null
            : clubRecordMap.get(athleteRecord.getClubId()).getAbbreviation()).setHeader(getTranslation("Club")).setAutoWidth(true);

        var assign = new Button(getTranslation("Assign.Athlete"));
        assign.setId("assign-athlete");
        assign.addClickListener(event -> {
            SearchAthleteDialog dialog = new SearchAthleteDialog(athleteRepository, clubRepository, organizationProvider,
                organizationRecord.getId(), seriesRecord.getId(), this::onAthleteSelect);
            dialog.open();
        });

        athletesGrid.addComponentColumn(athleteRecord -> {
            var remove = new Button(getTranslation("Remove"));
            remove.addThemeVariants(ButtonVariant.LUMO_ERROR);
            remove.addClickListener(event ->
                new ConfirmDialog(
                    "athlete-delete-confirm-dialog",
                    getTranslation("Confirm"),
                    getTranslation("Are.you.sure"),
                    getTranslation("Remove"), e -> removeAthleteFromSeries(athleteRecord),
                    getTranslation("Cancel"), e -> {
                }).open());

            var horizontalLayout = new HorizontalLayout(remove);
            horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            return horizontalLayout;
        }).setTextAlign(ColumnTextAlign.END).setHeader(assign).setAutoWidth(true).setKey("remove-column");
    }

    private void onAthleteSelect(SearchAthleteDialog.AthleteSelectedEvent athleteSelectedEvent) {
        var athleteRecord = athleteSelectedEvent.getAthleteRecord();
        categoryAthleteRepository.createCategoryAthlete(athleteRecord, seriesRecord.getId());

        refreshAll();
    }

    private void removeAthleteFromSeries(UpdatableRecord<?> updatableRecord) {
        var athleteRecord = (AthleteRecord) updatableRecord;
        categoryAthleteRepository.deleteCategoryAthlete(athleteRecord, seriesRecord.getId());
        refreshAll();
    }

}
