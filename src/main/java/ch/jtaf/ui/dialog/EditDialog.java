package ch.jtaf.ui.dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.theme.lumo.Lumo;
import org.jooq.DSLContext;
import org.jooq.UpdatableRecord;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.Serial;
import java.util.function.Consumer;

import static ch.jtaf.context.ApplicationContextHolder.getBean;

public abstract class EditDialog<R extends UpdatableRecord<?>> extends Dialog {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String FULLSCREEN = "fullscreen";

    private final String initialWidth;

    private boolean isFullScreen = false;
    private final Div content;
    private final Button max;

    final Binder<R> binder;
    final FormLayout formLayout;

    private transient Consumer<R> afterSave;
    private boolean initialized;

    protected EditDialog(String title, String initialWidth) {
        this.initialWidth = initialWidth;
        setWidth(initialWidth);

        getElement().getThemeList().add("jtaf-dialog");
        getElement().setAttribute("aria-labelledby", "dialog-title");

        setDraggable(true);
        setResizable(true);

        var headerTitel = new H2(title);
        headerTitel.addClassName("dialog-title");

        max = new Button(VaadinIcon.EXPAND_SQUARE.create());
        max.addClickListener(event -> maximiseMinimize());

        var close = new Button(VaadinIcon.CLOSE_SMALL.create());
        close.addClickListener(event -> close());

        var header = new Header(headerTitel, max, close);
        header.getElement().getThemeList().add(Lumo.LIGHT);
        add(header);

        formLayout = new FormLayout();

        binder = new Binder<>();

        var save = new Button(getTranslation("Save"));
        save.setId("edit-save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> {
            getBean(TransactionTemplate.class).executeWithoutResult(transactionStatus -> {
                getBean(DSLContext.class).attach(binder.getBean());
                binder.getBean().store();

                if (afterSave != null) {
                    afterSave.accept(binder.getBean());
                }
            });
            close();
        });

        var cancel = new Button(getTranslation("Cancel"));
        cancel.addClickListener(event -> close());

        var buttons = new HorizontalLayout(save, cancel);
        buttons.getStyle().set("padding-top", "20px");

        content = new Div(formLayout, buttons);
        content.addClassName("dialog-content");

        add(content);
    }

    public abstract void createForm();

    @SuppressWarnings("unchecked")
    public void open(UpdatableRecord<?> updatableRecord, Consumer<R> afterSave) {
        binder.setBean((R) updatableRecord);
        this.afterSave = afterSave;

        if (!initialized) {
            createForm();
            initialized = true;
        }

        super.open();
    }

    private void initialSize() {
        max.setIcon(VaadinIcon.EXPAND_SQUARE.create());
        getElement().getThemeList().remove(FULLSCREEN);
        setHeight("auto");
        setWidth(initialWidth);
    }

    private void maximiseMinimize() {
        if (isFullScreen) {
            initialSize();
        } else {
            max.setIcon(VaadinIcon.COMPRESS_SQUARE.create());
            getElement().getThemeList().add(FULLSCREEN);
            setSizeFull();
            content.setVisible(true);
        }
        isFullScreen = !isFullScreen;
    }

}
