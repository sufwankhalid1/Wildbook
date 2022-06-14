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

private Set<Encounter> findEncs(MediaAsset ma, Shepherd myShepherd) {
    Set<Encounter> encs = new HashSet<Encounter>();
    for (Annotation ann : ma.getAnnotations()) {
        Encounter enc = Encounter.findByAnnotation(ann, myShepherd);
        if (enc != null) encs.add(enc);
    }
    return encs;
}

%><html>
<head>
    <title>Codex CustomField LabeledKeyword choices PATCH</title>
    <link rel="stylesheet" href="m.css" />
</head>
<body>
<p>
Add choices to LabeledKeyword CFD
</p>
<hr />



<%

String context = "context0";
Shepherd myShepherd = new Shepherd(context);
boolean commit = Util.requestParameterSet(request.getParameter("commit"));
myShepherd.beginDBTransaction();

String encCatId = MigrationUtil.getOrMakeCustomFieldCategory(myShepherd, "encounter", "Labeled Keywords");

Map<String, List<String>> uiMap = LabeledKeyword.labelUIMapFromProperties(myShepherd, request);

List<CustomFieldDefinition> all = myShepherd.getAllCustomFieldDefinitions();
for (CustomFieldDefinition cfd : all) {
    JSONObject schema = cfd.toJSONObject().getJSONObject("schema");
    if (!encCatId.equals(schema.optString("category"))) continue;
    if (uiMap.containsKey(cfd.getName())) {
        schema.put("displayType", "select");
        JSONArray choices = new JSONArray();
        for (String label : uiMap.get(cfd.getName())) {
            JSONObject c = new JSONObject();
            c.put("label", label);
            c.put("value", label);
            choices.put(c);
        }
        schema.put("choices", choices);
        JSONObject params = new JSONObject();
        params.put("schema", schema);
        cfd.setParameters(params);
    }
    out.println("<p>" + cfd.toJSONObject() + "</p>");
    System.out.println("adding choices to " + cfd);
}


//update configuration to reflect changes in CustomFieldDefinitions
String[] classes = {"Encounter"};
for (String cfcls : classes) {
    String key = "site.custom.customFields." + cfcls;
    ConfigurationUtil.setConfigurationValue(myShepherd, key, CustomFieldDefinition.getDefinitionsAsJSONObject(myShepherd, "org.ecocean." + cfcls));
}
ConfigurationUtil.resetValueCache("site");

myShepherd.commitDBTransaction();

%>
<h3>done</h3>


</body></html>
