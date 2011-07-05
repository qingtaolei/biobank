package edu.ualberta.med.biobank.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.gui.common.widgets.BgcBaseText;
import edu.ualberta.med.biobank.gui.common.widgets.BgcBaseWidget;
import edu.ualberta.med.biobank.gui.common.widgets.utils.BgcWidgetCreator;
import edu.ualberta.med.biobank.widgets.infotables.IInfoTableDeleteItemListener;
import edu.ualberta.med.biobank.widgets.infotables.InfoTableEvent;
import edu.ualberta.med.biobank.widgets.infotables.SpecimenInfoTable;
import edu.ualberta.med.biobank.widgets.infotables.SpecimenInfoTable.ColumnsShown;
import edu.ualberta.med.biobank.widgets.infotables.entry.SpecimenEntryInfoTable;
import edu.ualberta.med.biobank.widgets.listeners.VetoListenerSupport;
import edu.ualberta.med.biobank.widgets.listeners.VetoListenerSupport.VetoException;
import edu.ualberta.med.biobank.widgets.listeners.VetoListenerSupport.VetoListener;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class SpecimenEntryWidget extends BgcBaseWidget {
    private WritableApplicationService appService;
    private List<SpecimenWrapper> specimens;
    private List<SpecimenWrapper> addedOrModifiedSpecimens = new ArrayList<SpecimenWrapper>();
    private List<SpecimenWrapper> removedSpecimens = new ArrayList<SpecimenWrapper>();
    private SpecimenInfoTable specTable;
    private BgcBaseText newSpecimenInventoryId;
    private boolean editable;
    private Button addButton;
    private IObservableValue hasSpecimens = new WritableValue(Boolean.FALSE,
        Boolean.class);

    // TODO: not sure these should all be interruptable
    public static enum ItemAction {
        PRE_ADD, POST_ADD, PRE_DELETE, POST_DELETE;
    }

    private VetoListenerSupport<ItemAction, SpecimenWrapper> vetoListenerSupport = new VetoListenerSupport<ItemAction, SpecimenWrapper>();

    public void addVetoListener(ItemAction action,
        VetoListener<ItemAction, SpecimenWrapper> listener) {
        vetoListenerSupport.addListener(action, listener);
    }

    public void removeVetoListener(ItemAction action,
        VetoListener<ItemAction, SpecimenWrapper> listener) {
        vetoListenerSupport.removeListener(action, listener);
    }

    public SpecimenEntryWidget(Composite parent, int style,
        FormToolkit toolkit, WritableApplicationService appService,
        boolean editable) {
        super(parent, style);
        Assert.isNotNull(toolkit, "toolkit is null");
        this.appService = appService;
        this.editable = editable;

        setLayout(new GridLayout(2, false));
        setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        toolkit.paintBordersFor(this);

        if (editable) {
            Label label = toolkit.createLabel(this,
                "Enter specimen inventory ID to add:");
            GridData gd = new GridData();
            gd.horizontalSpan = 2;
            label.setLayoutData(gd);
            newSpecimenInventoryId = new BgcBaseText(this, SWT.NONE, toolkit);
            newSpecimenInventoryId.addListener(SWT.DefaultSelection,
                new Listener() {
                    @Override
                    public void handleEvent(Event e) {
                        addSpecimen();
                        newSpecimenInventoryId.setFocus();
                        newSpecimenInventoryId.setText("");
                    }
                });
            addButton = toolkit.createButton(this, "", SWT.PUSH);
            addButton.setImage(BgcPlugin.getDefault().getImageRegistry()
                .get(BgcPlugin.IMG_ADD));
            addButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    addSpecimen();
                }
            });
        }

        if (editable) {
            specTable = new SpecimenEntryInfoTable(this, null,
                ColumnsShown.SOURCE_SPECIMENS);
        } else {
            specTable = new SpecimenInfoTable(this, null,
                ColumnsShown.SOURCE_SPECIMENS, 20);
        }

        specTable.adaptToToolkit(toolkit, true);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        specTable.setLayoutData(gd);
        addDeleteSupport();
    }

    public List<SpecimenWrapper> getAddedOrModifiedSpecimens() {
        return new ArrayList<SpecimenWrapper>(addedOrModifiedSpecimens);
    }

    public List<SpecimenWrapper> getRemovedSpecimens() {
        return new ArrayList<SpecimenWrapper>(removedSpecimens);
    }

    private void addSpecimen() {
        String inventoryId = newSpecimenInventoryId.getText().trim();
        if (!inventoryId.isEmpty()) {
            try {
                // FIXME related to issue #1198 : should call this method with
                // the current user, but can be a problem in dispatch. What
                // security modification should be made ? Should we check that
                // anyway ?
                SpecimenWrapper specimen = SpecimenWrapper.getSpecimen(
                    appService, inventoryId);
                addSpecimen(specimen);
            } catch (Exception e) {
                BgcPlugin.openAsyncError("Error while looking up specimen", e);
            }
        }
    }

    private void addSpecimen(SpecimenWrapper specimen) {
        if (specimen != null && specimens.contains(specimen)) {
            BgcPlugin.openAsyncError("Error",
                "Specimen " + specimen.getInventoryId()
                    + " has already been added to this list");
            return;
        }

        VetoListenerSupport.Event<ItemAction, SpecimenWrapper> preAdd = VetoListenerSupport.Event
            .newEvent(ItemAction.PRE_ADD, specimen);

        VetoListenerSupport.Event<ItemAction, SpecimenWrapper> postAdd = VetoListenerSupport.Event
            .newEvent(ItemAction.POST_ADD, specimen);

        try {
            vetoListenerSupport.notifyListeners(preAdd);

            specimens.add(specimen);
            Collections.sort(specimens);
            specTable.setCollection(specimens);

            notifyListeners();
            hasSpecimens.setValue(true);

            vetoListenerSupport.notifyListeners(postAdd);
        } catch (VetoException e) {
            BgcPlugin.openAsyncError("Error", e.getMessage());
        }
    }

    private void addDeleteSupport() {
        if (!editable)
            return;

        specTable.addDeleteItemListener(new IInfoTableDeleteItemListener() {
            @Override
            public void deleteItem(InfoTableEvent event) {
                SpecimenWrapper specimen = specTable.getSelection();
                if (specimen != null) {
                    if (!MessageDialog.openConfirm(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(),
                        "Delete Specimen",
                        "Are you sure you want to remove specimen "
                            + specimen.getInventoryId() + "?")) {
                        return;
                    }

                    VetoListenerSupport.Event<ItemAction, SpecimenWrapper> preDelete = VetoListenerSupport.Event
                        .newEvent(ItemAction.PRE_DELETE, specimen);

                    VetoListenerSupport.Event<ItemAction, SpecimenWrapper> postDelete = VetoListenerSupport.Event
                        .newEvent(ItemAction.POST_DELETE, specimen);

                    try {
                        vetoListenerSupport.notifyListeners(preDelete);
                        if (preDelete.doit) {
                            specimens.remove(specimen);
                            Collections.sort(specimens);
                            specTable.setCollection(specimens);

                            notifyListeners();
                            hasSpecimens.setValue(specimens.size() > 0);

                            vetoListenerSupport.notifyListeners(postDelete);
                        }
                    } catch (VetoException e) {
                        BgcPlugin.openAsyncError("Error", e.getMessage());
                    }
                }
            }
        });
    }

    public void setSpecimens(List<SpecimenWrapper> specimens) {
        this.specimens = specimens;

        if (specimens != null)
            specTable.setCollection(specimens);
        else
            specTable.setCollection(new ArrayList<SpecimenWrapper>());

        addedOrModifiedSpecimens.clear();
        removedSpecimens.clear();

        hasSpecimens.setValue(specimens != null && specimens.size() > 0);
    }

    public void addDoubleClickListener(IDoubleClickListener listener) {
        specTable.addClickListener(listener);
    }

    public void addBinding(BgcWidgetCreator dbc, final String message) {
        final ControlDecoration controlDecoration = createDecorator(addButton,
            message);
        WritableValue wv = new WritableValue(Boolean.FALSE, Boolean.class);
        UpdateValueStrategy uvs = new UpdateValueStrategy();
        uvs.setAfterGetValidator(new IValidator() {
            @Override
            public IStatus validate(Object value) {
                if (value instanceof Boolean && !(Boolean) value) {
                    controlDecoration.show();
                    return ValidationStatus.error(message);
                } else {
                    controlDecoration.hide();
                    return Status.OK_STATUS;
                }
            }
        });
        dbc.bindValue(wv, hasSpecimens, uvs, uvs);
    }
}
