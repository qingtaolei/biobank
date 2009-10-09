package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.model.Address;
import edu.ualberta.med.biobank.model.Clinic;
import edu.ualberta.med.biobank.model.Contact;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.model.Study;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class ClinicWrapper extends ModelWrapper<Clinic> {

    public ClinicWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public ClinicWrapper(WritableApplicationService appService,
        Clinic wrappedObject) {
        super(appService, wrappedObject);
    }

    @Override
    protected String[] getPropertyChangesNames() {
        return new String[] { "name", "activityStatus", "comment", "address",
            "site", "contactCollection" };
    }

    public AddressWrapper getAddress() {
        Address address = wrappedObject.getAddress();
        if (address == null) {
            return null;
        }
        return new AddressWrapper(appService, address);
    }

    public void setAddress(Address address) {
        Address oldAddress = wrappedObject.getAddress();
        wrappedObject.setAddress(address);
        propertyChangeSupport
            .firePropertyChange("address", oldAddress, address);
    }

    public void setAddress(AddressWrapper study) {
        setAddress(study.wrappedObject);
    }

    public String getName() {
        return wrappedObject.getName();
    }

    public void setName(String name) {
        String oldName = getName();
        wrappedObject.setName(name);
        propertyChangeSupport.firePropertyChange("name", oldName, name);
    }

    public String getActivityStatus() {
        return wrappedObject.getActivityStatus();
    }

    public void setActivityStatus(String activityStatus) {
        String oldStatus = getActivityStatus();
        wrappedObject.setActivityStatus(activityStatus);
        propertyChangeSupport.firePropertyChange("activityStatus", oldStatus,
            activityStatus);
    }

    public String getComment() {
        return wrappedObject.getComment();
    }

    public void setComment(String comment) {
        String oldComment = getComment();
        wrappedObject.setName(comment);
        propertyChangeSupport
            .firePropertyChange("comment", oldComment, comment);
    }

    public Site getSite() {
        return wrappedObject.getSite();
    }

    public void setSiteWrapper(SiteWrapper siteWrapper) {
        Site oldSite = wrappedObject.getSite();
        Site newSite = siteWrapper.getWrappedObject();
        wrappedObject.setSite(newSite);
        propertyChangeSupport.firePropertyChange("site", oldSite, newSite);
    }

    @Override
    protected void persistChecks() throws BiobankCheckException, Exception {
        if (!checkClinicNameUnique()) {
            throw new BiobankCheckException("A clinic with name \"" + getName()
                + "\" already exists.");
        }
    }

    public boolean checkClinicNameUnique() throws ApplicationException {
        HQLCriteria c;

        if (getWrappedObject().getId() == null) {
            c = new HQLCriteria("from " + Clinic.class.getName()
                + " where name = ?", Arrays.asList(new Object[] { getName() }));
        } else {
            c = new HQLCriteria("from " + Clinic.class.getName()
                + " as clinic where site = ? and name = ? and clinic <> ?",
                Arrays.asList(new Object[] { getSite(), getName(),
                    getWrappedObject() }));
        }

        List<Clinic> results = appService.query(c);
        return (results.size() == 0);
    }

    @Override
    public Class<Clinic> getWrappedClass() {
        return Clinic.class;
    }

    @SuppressWarnings("unchecked")
    public Collection<ContactWrapper> getContactCollection(boolean sort) {
        List<ContactWrapper> contactCollection = (List<ContactWrapper>) propertiesMap
            .get("contactCollection");
        if (contactCollection == null) {
            Collection<Contact> children = wrappedObject.getContactCollection();
            if (children != null) {
                contactCollection = new ArrayList<ContactWrapper>();
                for (Contact type : children) {
                    contactCollection.add(new ContactWrapper(appService, type));
                }
                propertiesMap.put("contactCollection", contactCollection);
            }
        }
        if ((contactCollection != null) && sort)
            Collections.sort(contactCollection);
        return contactCollection;
    }

    public Collection<ContactWrapper> getContactCollection() {
        return getContactCollection(false);
    }

    public void setContactCollection(Collection<Contact> contacts,
        boolean setNull) {
        Collection<Contact> oldContacts = wrappedObject.getContactCollection();
        wrappedObject.setContactCollection(contacts);
        propertyChangeSupport.firePropertyChange("contactCollection",
            oldContacts, contacts);
        if (setNull) {
            propertiesMap.put("contactCollection", null);
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<StudyWrapper> getStudyCollection(boolean sort)
        throws Exception {
        List<StudyWrapper> studyCollection = (List<StudyWrapper>) propertiesMap
            .get("studyCollection");

        if (studyCollection == null) {
            studyCollection = new ArrayList<StudyWrapper>();
            HQLCriteria c = new HQLCriteria("select distinct studies from "
                + Contact.class.getName() + " as contacts"
                + " inner join contacts.studyCollection as studies"
                + " where contacts.clinic = ?", Arrays
                .asList(new Object[] { wrappedObject }));
            List<Study> collection = appService.query(c);
            for (Study study : collection) {
                studyCollection.add(new StudyWrapper(appService, study));
            }
            if (sort)
                Collections.sort(studyCollection);
            propertiesMap.put("studyCollection", studyCollection);
        }
        return studyCollection;
    }

    @Override
    protected void deleteChecks() throws BiobankCheckException, Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public int compareTo(ModelWrapper<Clinic> wrapper) {
        String myName = wrappedObject.getName();
        String wrapperName = wrapper.wrappedObject.getName();
        return ((myName.compareTo(wrapperName) > 0) ? 1 : (myName
            .equals(wrapperName) ? 0 : -1));
    }

}
