package edu.ualberta.med.biobank.common.wrappers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.ualberta.med.biobank.common.DatabaseResult;
import edu.ualberta.med.biobank.common.LabelingScheme;
import edu.ualberta.med.biobank.common.RowColPos;
import edu.ualberta.med.biobank.model.Capacity;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.ContainerPosition;
import edu.ualberta.med.biobank.model.ContainerType;
import edu.ualberta.med.biobank.model.Sample;
import edu.ualberta.med.biobank.model.SamplePosition;
import edu.ualberta.med.biobank.model.SampleType;
import edu.ualberta.med.biobank.model.Site;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

//FIXME to do by Delphine
public class ContainerWrapper extends ModelWrapper<Container> {

    public ContainerWrapper(WritableApplicationService appService,
        Container wrappedObject) {
        super(appService, wrappedObject);
    }

    @Override
    protected void firePropertyChanges(Container oldWrappedObject,
        Container newWrappedObject) {
        propertyChangeSupport.firePropertyChange("productBarcode",
            oldWrappedObject, newWrappedObject);
        propertyChangeSupport.firePropertyChange("position", oldWrappedObject,
            newWrappedObject);
        propertyChangeSupport.firePropertyChange("activityStatus",
            oldWrappedObject, newWrappedObject);
        propertyChangeSupport.firePropertyChange("site", oldWrappedObject,
            newWrappedObject);
        propertyChangeSupport.firePropertyChange("label", oldWrappedObject,
            newWrappedObject);
        propertyChangeSupport.firePropertyChange("temperature",
            oldWrappedObject, newWrappedObject);
        propertyChangeSupport.firePropertyChange("comment", oldWrappedObject,
            newWrappedObject);
        propertyChangeSupport.firePropertyChange("samplePositionCollection",
            oldWrappedObject, newWrappedObject);
        propertyChangeSupport.firePropertyChange("childPositionCollection",
            oldWrappedObject, newWrappedObject);
        propertyChangeSupport.firePropertyChange("containerType",
            oldWrappedObject, newWrappedObject);
    }

    @Override
    protected DatabaseResult persistChecks() throws ApplicationException {
        return checkContainerUnique();
    }

    @Override
    protected Class<Container> getWrappedClass() {
        return Container.class;
    }

    public SiteWrapper getSiteWrapper() {
        return new SiteWrapper(appService, wrappedObject.getSite());
    }

    public String getLabel() {
        return wrappedObject.getLabel();
    }

    public String getProductBarcode() {
        return wrappedObject.getProductBarcode();
    }

    public void setProductBarcode(String barcode) {
        String oldBarcode = getProductBarcode();
        wrappedObject.setProductBarcode(barcode);
        propertyChangeSupport.firePropertyChange("productBarcode", oldBarcode,
            barcode);
    }

    /**
     * get the container with label label and type container type and from same
     * site that this containerWrapper
     * 
     * @param label label of the container
     * @param containerType the type of the container
     * @throws ApplicationException
     */
    public Container getContainer(String label, ContainerType containerType)
        throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria("from "
            + Container.class.getName()
            + " where site = ? and label = ? and containerType = ?", Arrays
            .asList(new Object[] { wrappedObject.getSite(), label,
                containerType }));
        List<Container> containers = appService.query(criteria);
        if (containers.size() == 1) {
            return containers.get(0);
        }
        return null;
    }

    /**
     * get the containers with label label and from same site that this
     * containerWrapper
     * 
     * @param label label of the container
     * @throws ApplicationException
     */
    public List<Container> getContainers(String label)
        throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria("from "
            + Container.class.getName() + " where site = ? and label = ?",
            Arrays.asList(new Object[] { wrappedObject.getSite(), label }));
        return appService.query(criteria);
    }

    /**
     * get the containers with label label, with site site and container type
     * type
     */
    public static List<Container> getContainersHoldingContainerType(
        WritableApplicationService appService, String label, Site site,
        ContainerType type) throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria(
            "from "
                + Container.class.getName()
                + " where site = ? and label = ? and containerType in (select parent from "
                + ContainerType.class.getName()
                + " as parent where parent.id in (select ct.id" + " from "
                + ContainerType.class.getName() + " as ct"
                + " left join ct.childContainerTypeCollection as child "
                + " where child = ?))", Arrays.asList(new Object[] { site,
                label, type }));
        return appService.query(criteria);
    }

    /**
     * get the containers with label label and from same site that this
     * containerWrapper and holding this container type
     * 
     * @param label label of the container
     * @throws ApplicationException
     */
    public List<Container> getContainersHoldingContainerType(String label)
        throws ApplicationException {
        return getContainersHoldingContainerType(appService, label,
            wrappedObject.getSite(), getContainerType());
    }

    /**
     * get the containers with label label and from same site that this
     * containerWrapper and holding sample type
     */
    public static List<Container> getContainersHoldingSampleType(
        WritableApplicationService appService, SiteWrapper siteWrapper,
        String label, SampleType sampleType) throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria(
            "from "
                + Container.class.getName()
                + " where site = ? and label = ? and containerType in (select parent from "
                + ContainerType.class.getName()
                + " as parent where parent.id in (select ct.id" + " from "
                + ContainerType.class.getName() + " as ct"
                + " left join ct.sampleTypeCollection as sampleType "
                + " where sampleType = ?))", Arrays.asList(new Object[] {
                siteWrapper.getWrappedObject(), label, sampleType }));
        return appService.query(criteria);
    }

    /**
     * Get all containers form a given site with a given label
     */
    public static List<Container> getContainersInSite(
        WritableApplicationService appService, SiteWrapper siteWrapper,
        String label) throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria("from "
            + Container.class.getName() + " where site = ? and label = ?",
            Arrays
                .asList(new Object[] { siteWrapper.getWrappedClass(), label }));
        return appService.query(criteria);
    }

    /**
     * compute the ContainerPosition for this container using its label. If the
     * parent container cannot hold the container type of this container, then
     * an exception is launched
     */
    public void computePositionFromLabel() throws Exception {
        String parentContainerLabel = getLabel().substring(0,
            getLabel().length() - 2);
        List<Container> containersWithLabel = getContainersHoldingContainerType(parentContainerLabel);
        if (containersWithLabel.size() == 0) {
            throw new Exception("Can't find container with label "
                + parentContainerLabel + " holding containers of type "
                + getContainerType().getName());
        }
        if (containersWithLabel.size() > 1) {
            throw new Exception(
                containersWithLabel.size()
                    + " containers with label "
                    + parentContainerLabel
                    + " and holding container types "
                    + getContainerType().getName()
                    + " have been found. This is ambiguous: check containers definitions.");
        }
        // has the parent container. Can now find the position using the
        // parent labelling scheme
        ContainerPosition position = getPosition();
        if (position == null) {
            position = new ContainerPosition();
        }
        ContainerPositionWrapper positionWrapper = new ContainerPositionWrapper(
            appService, position);
        positionWrapper.setParentContainer(containersWithLabel.get(0));
        positionWrapper.setPosition(getLabel().substring(
            getLabel().length() - 2));
        setPosition(positionWrapper.getWrappedObject());
    }

    /**
     * Get the child container of this container with label label
     */
    public Container getChildWithLabel(String label)
        throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria("from "
            + Container.class.getName()
            + " where position.parentContainer = ? and label = ?", Arrays
            .asList(new Object[] { wrappedObject, label }));

        List<Container> containers = appService.query(criteria);
        if (containers.size() == 1) {
            return containers.get(0);
        }
        return null;
    }

    /**
     * position is 2 letters, or 2 number or 1 letter and 1 number... this
     * position string is used to get the correct row and column index the given
     * position String.
     * 
     * @throws Exception
     */
    public RowColPos getPositionFromLabelingScheme(String position)
        throws Exception {
        ContainerType type = getContainerType();
        RowColPos rcp = LabelingScheme.getRowColFromPositionString(position,
            type);
        Capacity capacity = type.getCapacity();
        if (rcp.row < capacity.getRowCapacity()
            && rcp.col < capacity.getColCapacity()) {
            return rcp;
        }
        throw new Exception("Can't use position " + position + " in container "
            + getFullInfoLabel() + "\nReason: capacity = "
            + capacity.getRowCapacity() + "*" + capacity.getColCapacity());
    }

    public void setContainerType(ContainerType containerType) {
        ContainerType oldType = getContainerType();
        wrappedObject.setContainerType(containerType);
        propertyChangeSupport.firePropertyChange("containerType", oldType,
            containerType);
    }

    public ContainerType getContainerType() {
        return wrappedObject.getContainerType();
    }

    public ContainerPosition getPosition() {
        return wrappedObject.getPosition();
    }

    public void setPosition(ContainerPosition position) {
        ContainerPosition oldPosition = position;
        wrappedObject.setPosition(position);
        propertyChangeSupport.firePropertyChange("position", oldPosition,
            position);
    }

    public void setActivityStatus(String activityStatus) {
        String oldActivityStatus = getActivityStatus();
        wrappedObject.setActivityStatus(activityStatus);
        propertyChangeSupport.firePropertyChange("activityStatus",
            oldActivityStatus, activityStatus);
    }

    public String getActivityStatus() {
        return wrappedObject.getActivityStatus();
    }

    public void setSite(SiteWrapper siteWrapper) {
        Site oldSite = wrappedObject.getSite();
        wrappedObject.setSite(siteWrapper.getWrappedObject());
        propertyChangeSupport.firePropertyChange("site", oldSite, siteWrapper
            .getWrappedObject());
    }

    public void setLabel(String label) {
        String oldLabel = getLabel();
        wrappedObject.setLabel(label);
        propertyChangeSupport.firePropertyChange("label", oldLabel, label);
    }

    public Collection<SamplePosition> getSamplePositionCollection() {
        return wrappedObject.getSamplePositionCollection();
    }

    public void setSamplePositionCollection(Collection<SamplePosition> positions) {
        Collection<SamplePosition> oldPositions = wrappedObject
            .getSamplePositionCollection();
        wrappedObject.setSamplePositionCollection(positions);
        propertyChangeSupport.firePropertyChange("samplePositionCollection",
            oldPositions, positions);
    }

    /**
     * return a string with the label of this container + the short name of its
     * type
     */
    public String getFullInfoLabel() {
        if (getContainerType() == null
            || getContainerType().getNameShort() == null) {
            return getLabel();
        }
        return getLabel() + "(" + getContainerType().getNameShort() + ")";
    }

    /**
     * Get the container with the given productBarcode in a site
     */
    public static Container getContainerWithProductBarcodeInSite(
        WritableApplicationService appService, SiteWrapper siteWrapper,
        String productBarcode) throws Exception {
        HQLCriteria criteria = new HQLCriteria("from "
            + Container.class.getName()
            + " where site = ? and productBarcode = ?", Arrays
            .asList(new Object[] { siteWrapper.getWrappedObject(),
                productBarcode }));
        List<Container> containers = appService.query(criteria);
        if (containers.size() == 0) {
            return null;
        } else if (containers.size() > 1) {
            throw new Exception(
                "Multiples containers registered with product barcode "
                    + productBarcode);
        }
        return containers.get(0);
    }

    public void setTemperature(Double temperature) {
        Double oldTemp = getTemperature();
        wrappedObject.setTemperature(temperature);
        propertyChangeSupport.firePropertyChange("temperature", oldTemp,
            temperature);
    }

    public Double getTemperature() {
        return getWrappedObject().getTemperature();
    }

    public Collection<ContainerPosition> getChildPositionCollection() {
        return getWrappedObject().getChildPositionCollection();
    }

    public void setChildPositionCollection(
        Collection<ContainerPosition> positions) {
        Collection<ContainerPosition> oldPositions = wrappedObject
            .getChildPositionCollection();
        wrappedObject.setChildPositionCollection(positions);
        propertyChangeSupport.firePropertyChange("childPositionCollection",
            oldPositions, positions);
    }

    /**
     * Return true if this container can hold the type of sample
     */
    public boolean canHold(Sample sample) throws ApplicationException {
        SampleType type = sample.getSampleType();
        HQLCriteria criteria = new HQLCriteria("select sampleType from "
            + ContainerType.class.getName()
            + " as ct inner join ct.sampleTypeCollection as sampleType"
            + " where ct = ? and sampleType = ?", Arrays.asList(new Object[] {
            getContainerType(), type }));
        List<SampleType> types = appService.query(criteria);
        return types.size() == 1;
    }

    public String getComment() {
        return wrappedObject.getComment();
    }

    public void setComment(String comment) {
        String oldComment = wrappedObject.getComment();
        wrappedObject.setComment(comment);
        propertyChangeSupport
            .firePropertyChange("comment", oldComment, comment);
    }

    @Override
    public boolean checkIntegrity() {
        if (wrappedObject != null)
            if ((getContainerType() != null
                && getContainerType().getCapacity() != null
                && getContainerType().getCapacity().getRowCapacity() != null && getContainerType()
                .getCapacity().getColCapacity() != null)
                || getContainerType() == null)
                if ((getPosition() != null && getPosition().getRow() != null && getPosition()
                    .getCol() != null)
                    || getPosition() == null)
                    if (wrappedObject.getSite() != null)
                        return true;
        return false;

    }

    private DatabaseResult checkContainerUnique() throws ApplicationException {
        // FIXME set constraint directly into the model ?
        HQLCriteria criteria;
        if (getPosition() == null) {
            criteria = new HQLCriteria("from " + Container.class.getName()
                + " where site=? and position=null and label=? "
                + "and containerType=?", Arrays.asList(new Object[] {
                wrappedObject.getSite(), getLabel(), getContainerType() }));
            List<Object> results = appService.query(criteria);
            if (results.size() > 0) {
                return new DatabaseResult("A container with label \""
                    + getLabel() + "\" and type \""
                    + getContainerType().getName() + "\" already exists.");
            }
        }
        criteria = new HQLCriteria("from " + Container.class.getName()
            + " as where site=? and productBarcode=?",
            Arrays.asList(new Object[] { wrappedObject.getSite(),
                getProductBarcode() }));

        List<Object> results = appService.query(criteria);
        if (results.size() > 0) {
            return new DatabaseResult("A container with product barcode \""
                + getProductBarcode() + "\" already exists.");
        }
        return DatabaseResult.OK;
    }

    @Override
    protected DatabaseResult deleteChecks() throws ApplicationException {
        if (getSamplePositionCollection().size() > 0
            || getChildPositionCollection().size() > 0) {
            return new DatabaseResult("Unable to delete container "
                + getLabel()
                + ". All subcontainers/samples must be removed first.");
        }
        return DatabaseResult.OK;
    }
}
