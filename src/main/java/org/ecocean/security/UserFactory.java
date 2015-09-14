package org.ecocean.security;

import java.util.HashSet;
import java.util.Set;

import org.ecocean.ContextConfiguration;
import org.ecocean.ShepherdPMF;
import org.ecocean.rest.SimpleFactory;
import org.ecocean.rest.SimpleUser;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class UserFactory {
//    private static Logger logger = LoggerFactory.getLogger(UserFactory.class);

    private static String TABLE_NAME = "users";
    private static String ROLE_TABLE_NAME = "userroles";

    private UserFactory() {
        // prevent instantiation
    }
//
//
//    public static User getUserById(final Integer id) {
//        try (Database db = ShepherdPMF.getDb()) {
//            return getUserById(db, id);
//        } catch (DatabaseException ex) {
//            logger.error("Can't get user.", ex);
//            return null;
//        }
//    }

    public static long getNumUsers(final Database db) throws DatabaseException {
        Table users = db.getTable(TABLE_NAME);
        return users.getCount(null);
    }

    public static User getUserById(final Database db, final Integer id) throws DatabaseException {
        if (id == null) {
            return null;
        }

        Table users = db.getTable(TABLE_NAME);
        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append("userid", id);
        RecordSet rs = users.getRecordSet(where.getWhereClause());

        if (rs.next()) {
            return UserFactory.readUser(rs);
        }

        return null;
    }


    public static User getUser(final Database db, final String username) throws DatabaseException {

        Table users = db.getTable(TABLE_NAME);
        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append("lower(username)", username.toLowerCase());
        RecordSet rs = users.getRecordSet(where.getWhereClause());

        if (rs.next()) {
            return readUser(rs);
        }

        return null;
    }

    public static User getUserByEmail(final Database db, final String email) throws DatabaseException {
        if (email == null) {
            return null;
        }

        Table users = db.getTable(TABLE_NAME);
        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append("lower(email)", email.toLowerCase());
        RecordSet rs = users.getRecordSet(where.getWhereClause());

        if (rs.next()) {
            return UserFactory.readUser(rs);
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
        SimpleUser su = SimpleFactory.readUser(rs);
        if (su == null) {
            return null;
        }

        User user = new User(su, rs.getString("email"));
        user.setAcceptedUserAgreement(rs.getBoolean("acceptedua"));
        user.setCreationDate(rs.getLocalDate("creationdate"));
        user.setLastLogin(rs.getLong("lastlogin"));
        user.setPhoneNumber(rs.getString("phonenumber"));
        user.setPhysicalAddress(rs.getString("physicaladdress"));
        user.setSaltAndHashedPass(rs.getString("salt"), rs.getString("password"));
        user.setAvatarid(rs.getInteger("avatarid"));
        user.setVerified(rs.getBoolean("verified"));
        return user;
    }


    public static void saveUser(final Database db, final User user) throws DatabaseException {
        Table table = db.getTable(TABLE_NAME);

        if (user.getUserId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillFormatter(formatter, user);

            user.setUserId(table.insertSequencedRow(formatter, "userid"));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillFormatter(formatter, user);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("userid", user.getUserId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private static void fillFormatter(final SqlFormatter formatter, final User user) {
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


    public static Set<String> getAllRolesForUserInContext(final Database db,
                                                          final int userid,
                                                          final String context) throws DatabaseException {
        Table users = db.getTable(ROLE_TABLE_NAME);
        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append("userid", userid);
        where.append("context", context);
        RecordSet rs = users.getRecordSet(where.getWhereClause());

        Set<String> roles = new HashSet<>();
        while (rs.next()) {
            roles.add(rs.getString("rolename"));
        }

        return roles;
    }


    public static void deleteRoles(final Database db, final int userid) throws DatabaseException {
        Table users = db.getTable(ROLE_TABLE_NAME);
        users.deleteRows("userid = " + userid);
    }


    public static void addRole(final Database db, final int userid, final String context, final String role) throws DatabaseException {
        Table users = db.getTable(ROLE_TABLE_NAME);
        SqlInsertFormatter formatter = new SqlInsertFormatter();
        formatter.append("userid", userid)
            .append("context", context)
            .append("rolename", role);
        users.insertRow(formatter.getColumnClause(), formatter.getValueClause());
    }

    public static boolean doesUserHaveRole(final Database db, final Integer userid, final String role, final String context)
            throws DatabaseException {
        if (userid == null) {
            return false;
        }

        Table users = db.getTable(ROLE_TABLE_NAME);
        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append("userid", userid);
        where.append("context", context);
        where.append("rolename", role);
        RecordSet rs = users.getRecordSet(where.getWhereClause());

        return (rs.next());
    }

    public static String getAllRolesForUserAsString(final Integer userid) throws DatabaseException {
        if (userid == null) {
            return "";
        }

        try (Database db = ShepherdPMF.getDb()) {
            Table users = db.getTable(ROLE_TABLE_NAME);
            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("userid", userid);
            RecordSet rs = users.getRecordSet(where.getWhereClause());

            String rolesFound = "";
            while (rs.next()) {
                String contextName = ContextConfiguration.getNameForContext(rs.getString("context"));
                rolesFound += contextName+":" + rs.getString("rolename") + "\r";
            }

            return rolesFound;
        }
    }
}
