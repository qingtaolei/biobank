package edu.ualberta.med.biobank.views;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShipmentWrapper;
import edu.ualberta.med.biobank.forms.PatientEntryForm;
import edu.ualberta.med.biobank.forms.PatientViewForm;
import edu.ualberta.med.biobank.forms.ShipmentEntryForm;
import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.ClinicAdapter;
import edu.ualberta.med.biobank.treeview.PatientAdapter;
import edu.ualberta.med.biobank.treeview.RootNode;
import edu.ualberta.med.biobank.treeview.ShipmentAdapter;
import edu.ualberta.med.biobank.treeview.SiteAdapter;
import edu.ualberta.med.biobank.treeview.StudyAdapter;

public class ShipmentAdministrationView extends AbstractAdministrationView {

    public static final String ID = "edu.ualberta.med.biobank.views.shipmentAdmin";

    public static ShipmentAdministrationView currentInstance;

    private SiteAdapter currentSiteAdapter;

    public ShipmentAdministrationView() {
        currentInstance = this;
    }

    @Override
    protected Object search(String text) throws Exception {
        return ShipmentWrapper.getShipmentInSite(
            SessionManager.getAppService(), text, SessionManager.getInstance()
                .getCurrentSiteWrapper());
    }

    @Override
    protected String getNoFoundText() {
        return "- No shipment found -";
    }

    @Override
    public void showInTree(Object searchedObject) {
        rootNode.removeAll();
        ShipmentWrapper shipment = (ShipmentWrapper) searchedObject;
        currentSiteAdapter = new SiteAdapter(rootNode, SessionManager
            .getInstance().getCurrentSiteWrapper(), false);
        rootNode.addChild(currentSiteAdapter);
        ClinicAdapter clinicAdapter = new ClinicAdapter(currentSiteAdapter,
            shipment.getClinic(), false);
        currentSiteAdapter.addChild(clinicAdapter);
        ShipmentAdapter shipmentAdapter = new ShipmentAdapter(clinicAdapter,
            shipment);
        clinicAdapter.addChild(shipmentAdapter);
        shipmentAdapter.performExpand();
        shipmentAdapter.performDoubleClick();
    }

    @Override
    protected void notFound(String text) {
        rootNode.removeAll();
        rootNode.addChild(getNotFoundAdapter());
        boolean create = BioBankPlugin.openConfirm("Shipment not found",
            "Do you want to create this shipment ?");
        if (create) {
            ShipmentWrapper shipment = new ShipmentWrapper(SessionManager
                .getAppService());
            shipment.setWaybill(text);
            ShipmentAdapter adapter = new ShipmentAdapter(rootNode, shipment);
            AdapterBase.openForm(new FormInput(adapter), ShipmentEntryForm.ID);
        }
    }

    public static RootNode getRootNode() {
        return currentInstance.rootNode;
    }

    public static void setSelectedNode(AdapterBase node) {
        currentInstance.selectNode(node);
    }

    public void displayPatient(PatientWrapper patient) {
        if (currentSiteAdapter != null) {
            StudyAdapter studyAdapter = new StudyAdapter(currentSiteAdapter,
                patient.getStudy(), false);
            // the tree structure is created but is never added to the tree
            // displayed. We don't want to see these nodes
            PatientAdapter patientAdapter = new PatientAdapter(studyAdapter,
                patient, false);
            studyAdapter.addChild(patientAdapter);
            FormInput input = new FormInput(patientAdapter);
            if (patient.isNew()) {
                AdapterBase.openForm(input, PatientEntryForm.ID);
            } else {
                patientAdapter.setEditable(false);
                AdapterBase.openForm(input, PatientViewForm.ID);
            }
        }
    }
}
