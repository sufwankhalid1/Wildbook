package org.ecocean.security;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.Organization;
import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.mmutil.StringUtilities;
import org.ecocean.rest.SimpleUser;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class UserFactory {
//    private static Logger logger = LoggerFactory.getLogger(UserFactory.class);

    public static String TABLENAME_USERS = "users";
    public static String TABLENAME_ROLES = "userroles";
    public static String TABLENAME_ORG = "organization";

    public static String AlIAS_USERS = "u";
    public static String ALIAS_ORG = "o";

    public static String PK_USERS = "userid";

    private UserFactory() {
        // prevent instantiation
    }

    public static SqlStatement getUserStatement() {
        SqlStatement sql = new SqlStatement(TABLENAME_USERS, AlIAS_USERS);
        sql.addLeftOuterJoin(AlIAS_USERS, "orgid", TABLENAME_ORG, ALIAS_ORG, "orgid");
        sql.addLeftOuterJoin(AlIAS_USERS,
                             "avatarid",
                             MediaAssetFactory.TABLENAME_MEDIAASSET,
                             MediaAssetFactory.ALIAS_MEDIAASSET,
                             MediaAssetFactory.PK_MEDIAASSET);
        return sql;
    }

    public static SqlStatement getUserStatement(final boolean distinct) {
        SqlStatement sql = getUserStatement();
        sql.setSelectString(AlIAS_USERS
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
        sql.addCondition(AlIAS_USERS, PK_USERS, SqlRelationType.EQUAL, id);
        RecordSet rs = db.getRecordSet(sql);

        if (rs.next()) {
            return UserFactory.readUser(rs);
        }

        return null;
    }


    public static User getUser(final Database db, final String username) throws DatabaseException {
        SqlStatement sql = getUserStatement();
        sql.addCondition(AlIAS_USERS, "username", SqlRelationType.EQUAL, username.toLowerCase()).setFunction("lower");
        RecordSet rs = db.getRecordSet(sql);

        if (rs.next()) {
            return readUser(rs);
        }

        return null;
    }

    public static User getUserByEmail(final Database db, final String email) throws DatabaseException {
        if (email == null) {
            return null;
        }

        SqlStatement sql = getUserStatement();
        sql.addCondition(AlIAS_USERS, "email", SqlRelationType.EQUAL, email.toLowerCase()).setFunction("lower");
        RecordSet rs = db.getRecordSet(sql);

        if (rs.next()) {
            return readUser(rs);
        }

        return null;
    }

    public static User getUserByNameOrEmail(final Database db, final String term) throws DatabaseException {
        if (term == null) {
            return null;
        }

        User user = UserFactory.getUser(db, term);
        if (user != null) {
            return user;
        }
        return UserFactory.getUserByEmail(db, term);
    }


    public static User readUser(final RecordSet rs) throws DatabaseException {
        Integer id = rs.getInteger(PK_USERS);
        if (id == null) {
            return null;
        }

        User user = new User(id, rs.getString("username"), rs.getString("fullname"), rs.getString("email"));

        MediaAsset ma = MediaAssetFactory.valueOf(rs);

        if (ma != null) {
            user.setAvatarid(rs.getInteger("avatarid"));
            user.setAvatar(UserFactory.getAvatar(ma.webPathString(), rs.getString("email")));
        }
        user.setStatement(rs.getString("statement"));
        user.setOrganization(readOrganization(rs));

        user.setAcceptedUserAgreement(rs.getBoolean("acceptedua"));
        user.setCreationDate(rs.getLocalDate("creationdate"));
        user.setLastLogin(rs.getLong("lastlogin"));
        user.setPhoneNumber(rs.getString("phonenumber"));
        user.setPhysicalAddress(rs.getString("physicaladdress"));
        user.setSaltAndHashedPass(rs.getString("salt"), rs.getString("password"));
        user.setVerified(rs.getBoolean("verified"));

        return user;
    }


    public static SimpleUser readSimpleUser(final RecordSet rs) throws DatabaseException {
        User user = UserFactory.readUser(rs);
        if (user == null) {
            return null;
        }

        return user.toSimple();
    }

    public static String getAvatar(final String avatar, final String email) {
        if (StringUtils.isBlank(avatar) && ! StringUtils.isBlank(email)) {
            //
            // Return 80x80 sized gravatar. They default to 80x80 but can be requested up to 2048x2048.
            // Though most users will have used a small image.
            // Feel free to change if you want it bigger as all the code on the browser side should
            // be sized to fit it's use anyway.
            // NOTE: d=identicon makes default (when not set by user) be those crazy (unique) geometric shapes, rather than the gravatar logo
            //         - https://en.wikipedia.org/wiki/Identicon
            //
            return "http://www.gravatar.com/avatar/"
                    + StringUtilities.getHashOf(email.trim().toLowerCase())
                    + "?s=80&d=identicon";
        }

        return avatar;
    }

    public static void saveUser(final Database db, final User user) throws DatabaseException {
        Table table = db.getTable(TABLENAME_USERS);

        if (user.getUserId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillUserFormatter(formatter, user);

            user.setUserId(table.insertSequencedRow(formatter, PK_USERS));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillUserFormatter(formatter, user);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_USERS, user.getUserId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private static void fillUserFormatter(final SqlFormatter formatter, final User user) {
        formatter.append("username", user.getUsername());
        formatter.append("fullname", user.getFullName());
        formatter.append("email", user.getEmail());
        formatter.append("lastlogin", user.getLastLogin());
        formatter.append("password", user.getHashedPass());
        formatter.append("salt", user.getSalt());
        formatter.append("phonenumber", user.getPhoneNumber());
        formatter.append("physicaladdress", user.getPhysicalAddress());
        formatter.append("avatarid", user.getAvatarid());
        formatter.append("acceptedua", user.getAcceptedUserAgreement());
        formatter.append("statement", user.getStatement());
        formatter.append("verified", user.isVerified());
    }


    //======================================
    // Role stuff
    //======================================

    public static Set<String> getAllRolesForUserInContext(final Database db,
                                                          final int userid,
                                                          final String context) throws DatabaseException {
        Table users = db.getTable(TABLENAME_ROLES);
        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append(PK_USERS, userid);
        where.append("context", context);
        RecordSet rs = users.getRecordSet(where.getWhereClause());

        Set<String> roles = new HashSet<>();
        while (rs.next()) {
            roles.add(rs.getString("rolename"));
        }

        return roles;
    }


    public static void deleteRoles(final Database db, final int userid) throws DatabaseException {
        Table users = db.getTable(TABLENAME_ROLES);
        users.deleteRows("userid = " + userid);
    }


    public static void addRole(final Database db, final int userid, final String context, final String role) throws DatabaseException {
        Table users = db.getTable(TABLENAME_ROLES);
        SqlInsertFormatter formatter = new SqlInsertFormatter();
        formatter.append(PK_USERS, userid)
            .append("context", context)
            .append("rolename", role);
        users.insertRow(formatter.getColumnClause(), formatter.getValueClause());
    }

    public static boolean doesUserHaveRole(final Database db, final Integer userid, final String role, final String context)
            throws DatabaseException {
        if (userid == null) {
            return false;
        }

        Table users = db.getTable(TABLENAME_ROLES);
        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append(PK_USERS, userid);
        where.append("context", context);
        where.append("rolename", role);
        RecordSet rs = users.getRecordSet(where.getWhereClause());

        return (rs.next());
    }


    //===================================
    // Organization stuff
    //===================================

    public static Organization readOrganization(final RecordSet rs) throws DatabaseException {
        Integer orgId = rs.getInteger("orgid");
        if (orgId == null) {
            return null;
        }
        return new Organization(orgId, rs.getString("name"));
    }


    public static void saveOrganization(final Database db, final Organization organization) throws DatabaseException {
        Table table = db.getTable(TABLENAME_ORG);

        if (organization.getOrgId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillOrgFormatter(formatter, organization);

            organization.setOrgId(table.insertSequencedRow(formatter, "orgid"));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillOrgFormatter(formatter, organization);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("orgid", organization.getOrgId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private static void fillOrgFormatter(final SqlFormatter formatter, final Organization organization) {
        formatter.append("name", organization.getName());
    }
}
