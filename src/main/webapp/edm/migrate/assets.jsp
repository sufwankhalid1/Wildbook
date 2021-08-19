<%@ page contentType="text/html; charset=utf-8" language="java"
     import="org.ecocean.*,
java.util.Collection,
java.util.List,
java.util.Iterator,
java.nio.file.Path,
java.util.ArrayList,
java.io.File,
javax.jdo.*,
java.util.Arrays,
java.util.Map,
java.util.HashMap,
java.util.Set,
java.util.UUID,
java.util.HashSet,
org.json.JSONObject,
org.json.JSONArray,
java.sql.Connection,
java.sql.PreparedStatement,
java.sql.DriverManager,
java.lang.reflect.*,
org.ecocean.MigrationUtil,
org.ecocean.Util.MeasurementDesc,
org.ecocean.api.ApiCustomFields,
org.ecocean.customfield.*,
org.ecocean.servlet.ServletUtilities,

org.ecocean.media.*
              "
%><%!

private String fileWriteAndPreview(String name, String content) throws java.io.IOException {
    String fname = "assets_" + name;
    File loc = MigrationUtil.writeFile(fname, content);
    String rtn = content;
    if (rtn.length() > 3000) rtn = rtn.substring(0, 3000) + "\n\n   [... preview truncated ...]";
    return "<div>This file located at: <i class=\"code\">" + loc.toString() + "</i><br /><textarea class=\"preview\">" + rtn + "</textarea></div>";
}

//ideally there would only be one (total) for any Asset, but this gets "the first one" it finds
private Occurrence getSomeOccurrence(Shepherd myShepherd, MediaAsset ma) {
    Encounter enc = null;
    for (Annotation ann : ma.getAnnotations()) {
        enc = ann.findEncounter(myShepherd);
        if (enc != null) break;
    }
    if (enc == null) {
        System.out.println("migration/assets.jsp could not find any Encounter for " + ma);
        return null;
    }
    Occurrence occ = myShepherd.getOccurrence(enc);
    if (occ == null) {
        System.out.println("migration/assets.jsp could not find any Occurrence for " + enc + " (via " + ma + ")");
        return null;
    }
    return occ;
}

/*
created        | 2021-08-10 18:27:46.077475
updated        | 2021-08-10 18:27:46.077588
viewed         | 2021-08-10 18:27:46.077628
guid           | 2b272728-f132-42cd-9347-559a74188347
version        | 
asset_guid     | e01f9cb2-0442-4f42-9068-244351e4b207
encounter_guid | 
jobs           | "null"
ia_class       | TEST
bounds         | "{\"rect\": [0, 2, 2170, 7120]}"


annotation_keywords
 created         | timestamp without time zone |           | not null | 
 updated         | timestamp without time zone |           | not null | 
 viewed          | timestamp without time zone |           | not null | 
 annotation_guid | uuid                        |           | not null | 
 keyword_guid    | uuid                        |           | not null | 

*/
private String annotSql(MediaAsset ma, Map<String,String> kmap) {
    if (!ma.hasAnnotations()) {
        return "-- no annots on " + ma + "\n";
    }
    String s = "";
    for (Annotation ann : ma.getAnnotations()) {
        String one = annotSingleSql(ann, ma) + "\n";
        s += one;
        if (one.contains("already processed")) continue;
        s += annotKeywords(ann, ma, kmap);
        s += "\n";
    }
    return s;
}

private Set<String> doneAnns = new HashSet<String>();

private String annotSingleSql(Annotation ann, MediaAsset ma) {
    if (doneAnns.contains(ann.getId())) return "-- already processed " + ann + "\n";
    String sqlIns = "INSERT INTO annotation (created, updated, viewed, guid, asset_guid, ia_class, bounds) VALUES (now(), now(), now(), ?, ?, ?, ?);";
    sqlIns = sqlSub(sqlIns, ann.getId());
    sqlIns = sqlSub(sqlIns, ma.getUUID());
    String iac = ann.getIAClass();
    if (iac == null) iac = "";
    sqlIns = sqlSub(sqlIns, iac);
    JSONObject bounds = new JSONObject();
    int[] bb = ann.getBbox();
    if (bb == null) {
        bounds.put("migrateBbox", false);
    } else {
        bounds.put("rect", new JSONArray(bb));
    }
    sqlIns = sqlSub(sqlIns, MigrationUtil.jsonQuote(bounds));
    doneAnns.add(ann.getId());
    return sqlIns;
}

//*every* annot gets all asset keywords -- long live next-gen
private String annotKeywords(Annotation ann, MediaAsset ma, Map<String,String> kmap) {
    String s = "";
    if (!ma.hasKeywords()) return s;
    Set<String> hasKw = new HashSet<String>();
    for (Keyword kw : ma.getKeywords()) {
        String kid = kmap.get(kw.getReadableName());
        if (kid == null) {
            s += "-- WARNING: no kmap id for " + kw.getReadableName() + " on " + ann + " (might be LabeledKeyword)\n";
            continue;
        }
        if (hasKw.contains(kid)) continue;
        hasKw.add(kid);
        String sqlIns = "INSERT INTO annotation_keywords (created, updated, viewed, annotation_guid, keyword_guid) VALUES (now(), now(), now(), ?, ?);";
        sqlIns = sqlSub(sqlIns, ann.getId());
        sqlIns = sqlSub(sqlIns, kid);
        s += sqlIns + "\n";
    }
    return s;
}

private String getRelPath(MediaAsset ma) {
    String prefix = ((LocalAssetStore)ma.getStore()).root().toString();
    return ma.localPath().toString().substring(prefix.length() + 1);
}

public static Connection getConnection(Shepherd myShepherd) throws java.sql.SQLException {
    //PersistenceManagerFactory pmf = myShepherd.getPMF("context0");
    //Connection con=DriverManager.getConnection(pmf.getConnectionURL(), pmf.getConnectionUserName()
    Connection conn = null;
    //java.util.Properties connectionProps = new java.util.Properties();
    //connectionProps.put("user", CommonConfiguration.getPropertyLEGACY("datanucleus.ConnectionUserName","context0"));
    //connectionProps.put("user", "wildbook");
    //connectionProps.put("password", CommonConfiguration.getPropertyLEGACY("datanucleus.ConnectionPassword","context0"));
    //connectionProps.put("password", "wildbook");
    //conn = DriverManager.getConnection(CommonConfiguration.getPropertyLEGACY("datanucleus.ConnectionURL","context0"), connectionProps);
    conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/mig_after");
    return conn;
}

private static String sqlSub(String inSql, String rep) {
    rep = rep.replaceAll("'", "''");
    return inSql.replaceFirst("\\?", "'" + rep + "'");
}

private static String sqlSub(String inSql, Integer rep) {
    return inSql.replaceFirst("\\?", rep.toString());
}


%><html>
<head>
    <title>Codex Asset Migration</title>
    <link rel="stylesheet" href="m.css" />
</head>
<body>
<p>
<h1>This will help migrate <b>Assets</b> to Codex.</h1>
</p>
<hr />

<p>
The following contents below can be used with <i class="code">rsync</i> to copy the files the codex server.  This should place the files
into a <i>temporary</i> location, which will be used in subsequent steps (below) to actually ingest the files as houston <b>Assets</b>.
</p>

<p>
In this example, the temporary directory (on the houston container) will be <i class="code">/data/migration/assets</i> and the contents
of below will be copied into a file <i class="code">/tmp/filelist</i>.  Due to the nature of
<i class="code">rsync</i>, this example only serves as a template and should be modified for specific needs.
</p>
<div class="code-block">
rsync -av --files-from=/tmp/filelist oldworld.wildbook.server:/var/lib/tomcat8/webapps/wildbook_data_dir/ /data/migration/assets
</div>

<%
MigrationUtil.setDir(request.getParameter("migrationDir"));
String checkDir = MigrationUtil.checkDir();
%>
<p>
migrationDir: <b><%=checkDir%></b>
</p>
<%


String sql = "SELECT * FROM \"MEDIAASSET\" ";
sql += "\"MEDIAASSET\" join \"MEDIAASSET_FEATURES\" on (\"ID\" = \"ID_OID\") ";
sql += "join \"ANNOTATION_FEATURES\" using (\"ID_EID\") ";
sql += "join \"ENCOUNTER_ANNOTATIONS\" on (\"ANNOTATION_FEATURES\".\"ID_OID\" = \"ENCOUNTER_ANNOTATIONS\".\"ID_EID\") ";
sql += "join \"ENCOUNTER\" on (\"ENCOUNTER_ANNOTATIONS\".\"ID_OID\" = \"ENCOUNTER\".\"ID\") ";
sql += "order by \"MEDIAASSET\".\"UUID\" ";
sql += "limit 30 ";

//out.println(sql);
//if (sql != null) return;
String context = "context0";
Shepherd myShepherd = new Shepherd(context);
boolean commit = Util.requestParameterSet(request.getParameter("commit"));
myShepherd.beginDBTransaction();

Query query = myShepherd.getPM().newQuery("javax.jdo.query.SQL", sql);
query.setClass(MediaAsset.class);
Collection c = (Collection)query.execute();
List<MediaAsset> allMA = new ArrayList<MediaAsset>(c);
query.closeAll();

Map<Occurrence, Set<MediaAsset>> agMap = new HashMap<Occurrence, Set<MediaAsset>>();

String content = "";
Set<Integer> done = new HashSet<Integer>();
for (MediaAsset ma : allMA) {
    if (done.contains(ma.getId())) continue;
    done.add(ma.getId());
    if (!ma.getStore().getType().equals(AssetStoreType.LOCAL)) continue;
    String path = getRelPath(ma);
    //out.println(ma.getId() + " - " + path);
    content += path + "\n";
    Occurrence occ = getSomeOccurrence(myShepherd, ma);
    if (occ == null) {
        content += "#### no occ for " + ma + " (should not happen if you had run occur.jsp)\n";
        continue;
    }

    Set<MediaAsset> occMas = agMap.get(occ);
    if (occMas == null) {
        occMas = new HashSet<MediaAsset>();
        agMap.put(occ, occMas);
    }
    occMas.add(ma);
}

out.println(fileWriteAndPreview("rsync_for_assets.filelist", content));
%>


<h2>Make dirs and copy files</h2>
<p>
This shell script will need the <b>directories</b> at the top modified to have the right locations, but will then copy the files
which were rsync'ed (above) into the proper final location for the houston assets.
</p>

<%
///Connection con = getConnection(myShepherd);

content = "### change these to appropriate directories\nTMP_ASSET_DIR=/data/migration/assets\nTARGET_DIR=/data/var/asset_group\n\n";
String allSql = "";
for (Occurrence occ : agMap.keySet()) {
    String subdir = occ.getId();
    content += "\nmkdir -p $TARGET_DIR/" + subdir + "/_asset_group\n";
    content += "mkdir $TARGET_DIR/" + subdir + "/_assets\n";

    String agSql = "INSERT INTO asset_group (created, updated, viewed, guid, major_type, description, owner_guid, config) VALUES (now(), now(), now(), ?, 'filesystem', 'Legacy migration', ?, '\"{}\"');";
    String userId = "88c3f1da-179b-40be-b186-5bd33ef7dc16";
    agSql = sqlSub(agSql, occ.getId());
    agSql = sqlSub(agSql, userId);
    allSql += agSql + "\n";

    String sqlIns = "INSERT INTO asset (created, updated, viewed, guid, extension, path, mime_type, magic_signature, size_bytes, filesystem_xxhash64, filesystem_guid, semantic_guid, title, meta, asset_group_guid) VALUES (now(), now(), now(), ?, ?, ?, ?, 'TBD', ?, '00000000', '00000000-0000-0000-0000-000000000000', ?, ?, ?, ?);";
    for (MediaAsset ma : agMap.get(occ)) {
        String path = getRelPath(ma);
        //out.println(ma);
        content += "cp -a $TMP_ASSET_DIR'/" + path + "' $TARGET_DIR/" + subdir + "/_asset_group/\n";
        String fname = ma.getFilename();
        int dot = fname.lastIndexOf(".");
        String ext = (dot < 0) ? "" : "." + fname.substring(dot + 1);
        content += "ln -s '../_asset_group/" + fname + "' $TARGET_DIR/" + subdir + "/_assets/" + ma.getUUID() + ext + "\n";
        String s = sqlIns;
        s = sqlSub(s, ma.getUUID());
        s = sqlSub(s, ext.substring(1));
        s = sqlSub(s, fname);
        s = sqlSub(s, "MIME");
        s = sqlSub(s, -1);
        s = sqlSub(s, Util.generateUUID());  //semantic guid -- needs to be unique????
        s = sqlSub(s, "Legacy MediaAsset id=" + ma.getId());
        s = sqlSub(s, "\"{}\"");  //meta (should include dimensions)
        s = sqlSub(s, occ.getId());
        allSql += s + "\n\n";
    }
}

out.println(fileWriteAndPreview("dirs_and_copy.sh", content));

/*
created                    | 2021-08-10 18:14:16.553598
updated                    | 2021-08-10 18:14:16.553643
viewed                     | 2021-08-10 18:14:16.553657
guid                       | f1bacade-f66b-4e39-9528-8d526bee002e
major_type                 | filesystem
commit                     | 
commit_mime_whitelist_guid | 
commit_houston_api_version | 
description                | Sighting.post fd7f02e3-eb37-4fec-8eb3-e129cd9e5d1e
owner_guid                 | 88c3f1da-179b-40be-b186-5bd33ef7dc16
submitter_guid             | 
config                     | "{}"



created             | 2021-08-16 22:46:38.025156
updated             | 2021-08-16 22:46:38.085569
viewed              | 2021-08-16 22:46:38.025208
guid                | d234bbbd-f536-4c0e-ac10-8f1bcff82171
extension           | jpg
path                | test.jpg
mime_type           | image/jpeg
magic_signature     | JPEG image data, JFIF standard 1.01, resolution (DPI), density 72x72, segment length 16, comment: "2021 All rights reserved. | wildbook.org | Flukebook.org | parent 78d42b24-02cf-4ef3-a878-a358", baseline, precision 8, 4032x3024, components 3
size_bytes          | 3618806
filesystem_xxhash64 | 1882783bb57fa8b9
filesystem_guid     | 3eb744e7-3ad7-e129-ed62-bee20fb27e4e
semantic_guid       | 929bd3cf-1ca4-aeaf-f496-0c0bce462a36
content_guid        | 
title               | 
description         | 
meta                | "{\"derived\": {\"width\": 4032, \"height\": 3024}}"
asset_group_guid    | e42fe051-9253-4390-abd9-2e7d35483872
*/
%>

<h2>Houston sql for AssetGroups and Assets</h2>
<p>
Now this sql will create the <b>AssetGroups</b> and <b>Assets</b> needed.
</p>

<%=fileWriteAndPreview("houston_assetgroups_assets.sql", "BEGIN;\n" + allSql + "\nEND;\n")%>

<h2>Keywords in houston</h2>
<p>
SQL to create the keywords in houston:
</p>

<%
content = "BEGIN;\n";
Map<String,String> kmap = new HashMap<String,String>();
List<LabeledKeyword> lkws = myShepherd.getAllLabeledKeywords();
List<Keyword> kws = myShepherd.getAllKeywordsList();
//TODO should we order by indexname to keep consistent?
for (Keyword kw : kws) {
    if (kmap.containsKey(kw.getReadableName())) continue;
    if (lkws.contains(kw)) {
        System.out.println("skipping LABELED Keyword " + kw);
        continue;
    }
    kmap.put(kw.getReadableName(), MigrationUtil.toUUID(kw.getIndexname()));
}

String sqlIns = "INSERT INTO keyword (created, updated, viewed, guid, value, source) VALUES (now(), now(), now(), ?, ?, 'user');";
for (String k : kmap.keySet()) {
    String s = sqlIns;
    s = sqlSub(s, kmap.get(k));
    s = sqlSub(s, k);
    content += s + "\n";
}
content += "\nEND;\n";

out.println(fileWriteAndPreview("houston_keywords.sql", content));

%>


<h2>Annotations/keywords in houston</h2>
<p>SQL for Annotations and keywords in houston</p>

<%
content = "BEGIN;\n";
for (MediaAsset ma : allMA) {
    content += annotSql(ma, kmap) + "\n";
}
content += "\nEND;\n";
myShepherd.rollbackDBTransaction();

out.println(fileWriteAndPreview("houston_annotations.sql", content));
%>


</body></html>
