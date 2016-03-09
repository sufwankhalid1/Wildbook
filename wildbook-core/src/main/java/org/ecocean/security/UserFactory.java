package org.ecocean.security;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ecocean.Organization;
import org.ecocean.location.GeoLocation;
import org.ecocean.location.LatLng;
import org.ecocean.rest.SimpleUser;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.GroupedSqlCondition;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlTable;
import com.samsix.database.SqlUpdateFormatter;

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
        addOrganization(sql);
        return sql;
    }

    private static void addOrganization(final SqlStatement sql) {
        sql.addLeftOuterJoin(ALIAS_USERS, UserFactory.PK_ORG, TABLENAME_ORG, ALIAS_ORG, UserFactory.PK_ORG);
        if (sql.hasSelect()) {
            sql.addSelectTable(ALIAS_ORG);
        }
    }

    public static void addAsLeftJoin(final String tableAlias, final String column, final SqlStatement sql) {
        sql.addLeftOuterJoin(tableAlias, column, TABLENAME_USERS, ALIAS_USERS, PK_USERS);
        if (sql.hasSelect()) {
            sql.addSelectTable(ALIAS_USERS);
        }

        addOrganization(sql);

    }

    public static SqlStatement getUserStatement(final boolean distinct) {
        SqlStatement sql = getUserStatement();
        sql.addSelectTable(ALIAS_USERS);
        sql.addSelectTable(ALIAS_ORG);
        sql.setSelectDistinct(true);
        return sql;
    }

    public static User readUser(final RecordSet rs) throws DatabaseException {
        if (!rs.hasColumn(PK_USERS)) {
            return null;
        }

        Integer id = rs.getInteger(PK_USERS);
        if (id == null) {
            return null;
        }

        User user = new User(id, rs.getString("username"), rs.getString("fullname"), rs.getString("email"));

        user.setAvatarPath(rs.getString("avatar"));
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

        user.setGeoLocation(readGeoLocation(rs));

        return user;
    }

    private static GeoLocation readGeoLocation(final RecordSet rs) throws DatabaseException {
        if (rs.getInteger("locserviceid") == null || ((Double) rs.getDouble("latitude") == null && (Double) rs.getDouble("longitude") == null)) {
            return null;
        }

        GeoLocation userlocation = new GeoLocation();
        LatLng latlng = new LatLng();

        userlocation.setCode(rs.getInteger("locserviceid").toString());
        userlocation.setCountry(rs.getString("country"));
        userlocation.setRegion(rs.getString("region"));
        userlocation.setSubregion(rs.getString("subregion"));

        latlng.setLatitude(rs.getDouble("latitude"));
        latlng.setLongitude(rs.getDouble("longitude"));

        userlocation.setLatlng(latlng);

        return userlocation;
    }

    public static SimpleUser readSimpleUser(final RecordSet rs) throws DatabaseException {
        User user = readUser(rs);
        if (user == null) {
            return null;
        }

        return user.toSimple();
    }

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

    public static List<SimpleUser> readSimpleUsers(final Database db, final SqlStatement sql) throws DatabaseException {
        List<SimpleUser> users = new ArrayList<>();

        db.select(sql, (rs) -> {
            users.add(readSimpleUser(rs));
        });

        return users;
    }
//
//    public static SimpleUser getProfiledUser(final Database db) throws DatabaseException {
//        //
//        // Weird (but cool) way to get random row but seems to work. Probably won't scale super well but we
//        // can deal with that later.
//        //
//        SqlStatement sql = getUserStatement();
//        sql.addCondition(new SpecialSqlCondition(ALIAS_USERS + ".statement IS NOT NULL"));
//        sql.setOrderBy("random()");
//        sql.setLimit(1);
//
//        return db.selectFirst(sql, (rs) -> {
//            return readSimpleUser(rs);
//        });
//    }

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

    public static SqlStatement userSearchStatement(final String query) {
        SqlStatement sql = UserFactory.getUserStatement();

        SqlTable users = sql.findTable(UserFactory.ALIAS_USERS);
        GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
        cond.addContainsCondition(users, "fullname", query);
        cond.addContainsCondition(users, "username", query);
        sql.addCondition(cond);

        return sql;
    }
}
