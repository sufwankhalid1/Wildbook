package org.ecocean.security;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ecocean.Global;
import org.ecocean.Organization;
import org.ecocean.rest.SimpleUser;
import org.ecocean.util.NotificationException;
import org.slf4j.Logger;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class DbUserService implements UserService {
    private static Logger logger = UserService.logger;

    private final ConnectionInfo ci;
    private final Map<Integer, SecurityInfo> mapUserId = new HashMap<>();
    private final Map<String, SecurityInfo> mapUserName = new HashMap<>();
    private final Map<String, SecurityInfo> mapUserEmail = new HashMap<>();
    private List<Organization> orgs;

    public DbUserService(final ConnectionInfo ci) {
        this.ci = ci;
    }

    private SecurityInfo addNewSecurityInfo(final User user) {
        SecurityInfo info = new SecurityInfo(user);
        mapUserId.put(user.getId(), info);
        if (user.getUsername() != null) {
            mapUserName.put(user.getUsername().toLowerCase(), info);
        }
        if (user.getEmail() != null) {
            mapUserEmail.put(user.getEmail().toLowerCase(), info);
        }

        try (Database db = new Database(ci)) {
            db.getTable(UserFactory.TABLENAME_ROLES).select((rs) -> {
                String aContext = rs.getString("context");
                Set<String> someRoles = info.getContextRoles(aContext);
                if (someRoles == null) {
                    someRoles = new HashSet<>();
                    info.setContextRoles(aContext, someRoles);
                }
                someRoles.add(rs.getString("rolename"));
            }, "userid = " + user.getId());
        } catch(DatabaseException ex){
            logger.error("Can't read roles", ex);
        }

        return info;
    }

    private void resetPassword(final User user, final String password) {
        user.resetPassword(password);
        user.setVerified(true);

        saveUser(user);
    }

    private User getUser(final Database db, final String username) throws DatabaseException {
        SqlStatement sql = UserFactory.getUserStatement();
        sql.addCondition(UserFactory.ALIAS_USERS, "username", SqlRelationType.EQUAL, username.toLowerCase()).setFunction("lower");
        return db.selectFirst(sql, (rs) -> {
            return UserFactory.readUser(rs);
        });
    }

    private User getUserByEmail(final Database db, final String email) throws DatabaseException {
        if (email == null) {
            return null;
        }

        SqlStatement sql = UserFactory.getUserStatement();
        sql.addCondition(UserFactory.ALIAS_USERS, "email", SqlRelationType.EQUAL, email.toLowerCase()).setFunction("lower");
        return db.selectFirst(sql, (rs) -> {
            return UserFactory.readUser(rs);
        });
    }

    private User getUserByNameOrEmail(final Database db, final String term) throws DatabaseException {
        if (term == null) {
            return null;
        }

        User user = getUserByEmail(db, term);
        if (user != null) {
            return user;
        }
        return getUser(db, term);
    }

    private User getUserByFullname(final Database db, final String fullname) throws DatabaseException {
        if (fullname == null) {
            return null;
        }

        SqlStatement sql = UserFactory.getUserStatement();
        sql.addCondition(UserFactory.ALIAS_USERS, "fullname", SqlRelationType.LIKE, fullname.toLowerCase()).setFunction("lower");

        return db.selectFirst(sql, (rs) -> {
            return UserFactory.readUser(rs);
        });
    }

    private User getUserById(final Database db, final Integer id) throws DatabaseException {
        if (id == null) {
            return null;
        }

        SqlStatement sql =  UserFactory.getUserStatement();
        sql.addCondition( UserFactory.ALIAS_USERS,  UserFactory.PK_USERS, SqlRelationType.EQUAL, id);

        return db.selectFirst(sql, (rs) -> {
            return  UserFactory.readUser(rs);
        });
    }

    private void deleteAllRoles(final Database db, final int userid) throws DatabaseException {
        Table users = db.getTable(UserFactory.TABLENAME_ROLES);
        users.deleteRows("userid = " + userid);
    }

    //deletes current roles then adds in the chosen roles
    private void updateRoles(final Database db, final int userid, final String context, final Set<String> roles) throws DatabaseException {
        Table users = db.getTable(UserFactory.TABLENAME_ROLES);
        Set<String> current_roles = Global.INST.getUserService().getAllRolesForUserInContext(Integer.toString(userid), "context0");
        if (!roles.isEmpty()) {
            if (current_roles != null && !current_roles.isEmpty()) {
                for (String current_role : current_roles) {
                    deleteRole(db, userid, current_role);
                }
            }
            for (String role : roles) {
                SqlInsertFormatter formatter = new SqlInsertFormatter();
                formatter.append(UserFactory.PK_USERS, userid)
                    .append("context", context)
                    .append("rolename", role);
                users.insertRow(formatter.getColumnClause(), formatter.getValueClause());
            }
        } else {
            deleteAllRoles(db, userid);
        }
    }

    private void deleteRole(final Database db, final int userid, final String role) throws DatabaseException {
        Table users = db.getTable(UserFactory.TABLENAME_ROLES);
        SqlWhereFormatter formatter = new SqlWhereFormatter();
        formatter.append("rolename", role);
        formatter.append("userid", userid);
        users.deleteRows(formatter.getWhereClause());
    }

    private void addRole(final Database db, final int userid, final String context, final String role) throws DatabaseException {
        Table users = db.getTable(UserFactory.TABLENAME_ROLES);
        SqlInsertFormatter formatter = new SqlInsertFormatter();
        formatter.append(UserFactory.PK_USERS, userid)
            .append("context", context)
            .append("rolename", role);
        users.insertRow(formatter.getColumnClause(), formatter.getValueClause());
    }

    private void saveUser(final Database db, final User user) throws DatabaseException {
        Table table = db.getTable(UserFactory.TABLENAME_USERS);

        //
        // We have a unique key on email address so we have to make sure to turn
        // empty strings into nulls.
        //
        if ("".equals(user.getEmail())) {
            user.setEmail(null);
        }

        if (user.getId() == null) {
            user.initPassword();

            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillUserFormatter(formatter, user);

            user.setId(table.insertSequencedRow(formatter, UserFactory.PK_USERS));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillUserFormatter(formatter, user);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(UserFactory.PK_USERS, user.getId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }

    private void fillUserFormatter(final SqlFormatter formatter, final User user) {
        formatter.append("username", user.getUsername());
        formatter.append("fullname", user.getFullName());
        formatter.append("email", user.getEmail());
        formatter.append("phonenumber", user.getPhoneNumber());
        formatter.append("physicaladdress", user.getPhysicalAddress());
        formatter.append("avatar", user.getAvatarPath());
        formatter.append("statement", user.getStatement());
        formatter.append("lastlogin", user.getLastLogin());
        formatter.append("password", user.getHashedPass());
        formatter.append("salt", user.getSalt());
        formatter.append("acceptedua", user.getAcceptedUserAgreement());
        formatter.append("verified", user.isVerified());
        formatter.append("prtoken", user.getPrtoken());
        formatter.append("prtimestamp", user.getPrtimestamp());

        if (user.getUserLocation() != null) {
            formatter.append("locserviceid", user.getUserLocation().getCode());
            formatter.append("region", user.getUserLocation().getRegion());
            formatter.append("subregion", user.getUserLocation().getSubregion());
            formatter.append("country", user.getUserLocation().getCountry());

            if (user.getUserLocation().getLatlng() != null) {
                formatter.append("longitude", user.getUserLocation().getLatlng().getLongitude());
                formatter.append("latitude", user.getUserLocation().getLatlng().getLatitude());
            }
        }
    }

    private User getUserByPRToken(final Database db, final String token) throws DatabaseException {
        if (token == null) {
            return null;
        }

        SqlStatement sql = UserFactory.getUserStatement();
        sql.addCondition(UserFactory.ALIAS_USERS, "prtoken", SqlRelationType.EQUAL, token);
        return db.selectFirst(sql, (rs) -> {
            return UserFactory.readUser(rs);
        });
    }

    private User verifyPRToken(final Database db, final String token) throws IllegalAccessException, DatabaseException {
        User user = getUserByPRToken(db, token);

        if (user == null) {
            throw new IllegalAccessException("Unknown password reset token.");
        }

        LocalDateTime aWeekAgo = LocalDateTime.now().minusWeeks(1);
        if (user.getPrtimestamp().isBefore(aWeekAgo)) {
            //
            // Expired reset token.
            //
            throw new IllegalAccessException("Password reset token is out of date.");
        }

        return user;
    }

    private void setLastLogin(final Database db, final User user) throws DatabaseException {
        SqlUpdateFormatter formatter = new SqlUpdateFormatter();
        formatter.append("lastlogin", System.currentTimeMillis());

        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append(UserFactory.PK_USERS, user.getId());

        db.getTable(UserFactory.TABLENAME_USERS).updateRow(formatter.getUpdateClause(), where.getWhereClause());
    }

    //=======================================
    // Organization stuff
    //=======================================

    private List<Organization> getOrganizations(final Database db) throws DatabaseException {
        List<Organization> orgs = new ArrayList<>();
        db.getTable(UserFactory.TABLENAME_ORG).select((rs) -> {
            orgs.add(UserFactory.readOrganization(rs));
        }, null, "orgname");
        return orgs;
    }

    private void saveOrganization(final Database db, final Organization organization) throws DatabaseException {
        Table table = db.getTable(UserFactory.TABLENAME_ORG);

        if (organization.getOrgId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillOrgFormatter(formatter, organization);

            organization.setOrgId(table.insertSequencedRow(formatter, UserFactory.PK_ORG));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillOrgFormatter(formatter, organization);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(UserFactory.PK_ORG, organization.getOrgId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }

    private void deleteOrganization(final Database db, final int orgid) throws DatabaseException, Throwable {
        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append(UserFactory.PK_ORG, orgid);

        Table organizations = db.getTable(UserFactory.TABLENAME_ORG);
        Table users = db.getTable(UserFactory.TABLENAME_USERS);
        Table survey = db.getTable(UserFactory.TABLENAME_SURVEY);
        Table vessel = db.getTable(UserFactory.TABLENAME_VESSEL);

        if (users.getCount(where.getWhereClause()) > 0 ||
            survey.getCount(where.getWhereClause()) > 0 ||
            vessel.getCount(where.getWhereClause()) > 0) {
                throw new NotificationException("Cannot delete. This organization is currently in use.");
        }

        organizations.deleteRows(where.getWhereClause());
    }

    private void fillOrgFormatter(final SqlFormatter formatter, final Organization organization) {
        formatter.append("orgname", organization.getName());
    }

    //=======================================
    //    UserService i/f
    //=======================================

    @Override
    public SecurityInfo getSecurityInfo(final String userIdString) {
        if (StringUtils.isBlank(userIdString)) {
            return null;
        }

        Integer userid = NumberUtils.createInteger(userIdString);

        SecurityInfo info = mapUserId.get(userid);

        if (info == null) {
            try (Database db = new Database(ci)) {
                User user = getUserById(db, userid);
                if (user == null) {
                    return null;
                }
                info = addNewSecurityInfo(user);
            } catch (DatabaseException ex) {
                throw new SecurityException("Trouble authenticating user [" + userid + "]", ex);
            }
        }

        return info;
    }

    @Override
    public User getUserById(final String id) {
        SecurityInfo info = getSecurityInfo(id);
        if (info == null) {
            return null;
        }
        return info.getUser();
    }

    @Override
    public Set<String> getAllRolesForUserInContext(final String id, final String context) {
        SecurityInfo info = getSecurityInfo(id);
        if (info == null) {
            return Collections.emptySet();
        }
        return info.getContextRoles(context);
    }

    @Override
    public List<Organization> getOrganizations() {
        if (orgs == null) {
            try (Database db = new Database(ci)){
                orgs = getOrganizations(db);
            } catch (DatabaseException ex) {
                throw new SecurityException("Can't read organizations", ex);
            }
        }

        return new ArrayList<Organization>(orgs);
    }

    @Override
    public Organization getOrganization(final String name) {
        for (Organization org : getOrganizations()) {
            if (org.getName().equalsIgnoreCase(name)) {
                return org;
            }
        }

        return null;
    }

    @Override
    public void saveOrganization(final Organization org) {
        try (Database db = new Database(ci)) {
            saveOrganization(db, org);
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't add new organization.", ex);
        }

        //
        // TODO: Need to add a sorting method so that the new org is properly sorted
        // within the list.
        //
        // Nulling orgs then calling getOrganizations to clear cache
        // to distinguish between an edit and an insert
        //
        orgs = null;
        getOrganizations();
    }

    @Override
    public void deleteOrganization(final int orgid) {
        try (Database db = new Database(ci)) {
            deleteOrganization(db, orgid);
        } catch (DatabaseException ex) {
            throw new SecurityException("Cannot delete organization", ex);
        } catch (Throwable msg) {
            throw new NotificationException("Cannot delete. This organization is currently in use.");
        }

        orgs = null;
        getOrganizations();
    }

    @Override
    public User getUserByEmail(final String email) {
        if (email == null) {
            return null;
        }

        SecurityInfo info = mapUserEmail.get(email.toLowerCase());

        if (info == null) {
            try (Database db = new Database(ci)) {
                User user;
                try {
                    user = getUserByEmail(db, email);
                } catch (DatabaseException ex) {
                    throw new SecurityException("Can't read user.", ex);
                }
                if (user == null) {
                    return null;
                }
                info = addNewSecurityInfo(user);
            }
        }

        return info.getUser();
    }

    @Override
    public User getUserByNameOrEmail(final String term) {
        if (term == null) {
            return null;
        }

        SecurityInfo info = mapUserEmail.get(term.toLowerCase());

        if (info != null) {
            return info.getUser();
        }

        info = mapUserName.get(term.toLowerCase());
        if (info != null) {
            return info.getUser();
        }

        if (info == null) {
            try (Database db = new Database(ci)) {
                User user;
                try {
                    user = getUserByNameOrEmail(db, term);
                } catch (DatabaseException ex) {
                    throw new SecurityException("Can't read user.", ex);
                }
                if (user == null) {
                    return null;
                }
                info = addNewSecurityInfo(user);
            }
        }

        return info.getUser();
    }

    @Override
    public User getUserByFullname(final String fullname) {
        try (Database db = new Database(ci)) {
            try {
                return getUserByFullname(db, fullname);
            } catch (DatabaseException ex) {
                throw new SecurityException("Can't read user.", ex);
            }
        }
    }

    @Override
    public String createPWResetToken(final String userid) {
        try (Database db = new Database(ci)) {
            return UserFactory.createPWResetToken(db, NumberUtils.createInteger(userid));
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't reset password token for user [" + userid + "]", ex);
        }
    }

    @Override
    public void saveUser(final User user) {
        if (user == null) {
            return;
        }

        try (Database db = new Database(ci)) {
            saveUser(db, user);
            //
            // Need to reupdate the maps with the new user info.
            //
            addNewSecurityInfo(user);
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't save user [" + user.getId() + "]", ex);
        }
    }

    @Override
    public void deleteAllRoles(final String userid) {
        try (Database db = new Database(ci)) {
            deleteAllRoles(db, NumberUtils.createInteger(userid));
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't delete roles from user [" + userid + "]", ex);
        }
    }

    @Override
    public void deleteRoles(final String userid) {
        try (Database db = new Database(ci)) {
            deleteAllRoles(db, NumberUtils.createInteger(userid));
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't delete roles from user [" + userid + "]", ex);
        }
    }

    @Override
    public void addRole(final String userid, final String context, final String role) {
        try (Database db = new Database(ci)) {
            addRole(db, NumberUtils.createInteger(userid), context, role);
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't delete roles from user [" + userid + "]", ex);
        }
    }

    @Override
    public void updateRoles(final int userid, final String context, final Set<String> roles) {
        try (Database db = new Database(ci)) {
            updateRoles(db, userid, context, roles);
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't delete roles from user [" + userid + "]", ex);
        }
    }

    @Override
    public void resetPass(final String userid, final String password) {
        User user = getUserById(userid);
        if (user != null) {
            resetPassword(user, password);
        }
    }

    @Override
    public void resetPassWithToken(final String token, final String password) {
        try (Database db = new Database(ci)) {
            User user = verifyPRToken(db, token);
            resetPassword(user, password);
        } catch (DatabaseException | IllegalAccessException ex) {
            throw new SecurityException("Can't find user for token [" + token + "]", ex);
        }
    }

    @Override
    public boolean doesUserHaveRole(final String userid, final String context, final String role) {
        SecurityInfo info = getSecurityInfo(userid);
        if (info == null) {
            return false;
        }
        Set<String> roles = info.getContextRoles(context);
        if (roles == null) {
            return false;
        }
        return roles.contains(role);
    }

    @Override
    public void deleteUser(final User user) {
        if (user == null) {
            return;
        }
        try (Database db = new Database(ci)) {
            db.getTable(UserFactory.TABLENAME_USERS).deleteRows("userid = " + user.getId());

            mapUserId.remove(user.getId());
            if (user.getUsername() != null) {
                mapUserName.remove(user.getUsername().toLowerCase());
            }
            if (user.getEmail() != null) {
                mapUserEmail.remove(user.getEmail().toLowerCase());
            }
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't delete user [" + user.getId() + "]", ex);
        }
    }

    @Override
    public void setLastLogin(final User user) {
        try (Database db = new Database(ci)) {
            setLastLogin(db, user);
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't set last login for user [" + user.getId() + "]", ex);
        }
    }

    @Override
    public void clearUserCache() {
        if (logger.isDebugEnabled()) {
            logger.debug("user cache length before clear: " + mapUserId.size());
        }

        mapUserId.clear();
        mapUserName.clear();
        mapUserEmail.clear();

        if (logger.isDebugEnabled()) {
            logger.debug("user cache length after clear: " + mapUserId.size());
        }
    }

    @Override
    public List<SimpleUser> searchUsers(final String query) {
        try (Database db = new Database(ci)) {
            SqlStatement sql = UserFactory.userSearchStatement(query);
            return UserFactory.readSimpleUsers(db, sql);
        } catch (DatabaseException ex) {
            throw new SecurityException("Could not search users for [" + query + "]", ex);
        }
    }

    @Override
    public int getNumUsers() {
        try (Database db = new Database(ci)) {
            Table users = db.getTable(UserFactory.TABLENAME_USERS);
            return (int)users.getCount(null);
        } catch (DatabaseException ex) {
            throw new SecurityException("Could not get number of users", ex);
        }
    }
}
