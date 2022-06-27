<%@ page contentType="text/html; charset=utf-8" language="java"
     import="org.ecocean.*,
java.util.Collection,
java.util.List,
java.util.ArrayList,
java.util.Map,
java.util.HashMap,
java.util.Set,
java.util.HashSet,
javax.jdo.*,
java.util.Arrays,
org.json.JSONObject,
org.json.JSONArray,
org.ecocean.MigrationUtil,
java.lang.reflect.*,
java.time.ZonedDateTime,
java.time.ZoneOffset,
java.io.BufferedReader,
java.io.FileReader,
java.io.File,

org.ecocean.api.ApiCustomFields,
org.ecocean.customfield.*,
org.ecocean.social.*,
org.ecocean.social.Relationship,

org.ecocean.media.*
              "
%><%!

public JSONObject typeRoleMap = new JSONObject();

private String filePreview(String name) throws java.io.IOException {
    File path = new File(MigrationUtil.getDir(), name);
    BufferedReader br = new BufferedReader(new FileReader(path));
    String content = "";
    String line;
    while ( ((line = br.readLine()) != null) && (content.length() < 3500) ) {
        content += line + "\n";
    }
    String rtn = content;
    if (rtn.length() > 3000) rtn = rtn.substring(0, 3000) + "\n\n   [... preview truncated ...]";
    return "<div>This file located at: <i class=\"code\">" + path.toString() + "</i><br /><textarea class=\"preview\">" + rtn + "</textarea></div>";
}


private String socialGroupSql(SocialUnit soc, Map<String,String> roleMap) {
    if (soc == null) return "";
    int size = Util.collectionSize(soc.getAllMembers());
    String sql = "\n-- " + soc.getSocialUnitName() + " (" + size + ")\n";
    if (size < 1) return sql;
    String guid = Util.generateUUID();
    String sqlIns = "INSERT INTO social_group (created, updated, viewed, guid, name) VALUES (now(), now(), now(), ?, ?);";
    sqlIns = MigrationUtil.sqlSub(sqlIns, guid);
    sqlIns = MigrationUtil.sqlSub(sqlIns, soc.getSocialUnitName());
    sql += sqlIns + "\n";
    for (Membership mem : soc.getAllMembers()) {
        String memSqlIns = "INSERT INTO social_group_individual_membership (created, updated, viewed, group_guid, individual_guid, roles) VALUES (?, now(), now(), ?, ?, ?);";
        //sqlIns = MigrationUtil.sqlSub(sqlIns, Util.millisToIso8601StringNoTimezone(st));
        //MarkedIndividual indiv = mem.getMarkedIndividual();
        String sdate = mem.getStartDate();
        if (sdate == null) {
            memSqlIns = MigrationUtil.sqlSub(memSqlIns, "now()", false);
        } else {
            memSqlIns = MigrationUtil.sqlSub(memSqlIns, sdate);
        }
        memSqlIns = MigrationUtil.sqlSub(memSqlIns, guid);
        memSqlIns = MigrationUtil.sqlSub(memSqlIns, mem.getMarkedIndividual().getId());
        String roles = "\"[]\"";
        String role = mem.getRole();
        if (role != null) {
            if (!roleMap.containsKey(role)) roleMap.put(role, Util.generateUUID());
            roles = "\"[\\\"" + roleMap.get(role) + "\\\"]\"";
        }
        memSqlIns = MigrationUtil.sqlSub(memSqlIns, roles);
        sql += memSqlIns + "\n";
    }
    return sql;
}


private String siteSettingSql(String key, JSONArray data) {
    if ((data == null) || (key == null)) return "";
    String sqlIns = "INSERT INTO site_setting (created, updated, key, public, data) VALUES (now(), now(), ?, 't', ?);";
    sqlIns = MigrationUtil.sqlSub(sqlIns, key);
    sqlIns = MigrationUtil.sqlSub(sqlIns, "\"" + data.toString().replaceAll("\"", "\\\\\"") + "\"");
    return "DELETE FROM site_setting WHERE key='" + key + "';\n" + sqlIns;
}

%><html>
<head>
    <title>Codex SocialGroup Migration Helper</title>
    <link rel="stylesheet" href="m.css" />
</head>
<body>
<p>
This will help migrate to Codex SocialGroups.
</p>




<%

MigrationUtil.checkDir();

Shepherd myShepherd = new Shepherd("context0");
myShepherd.beginDBTransaction();
ArrayList<SocialUnit> all = myShepherd.getAllSocialUnits();
myShepherd.rollbackDBTransaction();

if (all.size() < 1) {
    out.println("<h1>no SocialUnits to migrate</h1>");
    return;
}

String fname = "houston_10_indiv_socialgroups.sql";
MigrationUtil.writeFile(fname, "");

Map<String,String> roleMap = new HashMap<String,String>();
String content = "BEGIN;\n";
for (SocialUnit soc : all) {
    content += socialGroupSql(soc, roleMap);
}
content += "\n\nEND;\n";
MigrationUtil.appendFile(fname, content);

JSONArray roleData = new JSONArray();
for (String roleLabel : roleMap.keySet()) {
    JSONObject jr = new JSONObject();
    jr.put("multipleInGroup", true);  //meh?
    jr.put("label", roleLabel);
    jr.put("guid", roleMap.get(roleLabel));
    roleData.put(jr);
}
content = "BEGIN;\n\n" + siteSettingSql("social_group_roles", roleData) + "\n\nEND;\n";
MigrationUtil.appendFile(fname, content);

out.println(filePreview(fname));


System.out.println("migration/social_groups.jsp DONE");
%>


<p>done.</p>
</body></html>
