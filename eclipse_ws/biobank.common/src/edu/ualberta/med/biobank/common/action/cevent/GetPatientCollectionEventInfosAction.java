package edu.ualberta.med.biobank.common.action.cevent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionException;
import edu.ualberta.med.biobank.common.action.cevent.GetPatientCollectionEventInfosAction.PatientCEventInfo;
import edu.ualberta.med.biobank.common.peer.CollectionEventPeer;
import edu.ualberta.med.biobank.common.peer.PatientPeer;
import edu.ualberta.med.biobank.common.peer.SpecimenPeer;
import edu.ualberta.med.biobank.common.util.NotAProxy;
import edu.ualberta.med.biobank.common.wrappers.Property;
import edu.ualberta.med.biobank.model.CollectionEvent;
import edu.ualberta.med.biobank.model.User;

public class GetPatientCollectionEventInfosAction implements
    Action<List<PatientCEventInfo>> {
    private static final long serialVersionUID = 1L;
    // @formatter:off
    @SuppressWarnings("nls")
    private static final String CEVENT_INFO_QRY = 
    "select cevent, COUNT(DISTINCT sourcesSpecs), COUNT(DISTINCT aliquotedSpecs), min(sourcesSpecs." + SpecimenPeer.CREATED_AT.getName() + ")"
    + " from " + CollectionEvent.class.getName() + " as cevent"
    + " left join cevent." + CollectionEventPeer.ORIGINAL_SPECIMEN_COLLECTION.getName() + " as sourcesSpecs"
    + " left join cevent." + CollectionEventPeer.ALL_SPECIMEN_COLLECTION.getName() + " as aliquotedSpecs"
    + " where cevent." + Property.concatNames(CollectionEventPeer.PATIENT, PatientPeer.ID) + "=?"
    + " and aliquotedSpecs." + SpecimenPeer.PARENT_SPECIMEN.getName()+ " is null" // count only aliquoted Specimen-s
    + " GROUP BY cevent";
    // @formatter:on

    private final Integer patientId;

    public static class PatientCEventInfo implements Serializable, NotAProxy {
        private static final long serialVersionUID = 1L;
        public CollectionEvent cevent;
        public Long sourceSpecimenCount;
        public Long aliquotedSpecimenCount;
        public Date minSourceSpecimenDate;
    }

    public GetPatientCollectionEventInfosAction(Integer patientId) {
        this.patientId = patientId;
    }

    @Override
    public boolean isAllowed(User user, Session session) {
        return true; // TODO: restrict access
    }

    @Override
    public List<PatientCEventInfo> doAction(Session session)
        throws ActionException {
        List<PatientCEventInfo> ceventInfos = new ArrayList<PatientCEventInfo>();

        Query query = session.createQuery(CEVENT_INFO_QRY);
        query.setParameter(0, patientId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.list();
        for (Object[] row : rows) {
            PatientCEventInfo ceventInfo = new PatientCEventInfo();
            ceventInfo.cevent = (CollectionEvent) row[0];
            ceventInfo.sourceSpecimenCount = (Long) row[1];
            ceventInfo.aliquotedSpecimenCount = (Long) row[2];
            ceventInfo.minSourceSpecimenDate = (Date) row[3];
            ceventInfos.add(ceventInfo);
        }
        return ceventInfos;
    }
}