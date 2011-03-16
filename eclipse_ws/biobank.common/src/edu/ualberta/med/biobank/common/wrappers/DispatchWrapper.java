package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.exception.BiobankException;
import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.peer.CenterPeer;
import edu.ualberta.med.biobank.common.peer.DispatchPeer;
import edu.ualberta.med.biobank.common.peer.ShipmentInfoPeer;
import edu.ualberta.med.biobank.common.peer.SitePeer;
import edu.ualberta.med.biobank.common.security.User;
import edu.ualberta.med.biobank.common.util.DispatchSpecimenState;
import edu.ualberta.med.biobank.common.util.DispatchState;
import edu.ualberta.med.biobank.common.wrappers.base.DispatchBaseWrapper;
import edu.ualberta.med.biobank.model.Dispatch;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class DispatchWrapper extends DispatchBaseWrapper {

    private final Map<DispatchSpecimenState, List<DispatchSpecimenWrapper>> dispatchSpecimenMap = new HashMap<DispatchSpecimenState, List<DispatchSpecimenWrapper>>();

    private List<DispatchSpecimenWrapper> deletedDispatchedSpecimens = new ArrayList<DispatchSpecimenWrapper>();

    public DispatchWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public DispatchWrapper(WritableApplicationService appService,
        Dispatch dispatch) {
        super(appService, dispatch);
    }

    public String getStateDescription() {
        DispatchState state = DispatchState
            .getState(getProperty(DispatchPeer.STATE));
        if (state == null)
            return "";
        return state.getLabel();
    }

    public DispatchState getDispatchState() {
        return DispatchState.getState(getState());
    }

    public String getFormattedPackedAt() {
        return DateFormatter.formatAsDateTime(getPackedAt());
    }

    public boolean hasErrors() {
        return !getDispatchSpecimenCollectionWithState(
            DispatchSpecimenState.MISSING, DispatchSpecimenState.EXTRA)
            .isEmpty();
    }

    @Override
    protected void persistChecks() throws BiobankException,
        ApplicationException {
        if (getSenderCenter() == null) {
            throw new BiobankCheckException("Sender should be set");
        }
        if (getReceiverCenter() == null) {
            throw new BiobankCheckException("Receiver should be set");
        }

        if (!checkWaybillUniqueForSender()) {
            throw new BiobankCheckException("A dispatch with waybill "
                + getShipmentInfo().getWaybill()
                + " already exists for sending site "
                + getSenderCenter().getNameShort());
        }
    }

    @Override
    protected void persistDependencies(Dispatch origObject) throws Exception {
        for (DispatchSpecimenWrapper dsa : deletedDispatchedSpecimens) {
            if (!dsa.isNew()) {
                dsa.delete();
            }
        }
    }

    private static final String WAYBILL_UNIQUE_FOR_SENDER_QRY = "from "
        + Dispatch.class.getName()
        + " where "
        + Property.concatNames(DispatchPeer.SENDER_CENTER, CenterPeer.ID)
        + "=? and "
        + Property.concatNames(DispatchPeer.SHIPMENT_INFO,
            ShipmentInfoPeer.WAYBILL) + "=?";

    private boolean checkWaybillUniqueForSender() throws ApplicationException,
        BiobankCheckException {
        List<Object> params = new ArrayList<Object>();
        CenterWrapper<?> sender = getSenderCenter();
        if (sender == null) {
            throw new BiobankCheckException("sender site cannot be null");
        }
        params.add(sender.getId());
        if (getShipmentInfo() == null)
            params.add("");
        else
            params.add(getShipmentInfo().getWaybill());

        StringBuilder qry = new StringBuilder(WAYBILL_UNIQUE_FOR_SENDER_QRY);
        if (!isNew()) {
            qry.append(" and id <> ?");
            params.add(getId());
        }
        HQLCriteria c = new HQLCriteria(qry.toString(), params);

        List<Object> results = appService.query(c);
        return results.size() == 0;
    }

    private List<DispatchSpecimenWrapper> getDispatchSpecimenCollectionWithState(
        DispatchSpecimenState... states) {
        return getDispatchSpecimenCollectionWithState(dispatchSpecimenMap,
            getDispatchSpecimenCollection(false), states);
    }

    private List<DispatchSpecimenWrapper> getDispatchSpecimenCollectionWithState(
        Map<DispatchSpecimenState, List<DispatchSpecimenWrapper>> map,
        List<DispatchSpecimenWrapper> list, DispatchSpecimenState... states) {

        if (map.isEmpty()) {
            for (DispatchSpecimenState state : DispatchSpecimenState.values()) {
                map.put(state, new ArrayList<DispatchSpecimenWrapper>());
            }
            for (DispatchSpecimenWrapper wrapper : list) {
                map.get(wrapper.getDispatchSpecimenState()).add(wrapper);
            }
        }

        if (states.length == 1) {
            return map.get(states[0]);
        } else {
            List<DispatchSpecimenWrapper> tmp = new ArrayList<DispatchSpecimenWrapper>();
            for (DispatchSpecimenState state : states) {
                tmp.addAll(map.get(state));
            }
            return tmp;
        }
    }

    public List<SpecimenWrapper> getSpecimenCollection(boolean sort) {
        List<SpecimenWrapper> list = new ArrayList<SpecimenWrapper>();
        for (DispatchSpecimenWrapper da : getDispatchSpecimenCollection(false)) {
            list.add(da.getSpecimen());
        }
        if (sort) {
            Collections.sort(list);
        }
        return list;
    }

    public List<SpecimenWrapper> getSpecimenCollection() {
        return getSpecimenCollection(true);
    }

    public void addSpecimens(List<SpecimenWrapper> newSpecimens) {
        if (newSpecimens == null)
            return;

        // already added dsa
        List<DispatchSpecimenWrapper> currentDaList = getDispatchSpecimenCollection(false);
        List<DispatchSpecimenWrapper> newDispatchSpecimens = new ArrayList<DispatchSpecimenWrapper>();
        List<SpecimenWrapper> currentAliquotList = new ArrayList<SpecimenWrapper>();

        for (DispatchSpecimenWrapper dsa : currentDaList) {
            currentAliquotList.add(dsa.getSpecimen());
        }

        // new aliquots added
        for (SpecimenWrapper aliquot : newSpecimens) {
            if (!currentAliquotList.contains(aliquot)) {
                DispatchSpecimenWrapper dsa = new DispatchSpecimenWrapper(
                    appService);
                dsa.setSpecimen(aliquot);
                dsa.setDispatch(this);
                dsa.setDispatchSpecimenState(DispatchSpecimenState.NONE);
                newDispatchSpecimens.add(dsa);
                dispatchSpecimenMap.get(DispatchSpecimenState.NONE).add(dsa);
            }
        }

        addToDispatchSpecimenCollection(newDispatchSpecimens);

        // make sure previously deleted ones, that have been re-added, are
        // no longer deleted
        deletedDispatchedSpecimens.removeAll(newDispatchSpecimens);
    }

    public static class CheckStatus {
        public CheckStatus(boolean b, String string) {
            this.ok = b;
            this.message = string;
        }

        public boolean ok = true;
        public String message;

    }

    public CheckStatus checkCanAddSpecimen(SpecimenWrapper spc,
        boolean checkAlreadyAdded) {
        return checkCanAddSpecimen(getSpecimenCollection(), spc,
            checkAlreadyAdded);
    }

    public CheckStatus checkCanAddSpecimen(
        List<SpecimenWrapper> currentAliquots, SpecimenWrapper aliquot,
        boolean checkAlreadyAdded) {
        if (aliquot.isNew()) {
            return new CheckStatus(false, "Cannot add aliquot "
                + aliquot.getInventoryId() + ": it has not already been saved");
        }
        if (!aliquot.isActive()) {
            return new CheckStatus(false, "Activity status of "
                + aliquot.getInventoryId() + " is not 'Active'."
                + " Check comments on this aliquot for more information.");
        }
        if (!aliquot.getCurrentCenter().equals(getSenderCenter())) {
            return new CheckStatus(false, "Aliquot " + aliquot.getInventoryId()
                + " is currently assigned to site "
                + aliquot.getCurrentCenter().getNameShort()
                + ". It should be first assigned to "
                + getSenderCenter().getNameShort() + " site.");
        }
        if (checkAlreadyAdded && currentAliquots != null
            && currentAliquots.contains(aliquot)) {
            return new CheckStatus(false, aliquot.getInventoryId()
                + " is already in this Dispatch.");
        }
        if (aliquot.isUsedInDispatch()) {
            return new CheckStatus(false, aliquot.getInventoryId()
                + " is already in a Dispatch in-transit or in creation.");
        }
        return new CheckStatus(true, "");
    }

    @Override
    public void removeFromDispatchSpecimenCollection(
        List<DispatchSpecimenWrapper> dasToRemove) {
        super.removeFromDispatchSpecimenCollection(dasToRemove);
        resetMap();
    }

    public void removeAliquots(List<SpecimenWrapper> aliquotsToRemove) {
        if (aliquotsToRemove == null) {
            throw new NullPointerException();
        }

        if (aliquotsToRemove.isEmpty())
            return;

        List<DispatchSpecimenWrapper> currentDaList = getDispatchSpecimenCollection(false);
        List<DispatchSpecimenWrapper> removeDispatchSpecimens = new ArrayList<DispatchSpecimenWrapper>();

        for (DispatchSpecimenWrapper dsa : currentDaList) {
            if (aliquotsToRemove.contains(dsa.getSpecimen())) {
                removeDispatchSpecimens.add(dsa);
                deletedDispatchedSpecimens.add(dsa);
            }
        }
        removeFromDispatchSpecimenCollection(removeDispatchSpecimens);
    }

    public void receiveSpecimens(List<SpecimenWrapper> specimensToReceive) {
        List<DispatchSpecimenWrapper> nonProcessedAliquots = getDispatchSpecimenCollectionWithState(DispatchSpecimenState.NONE);
        for (DispatchSpecimenWrapper da : nonProcessedAliquots) {
            if (specimensToReceive.contains(da.getSpecimen())) {
                da.setDispatchSpecimenState(DispatchSpecimenState.RECEIVED);
                da.getDispatchSpecimenState();
            }
        }
        resetMap();
    }

    public boolean isInCreationState() {
        return getDispatchState() == null
            || DispatchState.CREATION.equals(getDispatchState());
    }

    public boolean isInTransitState() {
        return DispatchState.IN_TRANSIT.equals(getDispatchState());
    }

    public boolean isInReceivedState() {
        return DispatchState.RECEIVED.equals(getDispatchState());
    }

    public boolean hasBeenReceived() {
        return EnumSet.of(DispatchState.RECEIVED, DispatchState.CLOSED)
            .contains(getDispatchState());
    }

    public boolean isInClosedState() {
        return DispatchState.CLOSED.equals(getDispatchState());
    }

    private static final String DISPATCHES_IN_SITE_QRY = "from "
        + Dispatch.class.getName()
        + " where ("
        + Property.concatNames(DispatchPeer.SENDER_CENTER, SitePeer.ID)
        + "=? or "
        + Property.concatNames(DispatchPeer.RECEIVER_CENTER, SitePeer.ID)
        + "=?) and "
        + Property.concatNames(DispatchPeer.SHIPMENT_INFO,
            ShipmentInfoPeer.WAYBILL) + "=?";

    /**
     * Search for shipments with the given waybill. Site can be the sender or
     * the receiver.
     */
    public static List<DispatchWrapper> getDispatchesInSite(
        WritableApplicationService appService, String waybill, SiteWrapper site)
        throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria(DISPATCHES_IN_SITE_QRY,
            Arrays.asList(new Object[] { site.getId(), site.getId(), waybill }));
        List<Dispatch> shipments = appService.query(criteria);
        List<DispatchWrapper> wrappers = new ArrayList<DispatchWrapper>();
        for (Dispatch s : shipments) {
            wrappers.add(new DispatchWrapper(appService, s));
        }
        return wrappers;
    }

    private static final String DISPATCHES_IN_SITE_BY_DATE_PACKED_AT = "from "
        + Dispatch.class.getName() + " where ("
        + Property.concatNames(DispatchPeer.SENDER_CENTER, SitePeer.ID)
        + "=? or "
        + Property.concatNames(DispatchPeer.RECEIVER_CENTER, SitePeer.ID)
        + "=?) and " + DispatchPeer.PACKED_AT.getName() + ">=? and "
        + DispatchPeer.PACKED_AT.getName() + "<=?";

    /**
     * Search for shipments with the given date sent. Don't use hour and minute.
     * Site can be the sender or the receiver.
     */
    public static List<DispatchWrapper> getDispatchesInSiteByPackedAt(
        WritableApplicationService appService, Date dateReceived,
        SiteWrapper site) throws ApplicationException {
        Calendar cal = Calendar.getInstance();
        // date at 0:0am
        cal.setTime(dateReceived);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime();
        // date at 0:0pm
        cal.add(Calendar.DATE, 1);
        Date endDate = cal.getTime();
        HQLCriteria criteria = new HQLCriteria(
            DISPATCHES_IN_SITE_BY_DATE_PACKED_AT, Arrays.asList(new Object[] {
                site.getId(), site.getId(), startDate, endDate }));
        List<Dispatch> shipments = appService.query(criteria);
        List<DispatchWrapper> wrappers = new ArrayList<DispatchWrapper>();
        for (Dispatch s : shipments) {
            wrappers.add(new DispatchWrapper(appService, s));
        }
        return wrappers;
    }

    private static final String DISPATCHES_IN_SITE_BY_DATE_RECEIVED_QRY = "from "
        + Dispatch.class.getName()
        + " where ("
        + Property.concatNames(DispatchPeer.SENDER_CENTER, SitePeer.ID)
        + "=? or "
        + Property.concatNames(DispatchPeer.RECEIVER_CENTER, SitePeer.ID)
        + "=?) and "
        + Property.concatNames(DispatchPeer.SHIPMENT_INFO,
            ShipmentInfoPeer.RECEIVED_AT)
        + ">=? and "
        + Property.concatNames(DispatchPeer.SHIPMENT_INFO,
            ShipmentInfoPeer.RECEIVED_AT) + "<=?";

    /**
     * Search for shipments with the given date received. Don't use hour and
     * minute. Site can be the sender or the receiver.
     */
    public static List<DispatchWrapper> getDispatchesInSiteByDateReceived(
        WritableApplicationService appService, Date dateReceived,
        SiteWrapper site) throws ApplicationException {
        Calendar cal = Calendar.getInstance();
        // date at 0:0am
        cal.setTime(dateReceived);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime();
        // date at 0:0pm
        cal.add(Calendar.DATE, 1);
        Date endDate = cal.getTime();
        HQLCriteria criteria = new HQLCriteria(
            DISPATCHES_IN_SITE_BY_DATE_RECEIVED_QRY,
            Arrays.asList(new Object[] { site.getId(), site.getId(), startDate,
                endDate }));
        List<Dispatch> shipments = appService.query(criteria);
        List<DispatchWrapper> wrappers = new ArrayList<DispatchWrapper>();
        for (Dispatch s : shipments) {
            wrappers.add(new DispatchWrapper(appService, s));
        }
        return wrappers;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getSenderCenter() == null ? "" : getSenderCenter()
            .getNameShort() + "/");
        sb.append(getReceiverCenter() == null ? "" : getReceiverCenter()
            .getNameShort() + "/");
        sb.append(getShipmentInfo().getFormattedDateReceived());
        return sb.toString();
    }

    public boolean canBeSentBy(User user) {
        return canUpdate(user)
            && getSenderCenter().equals(user.getCurrentWorkingCenter())
            && isInCreationState() && hasDispatchSpecimens();
    }

    public boolean hasDispatchSpecimens() {
        return getSpecimenCollection() != null
            && !getSpecimenCollection().isEmpty();
    }

    public boolean canBeReceivedBy(User user) {
        return canUpdate(user)
            && getReceiverCenter().equals(user.getCurrentWorkingCenter())
            && isInTransitState();
    }

    public DispatchSpecimenWrapper getDispatchSpecimen(String inventoryId) {
        for (DispatchSpecimenWrapper dsa : getDispatchSpecimenCollection(false)) {
            if (dsa.getSpecimen().getInventoryId().equals(inventoryId))
                return dsa;
        }
        return null;
    }

    public void setState(DispatchState ds) {
        setState(ds.getId());
    }

    public boolean isInLostState() {
        return DispatchState.LOST.equals(getDispatchState());
    }

    public List<DispatchSpecimenWrapper> getNonProcessedDispatchSpecimenCollection() {
        return getDispatchSpecimenCollectionWithState(DispatchSpecimenState.NONE);
    }

    public List<DispatchSpecimenWrapper> getExtraDispatchSpecimens() {
        return getDispatchSpecimenCollectionWithState(DispatchSpecimenState.EXTRA);
    }

    public List<DispatchSpecimenWrapper> getMissingDispatchSpecimens() {
        return getDispatchSpecimenCollectionWithState(DispatchSpecimenState.MISSING);
    }

    public List<DispatchSpecimenWrapper> getReceivedDispatchSpecimens() {
        return getDispatchSpecimenCollectionWithState(DispatchSpecimenState.RECEIVED);
    }

    public void addExtraAliquots(List<SpecimenWrapper> extraAliquots) {
        List<DispatchSpecimenWrapper> daws = new ArrayList<DispatchSpecimenWrapper>();
        for (SpecimenWrapper a : extraAliquots) {
            DispatchSpecimenWrapper da = new DispatchSpecimenWrapper(appService);
            da.setSpecimen(a);
            da.setDispatch(this);
            da.setDispatchSpecimenState(DispatchSpecimenState.EXTRA);
            daws.add(da);
        }
        addToDispatchSpecimenCollection(daws);
        resetMap();
    }

    public List<DispatchSpecimenWrapper> getDispatchSpecimenCollection() {
        return getDispatchSpecimenCollection(false);
    }

    public void addAliquots(List<SpecimenWrapper> aliquots) {
        List<DispatchSpecimenWrapper> daws = new ArrayList<DispatchSpecimenWrapper>();
        for (SpecimenWrapper a : aliquots) {
            DispatchSpecimenWrapper da = new DispatchSpecimenWrapper(appService);
            da.setSpecimen(a);
            da.setDispatch(this);
            da.setDispatchSpecimenState(DispatchSpecimenState.NONE);
            daws.add(da);
        }
        addToDispatchSpecimenCollection(daws);
        resetMap();
    }

    public boolean canBeClosedBy(User user) {
        return isInReceivedState() && canUpdate(user);
    }

    @Override
    public void reload() throws Exception {
        super.reload();
        resetMap();
    }

    @Override
    public void reset() throws Exception {
        super.reset();
        resetMap();
    }

    public void resetMap() {
        dispatchSpecimenMap.clear();
    }
}