package edu.ualberta.med.biobank.forms;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PartInitException;

import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.model.Capacity;
import edu.ualberta.med.biobank.model.ContainerType;
import edu.ualberta.med.biobank.model.SampleType;
import edu.ualberta.med.biobank.treeview.ContainerTypeAdapter;
import edu.ualberta.med.biobank.widgets.CabinetDrawerWidget;
import edu.ualberta.med.biobank.widgets.ChooseContainerWidget;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class ContainerTypeViewForm extends BiobankViewForm {
    public static final String ID = "edu.ualberta.med.biobank.forms.ContainerTypeViewForm";

    private ContainerTypeAdapter containerTypeAdapter;

    private ContainerType containerType;

    private Capacity capacity;

    private Label nameLabel;

    private Label nameShortLabel;

    private Label defaultTempLabel;

    private Label numSchemeLabel;

    private Label activityStatusLabel;

    private Label commentLabel;

    private Label dimOneCapacityLabel;

    private Label dimTwoCapacityLabel;

    private org.eclipse.swt.widgets.List sampleTypesList;

    private org.eclipse.swt.widgets.List childContainerTypesList;

    public ContainerTypeViewForm() {
        super();
    }

    @Override
    public void init() {
        Assert.isTrue(adapter instanceof ContainerTypeAdapter,
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        containerTypeAdapter = (ContainerTypeAdapter) adapter;
        retrieveContainerType();
        setPartName("Container Type " + containerType.getName());
    }

    private void retrieveContainerType() {
        List<ContainerType> result;
        ContainerType searchContainerType = new ContainerType();
        searchContainerType.setId(containerTypeAdapter.getContainerType()
            .getId());
        try {
            result = appService
                .search(ContainerType.class, searchContainerType);
            Assert.isTrue(result.size() == 1);
            containerType = result.get(0);
            containerTypeAdapter.setContainerType(containerType);
            capacity = containerType.getCapacity();
        } catch (ApplicationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void createFormContent() {
        form.setText("Container Type: " + containerType.getName());
        addRefreshToolbarAction();
        form.getBody().setLayout(new GridLayout(1, false));
        createContainerTypeSection();
        if (containerType.getChildContainerTypeCollection().size() > 0) {
            visualizeContainer();
        }
        createDimensionsSection();
        createChildContainerTypesSection();
        createSampleTypesSection();
        createButtons();
    }

    private void createContainerTypeSection() {
        Composite client = toolkit.createComposite(form.getBody());
        client.setLayout(new GridLayout(2, false));
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        nameLabel = (Label) createWidget(client, Label.class, SWT.NONE, "Name");
        nameShortLabel = (Label) createWidget(client, Label.class, SWT.NONE,
            "Short Name");
        defaultTempLabel = (Label) createWidget(client, Label.class, SWT.NONE,
            "Default Temperature\n(Celcius)");
        numSchemeLabel = (Label) createWidget(client, Label.class, SWT.NONE,
            "Child Labeling Scheme");
        activityStatusLabel = (Label) createWidget(client, Label.class,
            SWT.NONE, "Activity Status");
        commentLabel = (Label) createWidget(client, Label.class, SWT.NONE,
            "Comments");

        setContainerTypeValues();
    }

    private void setContainerTypeValues() {
        FormUtils.setTextValue(nameLabel, containerType.getName());
        FormUtils.setTextValue(nameShortLabel, containerType.getNameShort());
        FormUtils.setTextValue(defaultTempLabel, containerType
            .getDefaultTemperature());
        FormUtils.setTextValue(numSchemeLabel, containerType
            .getChildLabelingScheme() == null ? "" : containerType
            .getChildLabelingScheme().getName());
        FormUtils.setTextValue(activityStatusLabel, containerType
            .getActivityStatus());
        FormUtils.setTextValue(commentLabel, containerType.getComment());
    }

    private void createDimensionsSection() {
        Composite client = createSectionWithClient("Default Capacity");
        GridLayout layout = (GridLayout) client.getLayout();
        layout.numColumns = 2;
        layout.horizontalSpacing = 10;
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        dimOneCapacityLabel = (Label) createWidget(client, Label.class,
            SWT.NONE, "Dimension One Capacity");
        dimTwoCapacityLabel = (Label) createWidget(client, Label.class,
            SWT.NONE, "Dimension Two Capacity");

        setDimensionsValues();
    }

    private void setDimensionsValues() {
        FormUtils.setTextValue(dimOneCapacityLabel, capacity
            .getDimensionOneCapacity());
        FormUtils.setTextValue(dimTwoCapacityLabel, capacity
            .getDimensionTwoCapacity());
    }

    private void createSampleTypesSection() {
        Composite client = createSectionWithClient("Contains Samples");
        GridLayout layout = (GridLayout) client.getLayout();
        layout.numColumns = 2;
        layout.horizontalSpacing = 10;
        toolkit.paintBordersFor(client);

        Label label = toolkit.createLabel(client, "Sample types:");
        label
            .setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));

        sampleTypesList = new org.eclipse.swt.widgets.List(client, SWT.BORDER
            | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        sampleTypesList.setLayoutData(gd);
        setSampleDerivTypesValues();
    }

    private void setSampleDerivTypesValues() {
        sampleTypesList.removeAll();
        for (SampleType type : containerType.getSampleTypeCollection()) {
            sampleTypesList.add(type.getNameShort());
        }
    }

    private void createChildContainerTypesSection() {
        Composite client = createSectionWithClient("Contains Container Types");
        GridLayout layout = (GridLayout) client.getLayout();
        layout.numColumns = 2;
        layout.horizontalSpacing = 10;
        toolkit.paintBordersFor(client);

        Label label = toolkit.createLabel(client, "Container types:");
        label
            .setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));

        childContainerTypesList = new org.eclipse.swt.widgets.List(client,
            SWT.BORDER | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        childContainerTypesList.setLayoutData(gd);
        setChildContainerTypesValues();
    }

    protected void visualizeContainer() {
        // default 2 dimensional grid
        int rowHeight = 40, colWidth = 40;
        Composite client = createSectionWithClient("Container Visual");

        // get occupied positions
        if (containerType.getName().equalsIgnoreCase("Drawer")) {
            // if Drawer, requires special grid
            CabinetDrawerWidget containerWidget = new CabinetDrawerWidget(
                client);
            GridData gdBin = new GridData();
            gdBin.widthHint = CabinetDrawerWidget.WIDTH;
            gdBin.heightHint = CabinetDrawerWidget.HEIGHT;
            gdBin.verticalSpan = 2;
            containerWidget.setLayoutData(gdBin);
        } else {
            // otherwise, normal grid
            ChooseContainerWidget containerWidget = new ChooseContainerWidget(
                client);
            containerWidget.setParams(containerType, null);

            int dim1 = containerType.getCapacity().getDimensionOneCapacity()
                .intValue();
            int dim2 = containerType.getCapacity().getDimensionTwoCapacity()
                .intValue();
            if (dim2 <= 1) {
                // single dimension size
                rowHeight = 40;
                colWidth = 150;
                containerWidget.setLegendOnSide(true);
            }

            containerWidget.setGridSizes(dim1, dim2, colWidth * dim2, rowHeight
                * dim1);
        }
    }

    private void setChildContainerTypesValues() {
        childContainerTypesList.removeAll();
        for (ContainerType type : containerType
            .getChildContainerTypeCollection()) {
            childContainerTypesList.add(type.getName());
        }
    }

    private void createButtons() {
        Composite client = toolkit.createComposite(form.getBody());
        client.setLayout(new GridLayout(4, false));
        toolkit.paintBordersFor(client);

        final Button edit = toolkit.createButton(client,
            "Edit this information", SWT.PUSH);
        edit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getSite().getPage().closeEditor(ContainerTypeViewForm.this,
                    false);
                try {
                    getSite().getPage().openEditor(
                        new FormInput(containerTypeAdapter),
                        ContainerTypeEntryForm.ID, true);
                } catch (PartInitException exp) {
                    exp.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void reload() {
        retrieveContainerType();
        setPartName("Container Type " + containerType.getName());
        form.setText("Container Type: " + containerType.getName());
        setContainerTypeValues();
        setDimensionsValues();
        // setSampleDerivTypesValues();
        setChildContainerTypesValues();
    }
}
