package edu.ualberta.med.biobank.widgets.infotables;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientVisitWrapper;
import edu.ualberta.med.biobank.model.SiteClinicInfo;

public class ClinicInfoTable extends InfoTableWidget<ClinicWrapper> {

    private static final String[] HEADINGS = new String[] { "Name",
        "Studies Participated", "Status", "Patients", "Patient Visits" };

    private static final int[] BOUNDS = new int[] { 160, 130, 130, 130, 130 };

    public ClinicInfoTable(Composite parent,
        Collection<ClinicWrapper> collection) {
        super(parent, collection, HEADINGS, BOUNDS);
    }

    @Override
    public Object getCollectionModelObject(ClinicWrapper clinic)
        throws Exception {
        SiteClinicInfo info = new SiteClinicInfo();
        info.clinicWrapper = clinic;
        info.patientVisits = clinic.getPatientVisitCollection().size();
        info.activityStatus = clinic.getActivityStatus();
        info.studies = clinic.getStudyCollection().size();
        List<PatientVisitWrapper> pvs = clinic.getPatientVisitCollection();
        HashSet<Integer> patients = new HashSet<Integer>();
        for (int i = 0; i < pvs.size(); i++) {
            patients.add(pvs.get(i).getPatient().getId());
        }
        info.patients = patients.size();
        return info;
    }
}
