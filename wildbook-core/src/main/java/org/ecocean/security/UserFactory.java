package org.ecocean.security;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ecocean.Global;
import org.ecocean.Organization;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.rest.SimpleUser;
import org.ecocean.util.NotificationException;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SpecialSqlCondition;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class UserFactory {
    //private static Logger logger = LoggerFactory.getLogger(UserFactory.class);

    private static SecureRandom random = new SecureRandom();

    public static String TABLENAME_USERS = "users";
    public static String TABLENAME_ROLES = "userroles";
    public static String TABLENAME_ORG = "organization";
    public static String TABLENAME_SURVEY = "survey";
    public static String TABLENAME_VESSEL = "vessel";

    public static String ALIAS_USERS = "u";
    public static String ALIAS_ORG = "o";

    public static String PK_USERS = "userid";
    public static String PK_ORG = "orgid";

    private UserFactory() {
        // prevent instantiation
    }

    public static SqlStatement getUserStatement() {
        SqlStatement sql = new SqlStatement(TABLENAME_USERS, ALIAS_USERS);
        sql.addLeftOuterJoin(ALIAS_USERS, UserFactory.PK_ORG, TABLENAME_ORG, ALIAS_ORG, UserFactory.PK_ORG);
        sql.addLeftOuterJoin(ALIAS_USERS,
                             "avatarid",
                             MediaAssetFactory.TABLENAME_MEDIAASSET,
                             MediaAssetFactory.ALIAS_MEDIAASSET,
                             MediaAssetFactory.PK_MEDIAASSET);
        return sql;
    }

    public static SqlStatement getUserStatement(final boolean distinct) {
        SqlStatement sql = getUserStatement();
        sql.setSelectString(ALIAS_USERS
                            + ".*, "
                            + ALIAS_ORG
                            + ".*, "
                            + MediaAssetFactory.ALIAS_MEDIAASSET
                            + ".*");
        sql.setSelectDistinct(true);
        return sql;
    }

    public static long getNumUsers(final Database db) throws DatabaseException {
        Table users = db.getTable(TABLENAME_USERS);
        return users.getCount(null);
    }

    public static User getUserById(final Database db, final Integer id) throws DatabaseException {
        if (id == null) {
            return null;
        }

        SqlStatement sql = getUserStatement();
        sql.addCondition(ALIAS_USERS, PK_USERS, SqlRelationType.EQUAL, id);

        return db.selectFirst(sql, (rs) -> {
            return readUser(rs);
        });
    }


    public static User getUser(final Database db, final String username) throws DatabaseException {
        SqlStatement sql = getUserStatement();
        sql.addCondition(ALIAS_USERS, "username", SqlRelationType.EQUAL, username.toLowerCase()).setFunction("lower");
        return db.selectFirst(sql, (rs) -> {
            return readUser(rs);
        });
    }

    public static SimpleUser getSimpleUser(final Database db, final String username) throws DatabaseException
    {
        User user = getUser(db, username);
        if (user == null) {
            return null;
        }

        return user.toSimple();
    }

    public static User getUserByPRToken(final Database db, final String token) throws DatabaseException {
        if (token == null) {
            return null;
        }

        SqlStatement sql = getUserStatement();
        sql.addCondition(ALIAS_USERS, "prtoken", SqlRelationType.EQUAL, token);
        return db.selectFirst(sql, (rs) -> {
            return readUser(rs);
        });
    }


    public static User getUserByEmail(final Database db, final String email) throws DatabaseException {
        if (email == null) {
            return null;
        }

        SqlStatement sql = getUserStatement();
        sql.addCondition(ALIAS_USERS, "email", SqlRelationType.EQUAL, email.toLowerCase()).setFunction("lower");
        return db.selectFirst(sql, (rs) -> {
            return readUser(rs);
        });
    }

    public static User getUserByNameOrEmail(final Database db, final String term) throws DatabaseException {
        if (term == null) {
            return null;
        }

        User user = getUser(db, term);
        if (user != null) {
            return user;
        }
        return getUserByEmail(db, term);
    }


    public static User getUserByFullname(final Database db, final String fullname) throws DatabaseException {
        if (fullname == null) {
            return null;
        }

        SqlStatement sql = getUserStatement();
        sql.addCondition(ALIAS_USERS, "fullname", SqlRelationType.LIKE, fullname.toLowerCase()).setFunction("lower");

        return db.selectFirst(sql, (rs) -> {
            return readUser(rs);
        });
    }


    public static User readUser(final RecordSet rs) throws DatabaseException {
        Integer id = rs.getInteger(PK_USERS);
        if (id == null) {
            return null;
        }

        User user = new User(id, rs.getString("username"), rs.getString("fullname"), rs.getString("email"));

        user.setAvatarFull(MediaAssetFactory.readPhoto(rs));
        user.setStatement(rs.getString("statement"));
        user.setOrganization(readOrganization(rs));

        user.setAcceptedUserAgreement(rs.getBoolean("acceptedua"));
        user.setCreationDate(rs.getLocalDate("creationdate"));
        user.setLastLogin(rs.getLong("lastlogin"));
        user.setPhoneNumber(rs.getString("phonenumber"));
        user.setPhysicalAddress(rs.getString("physicaladdress"));
        user.setSaltAndHashedPass(rs.getString("salt"), rs.getString("password"));
        user.setVerified(rs.getBoolean("verified"));
        user.setPrtoken(rs.getString("prtoken"));
        user.setPrtimestamp(rs.getLocalDateTime("prtimestamp"));

        return user;
    }


    public static SimpleUser readSimpleUser(final RecordSet rs) throws DatabaseException {
        User user = readUser(rs);
        if (user == null) {
            return null;
        }

        return user.toSimple();
    }

    public static void saveUser(final Database db, final User user) throws DatabaseException {
        Table table = db.getTable(TABLENAME_USERS);

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

            user.setId(table.insertSequencedRow(formatter, PK_USERS));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillUserFormatter(formatter, user);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_USERS, user.getId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }

    private static void fillUserFormatter(final SqlFormatter formatter, final User user) {
        formatter.append("username", user.getUsername());
        formatter.append("fullname", user.getFullName());
        formatter.append("email", user.getEmail());
        formatter.append("phonenumber", user.getPhoneNumber());
        formatter.append("physicaladdress", user.getPhysicalAddress());
        if (user.getAvatarFull() != null) {
            formatter.append("avatarid", user.getAvatarFull().getId());
        } else {
            formatter.appendNull("avatarid");
        }
        formatter.append("statement", user.getStatement());
        formatter.append("lastlogin", user.getLastLogin());
        formatter.append("password", user.getHashedPass());
        formatter.append("salt", user.getSalt());
        formatter.append("acceptedua", user.getAcceptedUserAgreement());
        formatter.append("verified", user.isVerified());
        formatter.append("prtoken", user.getPrtoken());
        formatter.append("prtimestamp", user.getPrtimestamp());
    }


    public static void deleteUser(final Database db, final int userid) throws DatabaseException {
        db.getTable(TABLENAME_USERS).deleteRows("userid = " + userid);
    }


    //======================================
    // Role stuff
    //======================================
//
//    public static Set<String> getAllRolesForUserInContext(final Database db,
//                                                          final int userid,
//                                                          final String context) throws DatabaseException {
//        Table users = db.getTable(TABLENAME_ROLES);
//        SqlWhereFormatter where = new SqlWhereFormatter();
//        where.append(PK_USERS, userid);
//        where.append("context", context);
//
//        Set<String> roles = new HashSet<>();
//        users.select((rs) -> {
//            roles.add(rs.getString("rolename"));
//        }, where.getWhereClause());
//
//        return roles;
//    }


    public static void deleteAllRoles(final Database db, final int userid) throws DatabaseException {
        Table users = db.getTable(TABLENAME_ROLES);
        users.deleteRows("userid = " + userid);
    }

    public static void deleteRole(final Database db, final int userid, final String role) throws DatabaseException {
        Table users = db.getTable(TABLENAME_ROLES);
        SqlWhereFormatter formatter = new SqlWhereFormatter();
        formatter.append("rolename", role);
        formatter.append("userid", userid);
        users.deleteRows(formatter.getWhereClause());
    }

    public static void addRole(final Database db, final int userid, final String context, final String role) throws DatabaseException {
        Table users = db.getTable(TABLENAME_ROLES);
        SqlInsertFormatter formatter = new SqlInsertFormatter();
        formatter.append(PK_USERS, userid)
            .append("context", context)
            .append("rolename", role);
        users.insertRow(formatter.getColumnClause(), formatter.getValueClause());
    }

    //deletes current roles then adds in the chosen roles
    public static void updateRoles(final Database db, final int userid, final String context, final Set<String> roles) throws DatabaseException {
        Table users = db.getTable(TABLENAME_ROLES);
        Set<String> current_roles = Global.INST.getUserService().getAllRolesForUserInContext(Integer.toString(userid), "context0");
        if (!roles.isEmpty()) {
            if (current_roles != null && !current_roles.isEmpty()) {
                for (String current_role : current_roles) {
                    deleteRole(db, userid, current_role);
                }
            }
            for (String role : roles) {
                SqlInsertFormatter formatter = new SqlInsertFormatter();
                formatter.append(PK_USERS, userid)
                    .append("context", context)
                    .append("rolename", role);
                users.insertRow(formatter.getColumnClause(), formatter.getValueClause());
            }
        } else {
            deleteAllRoles(db, userid);
        }
    }

//    public static boolean doesUserHaveRole(final Database db, final Integer userid, final String role, final String context)
//            throws DatabaseException {
//        if (userid == null) {
//            return false;
//        }
//
//        Table users = db.getTable(TABLENAME_ROLES);
//        SqlWhereFormatter where = new SqlWhereFormatter();
//        where.append(PK_USERS, userid);
//        where.append("context", context);
//        where.append("rolename", role);
//
//        return (users.getCount(where.getWhereClause()) > 0);
//    }


    //===================================
    // Organization stuff
    //===================================

    public static Organization readOrganization(final RecordSet rs) throws DatabaseException {
        Integer orgId = rs.getInteger(UserFactory.PK_ORG);
        if (orgId == null) {
            return null;
        }
        return new Organization(orgId, rs.getString("orgname"));
    }


    public static void saveOrganization(final Database db, final Organization organization) throws DatabaseException {
        Table table = db.getTable(TABLENAME_ORG);

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

    public static void deleteOrganization(final Database db, final int orgid) throws DatabaseException, Throwable {
        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append(UserFactory.PK_ORG, orgid);

        Table organizations = db.getTable(TABLENAME_ORG);
        Table users = db.getTable(TABLENAME_USERS);
        Table survey = db.getTable(TABLENAME_SURVEY);
        Table vessel = db.getTable(TABLENAME_VESSEL);

        if (users.getCount(where.getWhereClause()) > 0 ||
            survey.getCount(where.getWhereClause()) > 0 ||
            vessel.getCount(where.getWhereClause()) > 0) {
                throw new NotificationException("Cannot delete. This organization is currently in use.");
        }

        organizations.deleteRows(where.getWhereClause());
    }

    private static void fillOrgFormatter(final SqlFormatter formatter, final Organization organization) {
        formatter.append("orgname", organization.getName());
    }

    public static List<SimpleUser> readSimpleUsers(final Database db, final SqlStatement sql) throws DatabaseException {
        List<SimpleUser> users = new ArrayList<>();

        db.select(sql, (rs) -> {
            users.add(readSimpleUser(rs));
        });

        return users;
    }

    public static SimpleUser getProfiledUser(final Database db) throws DatabaseException {
        //
        // Weird (but cool) way to get random row but seems to work. Probably won't scale super well but we
        // can deal with that later.
        //
        SqlStatement sql = getUserStatement();
        sql.addCondition(new SpecialSqlCondition(ALIAS_USERS + ".statement IS NOT NULL"));
        sql.setOrderBy("random()");
        sql.setLimit(1);

        return db.selectFirst(sql, (rs) -> {
            return readSimpleUser(rs);
        });
    }

    public static String createPWResetToken(final Database db, final int userid) throws DatabaseException {
        //
        // This works by choosing 130 bits from a cryptographically secure random bit generator
        //  and encoding them in base-32
        //
        String token = new BigInteger(390, random).toString(32);

        SqlUpdateFormatter formatter = new SqlUpdateFormatter();
        formatter.append("prtoken", token)
            .append("prtimestamp", LocalDateTime.now().toString());

        db.getTable(TABLENAME_USERS).updateRow(formatter.getUpdateClause(), PK_USERS + " = " + userid);

        return token;
    }

    public static User verifyPRToken(final Database db, final String token) throws IllegalAccessException, DatabaseException {
        User user = UserFactory.getUserByPRToken(db, token);

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

    public static List<Organization> getOrganizations(final Database db) throws DatabaseException {
        List<Organization> orgs = new ArrayList<>();
        db.getTable(TABLENAME_ORG).select((rs) -> {
            orgs.add(readOrganization(rs));
        }, null, "orgname");
        return orgs;
    }

    public static void setLastLogin(final Database db, final User user) throws DatabaseException {
        SqlUpdateFormatter formatter = new SqlUpdateFormatter();
        formatter.append("lastlogin", System.currentTimeMillis());

        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append(PK_USERS, user.getId());

        db.getTable(TABLENAME_USERS).updateRow(formatter.getUpdateClause(), where.getWhereClause());
    }
}
