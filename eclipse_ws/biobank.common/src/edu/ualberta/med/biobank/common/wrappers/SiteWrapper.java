package edu.ualberta.med.biobank.common.wrappers;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ualberta.med.biobank.common.peer.SitePeer;
import edu.ualberta.med.biobank.common.util.RequestState;
import edu.ualberta.med.biobank.common.wrappers.WrapperTransaction.TaskList;
import edu.ualberta.med.biobank.common.wrappers.base.SiteBaseWrapper;
import edu.ualberta.med.biobank.common.wrappers.helpers.SiteQuery;
import edu.ualberta.med.biobank.model.Site;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class SiteWrapper extends SiteBaseWrapper {
    private static final String TOP_CONTAINER_COLLECTION_CACHE_KEY = "topContainerCollection";
    private static final String EXISTING_CHILDREN_MSG = "Unable to delete site {0}. All defined children (processing events, container types, and containers) must be removed first.";

    @SuppressWarnings("unused")
    private Map<RequestState, List<RequestWrapper>> requestCollectionMap = new HashMap<RequestState, List<RequestWrapper>>();

    public SiteWrapper(WritableApplicationService appService, Site wrappedObject) {
        super(appService, wrappedObject);
    }

    public SiteWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public List<ContainerTypeWrapper> getContainerTypeCollection() {
        return getContainerTypeCollection(false);
    }

    public List<ContainerWrapper> getContainerCollection() {
        return getContainerCollection(false);
    }

    @SuppressWarnings("unchecked")
    public List<ContainerWrapper> getTopContainerCollection(boolean sort)
        throws Exception {
        List<ContainerWrapper> topContainerCollection = (List<ContainerWrapper>) cache
            .get(TOP_CONTAINER_COLLECTION_CACHE_KEY);

        if (topContainerCollection == null) {
            topContainerCollection = SiteQuery.getTopContainerCollection(this);
            if (sort)
                Collections.sort(topContainerCollection);
            cache.put(TOP_CONTAINER_COLLECTION_CACHE_KEY,
                topContainerCollection);
        }
        return topContainerCollection;
    }

    public List<ContainerWrapper> getTopContainerCollection() throws Exception {
        return getTopContainerCollection(false);
    }

    public void clearTopContainerCollection() {
        cache.put(TOP_CONTAINER_COLLECTION_CACHE_KEY, null);
    }

    public Set<ClinicWrapper> getWorkingClinicCollection() {
        List<StudyWrapper> studies = getStudyCollection();
        Set<ClinicWrapper> clinics = new HashSet<ClinicWrapper>();
        for (StudyWrapper study : studies) {
            clinics.addAll(study.getClinicCollection());
        }
        return clinics;
    }

    @Override
    public List<StudyWrapper> getStudyCollection() {
        return getStudyCollection(true);
    }

    @Override
    protected void addDeleteTasks(TaskList tasks) {
        String errMsg = MessageFormat.format(EXISTING_CHILDREN_MSG, getName());
        tasks.add(check().empty(SitePeer.CONTAINER_COLLECTION, errMsg));
        tasks.add(check().empty(SitePeer.CONTAINER_TYPE_COLLECTION, errMsg));
        tasks.add(check().empty(SitePeer.PROCESSING_EVENT_COLLECTION, errMsg));

        super.addDeleteTasks(tasks);
    }
}
