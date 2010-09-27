package edu.ualberta.med.biobank.treeview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.springframework.remoting.RemoteAccessException;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ClinicShipmentWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.views.PatientAdministrationView;
import edu.ualberta.med.biobank.views.ShipmentAdministrationView;
import gov.nih.nci.system.applicationservice.ApplicationException;

public abstract class AbstractTodayNode extends AdapterBase {

    private static BiobankLogger logger = BiobankLogger
        .getLogger(AbstractTodayNode.class.getName());

    public AbstractTodayNode(AdapterBase parent, int id) {
        super(parent, id, "Today", true, false);
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
    }

    @Override
    protected void executeDoubleClick() {
        performExpand();
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        return null;
    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return 0;
    }

    @Override
    public String getViewFormId() {
        return null;
    }

    @Override
    public String getTooltipText() {
        return null;
    }

    @Override
    protected String getLabelInternal() {
        return null;
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    @Override
    public void performExpand() {
        if (!SessionManager.getInstance().isAllSitesSelected()) {
            try {
                List<? extends ModelWrapper<?>> todayElements =
                    getTodayElements();

                // remove elements that are not in today list
                for (AdapterBase child : getChildren()) {
                    ModelWrapper<?> childWrapper = child.getModelObject();
                    childWrapper.reload();
                    List<AdapterBase> subChildren =
                        new ArrayList<AdapterBase>(child.getChildren());
                    for (AdapterBase subChild : subChildren) {
                        ModelWrapper<?> subChildWrapper =
                            subChild.getModelObject();
                        subChildWrapper.reload();
                        if (!todayElements.contains(subChildWrapper)
                            || !isParentTo(childWrapper, subChildWrapper)) {
                            subChild.getParent().removeChild(subChild);
                        }
                    }
                }

                // add today elements is not yet there
                for (ModelWrapper<?> wrapper : todayElements) {
                    assert wrapper instanceof PatientWrapper
                        || wrapper instanceof ClinicShipmentWrapper;
                    if (wrapper instanceof PatientWrapper) {
                        PatientAdministrationView.getCurrent().addToNode(this,
                            wrapper);
                    } else if (wrapper instanceof ClinicShipmentWrapper) {
                        ShipmentAdministrationView.getCurrent().addToNode(this,
                            wrapper);
                    }
                }

                // remove sub children without any children
                List<AdapterBase> children =
                    new ArrayList<AdapterBase>(getChildren());
                for (AdapterBase child : children) {
                    if (child.getChildren().size() == 0) {
                        removeChild(child);
                    }
                }
            } catch (final RemoteAccessException exp) {
                BioBankPlugin.openRemoteAccessErrorMessage(exp);
            } catch (Exception e) {
                logger.error("Error while getting today's patients", e);
            }
        }
    }

    protected abstract boolean isParentTo(ModelWrapper<?> parent,
        ModelWrapper<?> child);

    protected abstract List<? extends ModelWrapper<?>> getTodayElements()
        throws ApplicationException;

    @Override
    public AdapterBase search(Object searchedObject) {
        return searchChildren(searchedObject);
    }
}
