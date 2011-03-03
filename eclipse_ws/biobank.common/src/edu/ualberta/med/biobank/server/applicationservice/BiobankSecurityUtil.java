package edu.ualberta.med.biobank.server.applicationservice;

import edu.ualberta.med.biobank.common.security.ProtectionGroupPrivilege;
import edu.ualberta.med.biobank.model.Site;
import gov.nih.nci.security.SecurityServiceProvider;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authentication.LockoutManager;
import gov.nih.nci.security.authorization.domainobjects.Application;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.Privilege;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElementPrivilegeContext;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroupRoleContext;
import gov.nih.nci.security.authorization.domainobjects.Role;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.dao.GroupSearchCriteria;
import gov.nih.nci.security.dao.ProtectionElementSearchCriteria;
import gov.nih.nci.security.dao.ProtectionGroupSearchCriteria;
import gov.nih.nci.security.dao.RoleSearchCriteria;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import gov.nih.nci.security.exceptions.CSTransactionException;
import gov.nih.nci.system.applicationservice.ApplicationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.log4j.Logger;

public class BiobankSecurityUtil {

    private static Logger log = Logger.getLogger(BiobankSecurityUtil.class
        .getName());

    public static final String SITE_CLASS_NAME = "edu.ualberta.med.biobank.model.Site";

    public static final String APPLICATION_CONTEXT_NAME = "biobank2";

    public static final String ALL_SITES_PG_ID = "11";

    public static void modifyPassword(String oldPassword, String newPassword)
        throws ApplicationException {
        try {
            UserProvisioningManager upm = SecurityServiceProvider
                .getUserProvisioningManager(BiobankSecurityUtil.APPLICATION_CONTEXT_NAME);

            Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
            String userLogin = authentication.getName();
            if (!oldPassword.equals(authentication.getCredentials())) {
                throw new ApplicationException(
                    "Cannot modify password: verification password is incorrect.");
            }
            if (oldPassword.equals(newPassword)) {
                throw new ApplicationException(
                    "New password needs to be different from the old one.");
            }
            User user = upm.getUser(userLogin);
            user.setPassword(newPassword);
            user.setStartDate(null);
            upm.modifyUser(user);
        } catch (ApplicationException ae) {
            log.error("Error modifying password", ae);
            throw ae;
        } catch (Exception ex) {
            log.error("Error modifying password", ex);
            throw new ApplicationException(ex);
        }
    }

    public static List<edu.ualberta.med.biobank.common.security.Group> getSecurityGroups()
        throws ApplicationException {
        if (isWebsiteAdministrator()) {
            try {
                UserProvisioningManager upm = SecurityServiceProvider
                    .getUserProvisioningManager(BiobankSecurityUtil.APPLICATION_CONTEXT_NAME);
                List<edu.ualberta.med.biobank.common.security.Group> list = new ArrayList<edu.ualberta.med.biobank.common.security.Group>();
                for (Object object : upm.getObjects(new GroupSearchCriteria(
                    new Group()))) {
                    list.add(createGroup(upm, (Group) object));
                }
                return list;
            } catch (Exception ex) {
                log.error("Error retrieving security groups", ex);
                throw new ApplicationException(ex);
            }
        } else {
            throw new ApplicationException(
                "Only Website Administrators can retrieve security groups");
        }
    }

    private static edu.ualberta.med.biobank.common.security.Group createGroup(
        UserProvisioningManager upm, Group group)
        throws CSObjectNotFoundException {
        edu.ualberta.med.biobank.common.security.Group biobankGroup = new edu.ualberta.med.biobank.common.security.Group(
            group.getGroupId(), group.getGroupName());
        biobankGroup.setReadOnlySites(new ArrayList<Integer>());
        biobankGroup.setCanUpdateSites(new ArrayList<Integer>());
        Set<?> pepcList = upm
            .getProtectionElementPrivilegeContextForGroup(group.getGroupId()
                .toString());
        for (Object o : pepcList) {
            ProtectionElementPrivilegeContext pepc = (ProtectionElementPrivilegeContext) o;
            ProtectionElement pe = pepc.getProtectionElement();
            Set<edu.ualberta.med.biobank.common.security.Privilege> privileges = new HashSet<edu.ualberta.med.biobank.common.security.Privilege>();
            for (Object r : pepc.getPrivileges()) {
                Privilege csmPrivilege = (Privilege) r;
                privileges
                    .add(edu.ualberta.med.biobank.common.security.Privilege
                        .valueOf(csmPrivilege.getName()));
            }
            String type = pe.getObjectId();
            String id = null;
            if ("id".equals(pe.getAttribute())) {
                id = pe.getValue();
            }
            biobankGroup.addProtectionElementPrivilege(type, id, privileges);
            if (type.equals(Site.class.getName()) && id != null) {
                if (privileges
                    .contains(edu.ualberta.med.biobank.common.security.Privilege.UPDATE)) {
                    biobankGroup.getCanUpdateSites().add(new Integer(id));
                } else if (privileges
                    .contains(edu.ualberta.med.biobank.common.security.Privilege.READ)) {
                    biobankGroup.getReadOnlySites().add(new Integer(id));
                }
            }
        }

        Set<?> pgrcList = upm.getProtectionGroupRoleContextForGroup(group
            .getGroupId().toString());
        for (Object o : pgrcList) {
            ProtectionGroupRoleContext pgrc = (ProtectionGroupRoleContext) o;
            ProtectionGroup pg = pgrc.getProtectionGroup();
            Set<edu.ualberta.med.biobank.common.security.Privilege> privileges = new HashSet<edu.ualberta.med.biobank.common.security.Privilege>();
            boolean containsFullAccessObject = false;
            for (Object r : pgrc.getRoles()) {
                Role role = (Role) r;
                if (role
                    .getName()
                    .equals(
                        edu.ualberta.med.biobank.common.security.Group.OBJECT_FULL_ACCESS))
                    containsFullAccessObject = true;
                for (Object p : upm.getPrivileges(role.getId().toString())) {
                    Privilege csmPrivilege = (Privilege) p;
                    privileges
                        .add(edu.ualberta.med.biobank.common.security.Privilege
                            .valueOf(csmPrivilege.getName()));
                }
            }
            biobankGroup.addProtectionGroupPrivilege(
                pg.getProtectionGroupName(), privileges);
            if (edu.ualberta.med.biobank.common.security.Group.PG_SITE_ADMINISTRATION_ID
                .equals(pg.getProtectionGroupId()) && containsFullAccessObject)
                biobankGroup.setIsSiteAdministrator(true);
        }
        return biobankGroup;
    }

    public static List<edu.ualberta.med.biobank.common.security.User> getSecurityUsers()
        throws ApplicationException {
        if (isWebsiteAdministrator()) {
            try {
                UserProvisioningManager upm = SecurityServiceProvider
                    .getUserProvisioningManager(BiobankSecurityUtil.APPLICATION_CONTEXT_NAME);

                List<edu.ualberta.med.biobank.common.security.User> list = new ArrayList<edu.ualberta.med.biobank.common.security.User>();
                Map<Long, User> users = new HashMap<Long, User>();

                for (Object g : upm.getObjects(new GroupSearchCriteria(
                    new Group()))) {
                    Group group = (Group) g;
                    for (Object u : upm.getUsers(group.getGroupId().toString())) {
                        User user = (User) u;
                        if (!users.containsKey(user.getUserId())) {
                            list.add(createUser(upm, user));
                            users.put(user.getUserId(), user);
                        }
                    }
                }
                return list;
            } catch (Exception ex) {
                log.error("Error retrieving security users", ex);
                throw new ApplicationException(ex);
            }
        } else {
            throw new ApplicationException(
                "Only Website Administrators can retrieve security users");
        }
    }

    public static void persistUser(
        edu.ualberta.med.biobank.common.security.User user)
        throws ApplicationException {
        if (isWebsiteAdministrator()) {
            try {
                UserProvisioningManager upm = SecurityServiceProvider
                    .getUserProvisioningManager(BiobankSecurityUtil.APPLICATION_CONTEXT_NAME);
                if (user.getLogin() == null) {
                    throw new ApplicationException("Login should be set");
                }

                User serverUser = null;
                if (user.getId() != null) {
                    serverUser = upm.getUserById(user.getId().toString());
                }
                if (serverUser == null) {
                    serverUser = new User();
                }

                serverUser.setLoginName(user.getLogin());
                serverUser.setFirstName(user.getFirstName());
                serverUser.setLastName(user.getLastName());
                serverUser.setEmailId(user.getEmail());

                String password = user.getPassword();
                if (password != null && !password.isEmpty()) {
                    serverUser.setPassword(password);
                }

                if (user.passwordChangeRequired()) {
                    serverUser.setStartDate(new Date());
                }

                Set<Group> groups = new HashSet<Group>();
                for (edu.ualberta.med.biobank.common.security.Group groupDto : user
                    .getGroups()) {
                    Group g = upm.getGroupById(groupDto.getId().toString());
                    if (g == null) {
                        throw new ApplicationException("Invalid group "
                            + groupDto + " user groups.");
                    }
                    groups.add(g);
                }
                if (groups.size() == 0) {
                    throw new ApplicationException(
                        "No group has been set for this user.");
                }
                serverUser.setGroups(groups);
                if (serverUser.getUserId() == null) {
                    upm.createUser(serverUser);
                } else {
                    upm.modifyUser(serverUser);
                }
            } catch (ApplicationException ae) {
                log.error("Error persisting security user", ae);
                throw ae;
            } catch (Exception ex) {
                log.error("Error persisting security user", ex);
                throw new ApplicationException(ex.getMessage(), ex);
            }
        } else {
            throw new ApplicationException(
                "Only Website Administrators can add/modify users");
        }
    }

    public static void deleteUser(String login) throws ApplicationException {
        if (isWebsiteAdministrator()) {
            try {
                UserProvisioningManager upm = SecurityServiceProvider
                    .getUserProvisioningManager(BiobankSecurityUtil.APPLICATION_CONTEXT_NAME);
                String currentLogin = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
                if (currentLogin.equals(login)) {
                    throw new ApplicationException("User cannot delete himself");
                }
                User serverUser = upm.getUser(login);
                if (serverUser == null) {
                    throw new ApplicationException("Security user " + login
                        + " not found.");
                }
                upm.removeUser(serverUser.getUserId().toString());
            } catch (ApplicationException ae) {
                log.error("Error deleting security user", ae);
                throw ae;
            } catch (Exception ex) {
                log.error("Error deleting security user", ex);
                throw new ApplicationException(ex.getMessage(), ex);
            }
        } else {
            throw new ApplicationException(
                "Only Website Administrators can delete users");
        }
    }

    public static edu.ualberta.med.biobank.common.security.User getCurrentUser()
        throws ApplicationException {
        try {
            UserProvisioningManager upm = SecurityServiceProvider
                .getUserProvisioningManager(BiobankSecurityUtil.APPLICATION_CONTEXT_NAME);

            Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
            String userLogin = authentication.getName();
            User serverUser = upm.getUser(userLogin);
            if (serverUser == null)
                throw new ApplicationException("Problem with user retrieval");
            return createUser(upm, serverUser);
        } catch (ApplicationException ae) {
            log.error("Error getting current user", ae);
            throw ae;
        } catch (Exception ex) {
            log.error("Error getting current user", ex);
            throw new ApplicationException(ex.getMessage(), ex);
        }
    }

    private static edu.ualberta.med.biobank.common.security.User createUser(
        UserProvisioningManager upm, User serverUser)
        throws CSObjectNotFoundException {
        edu.ualberta.med.biobank.common.security.User userDTO = new edu.ualberta.med.biobank.common.security.User();
        userDTO.setId(serverUser.getUserId());
        userDTO.setLogin(serverUser.getLoginName());
        userDTO.setFirstName(serverUser.getFirstName());
        userDTO.setLastName(serverUser.getLastName());
        userDTO.setEmail(serverUser.getEmailId());
        userDTO.setLockedOut(LockoutManager.getInstance().isUserLockedOut(
            serverUser.getLoginName()));

        if (serverUser.getStartDate() != null) {
            userDTO.setNeedToChangePassword(true);
        }

        List<edu.ualberta.med.biobank.common.security.Group> groups = new ArrayList<edu.ualberta.med.biobank.common.security.Group>();
        for (Object o : upm.getGroups(serverUser.getUserId().toString())) {
            groups.add(createGroup(upm, (Group) o));
        }
        userDTO.setGroups(groups);
        return userDTO;
    }

    public static void unlockUser(String userName) throws ApplicationException {
        if (isWebsiteAdministrator()) {
            LockoutManager.getInstance().unLockUser(userName);
        }
    }

    public static edu.ualberta.med.biobank.common.security.Group persistGroup(
        edu.ualberta.med.biobank.common.security.Group group)
        throws ApplicationException {
        if (isWebsiteAdministrator()) {
            try {
                UserProvisioningManager upm = SecurityServiceProvider
                    .getUserProvisioningManager(BiobankSecurityUtil.APPLICATION_CONTEXT_NAME);
                if (!group.canBeEdited()) {
                    throw new ApplicationException(
                        "This group cannot be modified.");
                }
                if (group.getName() == null) {
                    throw new ApplicationException("Name should be set.");
                }

                Group serverGroup = null;
                if (group.getId() != null) {
                    serverGroup = upm.getGroupById(group.getId().toString());
                }
                if (serverGroup == null) {
                    serverGroup = new Group();
                }
                serverGroup.setGroupName(group.getName());
                if (serverGroup.getGroupId() == null) {
                    upm.createGroup(serverGroup);
                } else {
                    upm.modifyGroup(serverGroup);
                }
                // Default is Read Only
                addPGRoleAssociationToGroup(upm, serverGroup, 1L,
                    edu.ualberta.med.biobank.common.security.Group.READ_ONLY);
                if (group.getIsSiteAdministrator()) {
                    addPGRoleAssociationToGroup(
                        upm,
                        serverGroup,
                        edu.ualberta.med.biobank.common.security.Group.PG_SITE_ADMINISTRATION_ID,
                        edu.ualberta.med.biobank.common.security.Group.OBJECT_FULL_ACCESS);
                }
                for (Integer siteId : group.getCanUpdateSites()) {
                    setSiteSecurityForGroup(
                        upm,
                        serverGroup,
                        siteId,
                        edu.ualberta.med.biobank.common.security.Group.SITE_FULL_ACCESS);
                }
                for (Integer siteId : group.getReadOnlySites()) {
                    if (!group.getCanUpdateSites().contains(siteId))
                        setSiteSecurityForGroup(
                            upm,
                            serverGroup,
                            siteId,
                            edu.ualberta.med.biobank.common.security.Group.READ_ONLY);
                }
                for (Integer pgId : group.getFeaturesEnabled()) {
                    addPGRoleAssociationToGroup(
                        upm,
                        serverGroup,
                        pgId.longValue(),
                        edu.ualberta.med.biobank.common.security.Group.OBJECT_FULL_ACCESS);
                }
                return createGroup(upm, serverGroup);
            } catch (ApplicationException ae) {
                log.error("Error persisting security group", ae);
                throw ae;
            } catch (Exception ex) {
                log.error("Error persisting security group", ex);
                throw new ApplicationException(ex.getMessage(), ex);
            }
        } else {
            throw new ApplicationException(
                "Only Website Administrators can add/modify groups");
        }
    }

    @SuppressWarnings("unchecked")
    private static void setSiteSecurityForGroup(UserProvisioningManager upm,
        Group serverGroup, Integer siteId, String roleName)
        throws ApplicationException, CSTransactionException,
        CSObjectNotFoundException {
        ProtectionElement pe = new ProtectionElement();
        pe.setObjectId(Site.class.getName());
        pe.setAttribute("id");
        pe.setValue(siteId.toString());
        ProtectionElementSearchCriteria c = new ProtectionElementSearchCriteria(
            pe);
        List<?> peList = upm.getObjects(c);
        if (peList.size() != 1)
            throw new ApplicationException(
                "Problem with site protection element for id=" + siteId);
        pe = (ProtectionElement) peList.get(0);
        Set<ProtectionGroup> pgs = upm.getProtectionGroups(pe
            .getProtectionElementId().toString());
        if (pgs.size() != 1)
            throw new ApplicationException(
                "Problem with protection group for site with id=" + siteId);
        addPGRoleAssociationToGroup(upm, serverGroup, pgs.iterator().next()
            .getProtectionGroupId(), roleName);
    }

    private static void addPGRoleAssociationToGroup(
        UserProvisioningManager upm, Group serverGroup, Long protectionGroupID,
        String roleName) throws ApplicationException, CSTransactionException {
        Role role = new Role();
        role.setName(roleName);
        List<?> roles = upm.getObjects(new RoleSearchCriteria(role));
        if (roles.size() != 1)
            throw new ApplicationException("Problem getting role " + roleName);
        role = (Role) roles.get(0);
        upm.assignGroupRoleToProtectionGroup(protectionGroupID.toString(),
            serverGroup.getGroupId().toString(), new String[] { role.getId()
                .toString() });
    }

    public static void deleteGroup(
        edu.ualberta.med.biobank.common.security.Group group)
        throws ApplicationException {
        if (isWebsiteAdministrator()) {
            try {
                UserProvisioningManager upm = SecurityServiceProvider
                    .getUserProvisioningManager(BiobankSecurityUtil.APPLICATION_CONTEXT_NAME);
                if (group.canBeDeleted()) {
                    Group serverGroup = upm.getGroupById(group.getId()
                        .toString());
                    if (serverGroup == null) {
                        throw new ApplicationException("Security group "
                            + group.getName() + " not found.");
                    }
                    upm.removeGroup(serverGroup.getGroupId().toString());
                } else {
                    throw new ApplicationException("Deletion of group "
                        + group.getName() + " is not authorized.");
                }
            } catch (ApplicationException ae) {
                log.error("Error deleting security group", ae);
                throw ae;
            } catch (Exception ex) {
                log.error("Error deleting security group", ex);
                throw new ApplicationException(ex.getMessage(), ex);
            }
        } else {
            throw new ApplicationException(
                "Only Website Administrators can delete groups");
        }
    }

    public static List<ProtectionGroupPrivilege> getSecurityFeatures()
        throws ApplicationException {
        if (isWebsiteAdministrator()) {
            try {
                UserProvisioningManager upm = SecurityServiceProvider
                    .getUserProvisioningManager(BiobankSecurityUtil.APPLICATION_CONTEXT_NAME);
                ProtectionGroup pg = new ProtectionGroup();
                pg.setProtectionGroupName("%Feature");
                List<ProtectionGroupPrivilege> features = new ArrayList<ProtectionGroupPrivilege>();
                for (Object object : upm
                    .getObjects(new ProtectionGroupSearchCriteria(pg))) {
                    ProtectionGroup pgFeature = (ProtectionGroup) object;
                    features.add(new ProtectionGroupPrivilege(pgFeature
                        .getProtectionGroupId(), pgFeature
                        .getProtectionGroupName(), pgFeature
                        .getProtectionGroupDescription()));
                }
                return features;
            } catch (Exception ex) {
                log.error("Error retrieving security features", ex);
                throw new ApplicationException(ex);
            }
        } else {
            throw new ApplicationException(
                "Only Website Administrators can retrieve security features");
        }
    }

    private static boolean isWebsiteAdministrator() throws ApplicationException {
        try {
            String userLogin = SecurityContextHolder.getContext()
                .getAuthentication().getName();
            UserProvisioningManager upm = SecurityServiceProvider
                .getUserProvisioningManager(BiobankSecurityUtil.APPLICATION_CONTEXT_NAME);
            User user = upm.getUser(userLogin);
            if (user == null) {
                throw new ApplicationException("Error retrieving security user");
            }
            Set<?> groups = upm.getGroups(user.getUserId().toString());
            for (Object obj : groups) {
                Group group = (Group) obj;
                if (group
                    .getGroupName()
                    .equals(
                        edu.ualberta.med.biobank.common.security.Group.GROUP_WEBSITE_ADMINISTRATOR)) {
                    return true;
                }
            }
            return false;
        } catch (ApplicationException ae) {
            log.error("Error checking isWebsiteAdministrator", ae);
            throw ae;
        } catch (Exception ex) {
            log.error("Error checking isWebsiteAdministrator", ex);
            throw new ApplicationException(ex);
        }
    }

    public static void newSiteSecurity(Integer siteId, String nameShort) {
        try {
            UserProvisioningManager upm = SecurityServiceProvider
                .getUserProvisioningManager(APPLICATION_CONTEXT_NAME);
            Application currentApplication = upm
                .getApplication(APPLICATION_CONTEXT_NAME);
            // Create protection element for the site
            ProtectionElement pe = new ProtectionElement();
            pe.setApplication(currentApplication);
            pe.setProtectionElementName(SITE_CLASS_NAME + "/" + nameShort);
            pe.setProtectionElementDescription(nameShort);
            pe.setObjectId(SITE_CLASS_NAME);
            pe.setAttribute("id");
            pe.setValue(siteId.toString());
            upm.createProtectionElement(pe);

            // Create a new protection group for this protection element only
            ProtectionGroup pg = new ProtectionGroup();
            pg.setApplication(currentApplication);
            pg.setProtectionGroupName(nameShort + " site");
            pg.setProtectionGroupDescription("Protection group for site "
                + nameShort + " (id=" + siteId + ")");
            pg.setProtectionElements(new HashSet<ProtectionElement>(Arrays
                .asList(pe)));
            // parent will be the "all sites" protection group
            ProtectionGroup allSitePg = upm
                .getProtectionGroupById(ALL_SITES_PG_ID);
            pg.setParentProtectionGroup(allSitePg);
            upm.createProtectionGroup(pg);
        } catch (Exception e) {
            log.error("error adding new site security", e);
            throw new RuntimeException("Error adding new site " + siteId + ":"
                + nameShort + " security:" + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static void deleteSiteSecurity(Integer siteId, String nameShort) {
        try {
            UserProvisioningManager upm = SecurityServiceProvider
                .getUserProvisioningManager(APPLICATION_CONTEXT_NAME);
            ProtectionElement searchPE = new ProtectionElement();
            searchPE.setObjectId(Site.class.getName());
            searchPE.setAttribute("id");
            searchPE.setValue(siteId.toString());
            SearchCriteria sc = new ProtectionElementSearchCriteria(searchPE);
            List<ProtectionElement> peToDelete = upm.getObjects(sc);
            if (peToDelete == null || peToDelete.size() == 0) {
                return;
            }
            List<String> pgIdsToDelete = new ArrayList<String>();
            for (ProtectionElement pe : peToDelete) {
                Set<ProtectionGroup> pgs = upm.getProtectionGroups(pe
                    .getProtectionElementId().toString());
                for (ProtectionGroup pg : pgs) {
                    // remove the protection group only if it contains only
                    // this protection element and is not the main site
                    // admin group
                    String pgId = pg.getProtectionGroupId().toString();
                    if (!pgId.equals(ALL_SITES_PG_ID)
                        && upm.getProtectionElements(pgId).size() == 1) {
                        pgIdsToDelete.add(pgId);
                    }
                }
                upm.removeProtectionElement(pe.getProtectionElementId()
                    .toString());
            }
            for (String pgId : pgIdsToDelete) {
                upm.removeProtectionGroup(pgId);
            }
        } catch (Exception e) {
            log.error("error deleting site security", e);
            throw new RuntimeException("Error deleting site " + siteId + ":"
                + nameShort + " security: " + e.getMessage());
        }

    }
}