package edu.ualberta.med.biobank.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.cevent.CollectionEventInfo;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.gui.common.BgcLogger;
import edu.ualberta.med.biobank.model.CollectionEvent;
import edu.ualberta.med.biobank.treeview.patient.CollectionEventAdapter;
import edu.ualberta.med.biobank.treeview.patient.PatientAdapter;
import edu.ualberta.med.biobank.views.CollectionView;

public class CollectionEventAddHandler extends AbstractHandler {

    private static BgcLogger logger = BgcLogger
        .getLogger(CollectionEventAddHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            PatientAdapter patientAdapter = CollectionView.getCurrentPatient();
            CollectionEvent ce = new CollectionEvent();
            ce.setPatient(patientAdapter.getModelObject().patient);
            CollectionEventInfo ceventInfo = new CollectionEventInfo();
            ceventInfo.cevent = ce;
            CollectionEventAdapter adapter = new CollectionEventAdapter(
                patientAdapter, ceventInfo);
            adapter.openEntryForm();
        } catch (Exception exp) {
            logger.error("Error while opening the collection event entry form", //$NON-NLS-1$
                exp);
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        return SessionManager.canCreate(CollectionEventWrapper.class);
    }
}