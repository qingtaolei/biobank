package edu.ualberta.med.biobank.test.wrappers;

import java.util.Arrays;

import junit.framework.Assert;

import org.acegisecurity.AccessDeniedException;
import org.junit.Test;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.GroupWrapper;
import edu.ualberta.med.biobank.common.wrappers.MembershipWrapper;
import edu.ualberta.med.biobank.common.wrappers.RoleWrapper;
import edu.ualberta.med.biobank.common.wrappers.UserWrapper;
import edu.ualberta.med.biobank.model.Membership;
import edu.ualberta.med.biobank.model.PermissionEnum;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.model.User;
import edu.ualberta.med.biobank.server.applicationservice.BiobankApplicationService;
import edu.ualberta.med.biobank.server.applicationservice.BiobankCSMSecurityUtil;
import edu.ualberta.med.biobank.test.AllTestsSuite;
import edu.ualberta.med.biobank.test.TestDatabase;
import edu.ualberta.med.biobank.test.Utils;
import edu.ualberta.med.biobank.test.internal.GroupHelper;
import edu.ualberta.med.biobank.test.internal.MembershipHelper;
import edu.ualberta.med.biobank.test.internal.RoleHelper;
import edu.ualberta.med.biobank.test.internal.UserHelper;
import gov.nih.nci.security.SecurityServiceProvider;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.system.applicationservice.ApplicationException;

@Deprecated
public class TestUser extends TestDatabase {

    @Test
    public void testCreateUser() throws BiobankCheckException, Exception {
        String name = "createUser" + r.nextInt();
        String password = "123";
        UserWrapper user = UserHelper.addUser(name, password, true);

        // check biobank user
        User dbUser = ModelUtils.getObjectWithId(appService, User.class,
            user.getId());
        Assert.assertNotNull(dbUser);
        Assert.assertEquals(name, dbUser.getLogin());
        Assert.assertNotNull(dbUser.getCsmUserId());

        // check csm user
        UserProvisioningManager upm =
            SecurityServiceProvider
                .getUserProvisioningManager(BiobankCSMSecurityUtil.APPLICATION_CONTEXT_NAME);

        gov.nih.nci.security.authorization.domainobjects.User csmUser = upm
            .getUser(name);
        Assert.assertNotNull(csmUser);
        Assert.assertNotNull(csmUser.getPassword());
        Assert.assertFalse(csmUser.getPassword().isEmpty());

        // check user can connect
        BiobankApplicationService newUserAppService = AllTestsSuite.connect(name,
            password);
        // check user can access a biobank object using the new appService
        try {
            newUserAppService.search(Site.class, new Site());
        } catch (AccessDeniedException ade) {
            Assert.fail("User should be able to access any object");
        }
    }

    @Test
    public void testUpdateUser() throws BiobankCheckException, Exception {
        String name = "updateUser" + r.nextInt();
        String password = "123";
        UserWrapper user = UserHelper.addUser(name, password, true);

        user.reload();
        Assert.assertNull(user.getEmail());
        String email = "toto@gmail.com";
        user.setEmail(email);
        user.persist();

        user.reload();
        Assert.assertEquals(email, user.getEmail());
    }

    @Test
    public void testDeleteUser() throws BiobankCheckException, Exception {
        String name = "deleteUser" + r.nextInt();
        UserWrapper user = UserHelper.addUser(name, null, false);

        User dbUser = ModelUtils.getObjectWithId(appService, User.class,
            user.getId());
        Assert.assertNotNull(dbUser);
        UserProvisioningManager upm =
            SecurityServiceProvider
                .getUserProvisioningManager(BiobankCSMSecurityUtil.APPLICATION_CONTEXT_NAME);
        gov.nih.nci.security.authorization.domainobjects.User csmUser = upm
            .getUser(name);
        Assert.assertNotNull(csmUser);

        Integer idUser = user.getId();
        user.delete();
        Assert.assertNull(ModelUtils.getObjectWithId(appService, User.class,
            idUser));
        csmUser = upm.getUser(name);
        Assert.assertNull(csmUser);
    }

    @Test
    public void testGettersAndSetters() throws BiobankCheckException, Exception {
        String name = "testGettersAndSetters" + r.nextInt();
        UserWrapper user = UserHelper.addUser(name, null, true);
        testGettersAndSetters(user,
            Arrays.asList("getPassword", "getCsmUserId"));
    }

    @Test
    public void testDeleteWhenHasGroupRelation() throws Exception {
        String name = "deleteWhenHasGroupRelation" + r.nextInt();
        UserWrapper user1 = UserHelper.addUser(name + "_1", null, false);
        UserWrapper user2 = UserHelper.addUser(name + "_2", null, true);

        GroupWrapper group = GroupHelper.addGroup(name, true);
        group.addToUserCollection(Arrays.asList(user1, user2));
        group.persist();
        user1.reload();
        user2.reload();

        Assert.assertEquals(2, group.getUserCollection(false).size());

        // deletedependencies should remove the relation in the correlation
        // table
        user1.delete();

        group.reload();
        Assert.assertEquals(1, group.getUserCollection(false).size());
    }

    @Test
    public void testAddMembershipsWithNoObject() throws Exception {
        String name = "addMembershipsWithNoObject" + r.nextInt();
        UserWrapper user = UserHelper.addUser(name, null, true);

        UserHelper.addMembership(user, null, null);

        user.reload();
        Assert.assertEquals(1, user.getMembershipCollection(false).size());
    }

    @Test
    public void testAddMembershipsWithRole() throws Exception {
        String name = "addMembershipsWithRole" + r.nextInt();
        UserWrapper user = UserHelper.addUser(name, null, true);

        RoleWrapper role = RoleHelper.addRole(name, true);

        MembershipWrapper ms = MembershipHelper.newMembership(user, null, null);
        ms.addToRoleCollection(Arrays.asList(role));
        user.persist();

        user.reload();
        Assert.assertEquals(1, user.getMembershipCollection(false).size());
        ms = user.getMembershipCollection(false).get(0);
        Assert.assertEquals(1, ms.getRoleCollection(false).size());
    }

    @Test
    public void testRemoveMembershipWithRole() throws Exception {
        String name = "removeMembershipWithRole" + r.nextInt();
        UserWrapper user = UserHelper.addUser(name, null, true);

        RoleWrapper role = RoleHelper.addRole(name, true);
        MembershipWrapper mwr = MembershipHelper
            .newMembership(user, null, null);
        mwr.addToRoleCollection(Arrays.asList(role));
        user.persist();

        user.reload();
        Assert.assertEquals(1, user.getMembershipCollection(false).size());

        mwr = user.getMembershipCollection(false).get(0);
        Integer mwrId = mwr.getId();
        user.removeFromMembershipCollection(Arrays.asList(mwr));
        user.persist();
        user.reload();
        Assert.assertEquals(0, user.getMembershipCollection(false).size());

        Membership msDB = ModelUtils.getObjectWithId(appService,
            Membership.class, mwrId);
        Assert.assertNull(msDB);
    }

    @Test
    public void testAddMembershipsWithPermission() throws Exception {
        String name = "testAddMembershipsWithPermission" + r.nextInt();
        UserWrapper user = UserHelper.addUser(name, null, true);

        MembershipWrapper ms = MembershipHelper.newMembership(user, null, null);
        ms.addToPermissionCollection(Arrays
            .asList(PermissionEnum.CLINIC_CREATE));
        user.persist();

        user.reload();
        Assert.assertEquals(1, user.getMembershipCollection(false).size());
        ms = user.getMembershipCollection(false).get(0);
        Assert.assertEquals(1, ms.getPermissionCollection().size());
    }

    @Test
    public void testModifyPassword() throws Exception {
        String name = "createUser" + r.nextInt();
        String password = "123";
        UserWrapper user = UserHelper.addUser(name, password, true);

        // check user can connect
        BiobankApplicationService newUserAppService = AllTestsSuite.connect(name,
            password);
        String newPwd = "new123";
        // search the user again otherwise the appService will still try with
        // testuser
        user = UserWrapper.getUser(newUserAppService, name);
        user.modifyPassword(password, newPwd, null);

        // check user can't connect with old password
        try {
            AllTestsSuite.connect(name, password);
            Assert
                .fail("Should not be able to connect with the old password anymore");
        } catch (ApplicationException ae) {
            Assert.assertTrue("Should failed because of authentication", ae
                .getMessage().contains("Error authenticating user"));
        }
        // check user can't connect with new password
        AllTestsSuite.connect(name, newPwd);
    }

    @Test
    public void testAddUserFailAndCsmUser() throws Exception {
        UserWrapper user = new UserWrapper(appService);
        String login = Utils.getRandomString(300, 400);
        user.setLogin(login);
        // FIXME should use another test because I should set login length to be
        // the same between csm_user and our user table. Maybe in using a
        // WrapperTransation with something sending an error to make the commit
        // fail
        try {
            user.persist();
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue("should fail because login is too long", true);
        }
        // check csm user
        UserProvisioningManager upm =
            SecurityServiceProvider
                .getUserProvisioningManager(BiobankCSMSecurityUtil.APPLICATION_CONTEXT_NAME);

        gov.nih.nci.security.authorization.domainobjects.User csmUser = upm
            .getUser(login);
        Assert.assertNull(csmUser);
    }

    @Test
    public void testAddGroups() throws Exception {
        String name = "addGroups" + r.nextInt();

        UserWrapper user = UserHelper.addUser(name, null, true);

        Assert.assertEquals(0, user.getGroupCollection(false).size());

        GroupWrapper group1 = GroupHelper.addGroup(name + "_1", true);
        GroupWrapper group2 = GroupHelper.addGroup(name + "_2", true);

        user.addToGroupCollection(Arrays.asList(group1, group2));
        user.persist();

        user.reload();
        group1.reload();
        group2.reload();
        Assert.assertEquals(2, user.getGroupCollection(false).size());
        Assert.assertEquals(1, group1.getUserCollection(false).size());
        Assert.assertEquals(1, group2.getUserCollection(false).size());
    }
}
