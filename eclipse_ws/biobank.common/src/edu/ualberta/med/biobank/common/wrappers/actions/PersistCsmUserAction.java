package edu.ualberta.med.biobank.common.wrappers.actions;

import org.hibernate.Session;

import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.model.User;
import edu.ualberta.med.biobank.server.applicationservice.BiobankCSMSecurityUtil;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.BiobankSessionException;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class PersistCsmUserAction extends WrapperAction<User> {
    private static final long serialVersionUID = 1L;

    private String password;

    public PersistCsmUserAction(ModelWrapper<User> wrapper, String password) {
        super(wrapper);
        this.password = password;
    }

    @Override
    public Object doAction(Session session) throws BiobankSessionException {
        try {
            Long csmUserId = BiobankCSMSecurityUtil.persistUser(getModel(),
                password);
            getModel().setCsmUserId(csmUserId);
        } catch (ApplicationException e) {
            throw new BiobankSessionException("Error persisting csm user", e); //$NON-NLS-1$
        }
        return null;
    }

}
