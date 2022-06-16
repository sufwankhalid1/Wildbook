<%@ page contentType="text/html; charset=utf-8" language="java"
     import="org.ecocean.*,
java.util.Collection,
java.util.List,
java.util.ArrayList,
javax.jdo.*,
java.util.Set,
java.util.HashSet,
java.util.Arrays,
java.util.Map,
java.util.HashMap,
org.json.JSONObject,
org.json.JSONArray,
org.ecocean.MigrationUtil,
java.lang.reflect.*,
org.ecocean.Util.MeasurementDesc,
org.ecocean.api.ApiCustomFields,
org.ecocean.configuration.*,
org.ecocean.customfield.*,

org.ecocean.media.*
              "
%><%!

private List<Encounter> findValueHolders(Shepherd myShepherd, CustomFieldValue cfv){
    Query q = myShepherd.getPM().newQuery(Encounter.class);
    q.setFilter("this.customFieldValues.contains(val)");
    q.declareImports("import org.ecocean.customfield.CustomFieldValue");
    q.declareParameters("CustomFieldValue val");
    Collection c = (Collection)q.execute(cfv);
    List<Encounter> all = new ArrayList<Encounter>(c);
    q.closeAll();
    return all;
}

%><html>
<head>
    <title>Codex move Encounter CFD to Sighting</title>
    <link rel="stylesheet" href="m.css" />
</head>
<body>

<%

String context = "context0";
Shepherd myShepherd = new Shepherd(context);
String cfdId = request.getParameter("id");
myShepherd.beginDBTransaction();

if (cfdId == null) {
    out.println("must pass id=cfdId");
    return;
}
CustomFieldDefinition cfd = CustomFieldDefinition.load(myShepherd, cfdId);
if (cfd == null) {
    out.println("invalid id");
    return;
}

String encCatId = MigrationUtil.getOrMakeCustomFieldCategory(myShepherd, "encounter", "Measurements");
String occCatId = MigrationUtil.getOrMakeCustomFieldCategory(myShepherd, "sighting", "Measurements");

JSONObject schema = cfd.toJSONObject().getJSONObject("schema");
if (!encCatId.equals(schema.optString("category"))) {
    out.println("must be in category: " + encCatId);
    return;
}

out.println("<h1>" + cfd.getName() + ": " + cfdId + "</h1>");
out.println(cfd.toJSONObject());
List<CustomFieldValue> values = myShepherd.getCustomFieldValuesForDefinition(cfd);
out.println("<p>usage: <b>" + Util.collectionSize(values) + "</b> encs</p><ol>");

// convert cfd to sighting/occurrence flavor
schema.put("category", occCatId);
JSONObject params = new JSONObject();
params.put("schema", schema);
cfd.setParameters(params);
cfd.setClassName("org.ecocean.Occurrence");

Set<String> already = new HashSet<String>();

int ct = 0;
for (CustomFieldValue cfv : values) {
    ct++;
    Object cfvValue = cfv.getValue();
    List<Encounter> encs = findValueHolders(myShepherd, cfv);
    for (Encounter enc : encs) {
        Occurrence occ = myShepherd.getOccurrence(enc);
        if (occ == null) throw new RuntimeException(enc + " has no occurrence");
        boolean dup = false;
        String occId = occ.getId();
        CustomFieldValue newVal = null;
        if (already.contains(occId)) {
            dup = true;
        } else {
            already.add(occId);
            newVal = CustomFieldValue.makeSpecific(cfd, cfvValue);
        }
        out.println("<li><b>[" + cfvValue + "]</b> occ " + (dup ? "{dup}" : "") + occId + ": enc " + enc.getId() + "</li>");
        System.out.println("(" + ct + "/" + Util.collectionSize(values) + ")  moving [" + cfd + "] [" + cfv + "]: occ " + (dup ? "+" : " ") + occId + " <- enc " + enc.getId());
        // if its already added to this occurrence, we skip any subsequent ones
        if (!dup) occ.addCustomFieldValue(newVal);
        enc.resetCustomFieldValues(cfd);  // clear it from encounter
    }
}
out.println("</ol>");

//update configuration to reflect changes in CustomFieldDefinitions
String[] classes = {"Encounter", "Occurrence"};
for (String cfcls : classes) {
    String key = "site.custom.customFields." + cfcls;
    ConfigurationUtil.setConfigurationValue(myShepherd, key, CustomFieldDefinition.getDefinitionsAsJSONObject(myShepherd, "org.ecocean." + cfcls));
}
ConfigurationUtil.resetValueCache("site");

myShepherd.commitDBTransaction();


%>
<h3>done</h3>


</body></html>
