package edu.ualberta.med.biobank.common.permission.specimen;

import org.hibernate.Query;

import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.PermissionEnum;
import edu.ualberta.med.biobank.model.Specimen;
import edu.ualberta.med.biobank.model.Study;

public class SpecimenReadPermission implements Permission {
    private static final long serialVersionUID = 1L;

    private Integer specimenId = null;
    private String inventoryId = null;

    public SpecimenReadPermission(Integer specimenId) {
        this.specimenId = specimenId;
    }

    public SpecimenReadPermission(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    @Override
    public boolean isAllowed(ActionContext context) {
        Specimen specimen = null;
        if (specimenId != null)
            specimen = context.get(Specimen.class, specimenId);
        else {
            Query q =
                context
                    .getSession()
                    .createQuery(
                        "from "
                            + Specimen.class.getName()
                            + " spec inner join fetch spec.currentCenter inner join fetch "
                            + "spec.collectionEvent ce inner join fetch"
                            + " ce.patient p inner join fetch p.study where spec.inventoryId=?");
            q.setParameter(0, inventoryId);
            if (q.list().size() > 0)
                specimen = (Specimen) q.list().get(0);
        }

        if (specimen == null) return true;
        Center center = specimen.getCurrentCenter();
        Study study = specimen.getCollectionEvent().getPatient().getStudy();

        return PermissionEnum.SPECIMEN_READ.isAllowed(context.getUser(),
            center, study);
    }
}
