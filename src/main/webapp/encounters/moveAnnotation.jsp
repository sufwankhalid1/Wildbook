<%@ page contentType="text/html; charset=utf-8" language="java"
     import="org.ecocean.*,
		org.ecocean.servlet.ServletUtilities,
		javax.jdo.Query,
		java.util.Iterator,
		java.util.List,
                java.util.Set,
                java.util.HashSet,
		org.json.JSONObject,
		org.ecocean.media.*,
		org.ecocean.Annotation,
		java.net.URLEncoder,
		java.nio.charset.StandardCharsets,
		java.io.UnsupportedEncodingException,
		org.ecocean.identity.IBEISIA
		"
%>

<jsp:include page="../header.jsp" flush="true"/>


<%

String aidparam = request.getParameter("assetId");
int assetId = -1;
try {
    assetId = Integer.parseInt(aidparam);
} 
catch (NumberFormatException nex) {}

if (!"tomcat".equals(AccessControl.simpleUserString(request))) {
    out.println("401 denied");
    return;
}

String context = ServletUtilities.getContext(request);
Shepherd myShepherd = new Shepherd(context);
myShepherd.setAction("manualAnnotation.jsp");
myShepherd.beginDBTransaction();


String annId = request.getParameter("annotationId");
String cloneOrigId = request.getParameter("clone");
String ontoId = request.getParameter("onto");

if ((annId != null) && ((cloneOrigId != null) || (ontoId != null)) ) {
    Annotation ann = myShepherd.getAnnotation(annId);
    if (ann == null) {
        out.println("Invalid <b>annotationId=</b> value");
        myShepherd.rollbackDBTransaction();
        myShepherd.closeDBTransaction();
        return;
    }
    Encounter origEnc = myShepherd.getEncounter(cloneOrigId);
    Encounter ontoEnc = myShepherd.getEncounter(ontoId);
    if ((origEnc == null) && (ontoEnc == null)) {
        out.println("Invalid <b>clone=</b> or <b>onto=</b> value");
        myShepherd.rollbackDBTransaction();
        myShepherd.closeDBTransaction();
        return;
    }

    Encounter check = ann.findEncounter(myShepherd);
    if (ontoEnc != null) {
        ontoEnc.addAnnotation(ann);
        ontoEnc.addComments("<p data-annot-id=\"" + ann.getId() + "\">Annotation " + annId + " moved here by " + AccessControl.simpleUserString(request) + "</p>");
        if (check != null) {
            check.removeAnnotation(ann);
        }
        myShepherd.updateDBTransaction();
        myShepherd.closeDBTransaction();
%>
<div>
Moved Annotation
<a target="_new" href="../obrowse.jsp?type=Annotation&id=<%=ann.getId()%>" title="<%=ann.toString()%>"><%=ann.getId()%></a>
onto
<a target="_new" href="encounter.jsp?number=<%=ontoEnc.getCatalogNumber()%>" title="<%=ontoEnc.toString()%>"><%=ontoEnc.getCatalogNumber()%></a>
</div>
<%
        return;
    }

    //falls thru means we clone
    if ((check == null) || !check.getCatalogNumber().equals(origEnc.getCatalogNumber())) {
        out.println("Annotation does not appear to be attached to Encounter");
        myShepherd.rollbackDBTransaction();
        myShepherd.closeDBTransaction();
        return;
    }

    Encounter clone = origEnc.cloneWithoutAnnotations();
    clone.addAnnotation(ann);
    clone.addComments("<p data-annot-id=\"" + ann.getId() + "\">Encounter " + cloneOrigId + " cloned and Annotation " + annId + " moved onto it by " + AccessControl.simpleUserString(request) + "</p>");
    myShepherd.getPM().makePersistent(clone);
    origEnc.removeAnnotation(ann);

    Occurrence occ = myShepherd.getOccurrence(origEnc);
    if (occ != null) {
        occ.addEncounterAndUpdateIt(clone);
        occ.setDWCDateLastModified();
    } else {
        occ = new Occurrence(Util.generateUUID(), clone);
        occ.addEncounter(origEnc);
        myShepherd.getPM().makePersistent(occ);
    }
    myShepherd.updateDBTransaction();
    myShepherd.closeDBTransaction();
%>
<div>
New encounter
<a target="_new" href="../obrowse.jsp?type=Encounter&id=<%=clone.getCatalogNumber()%>" title="<%=clone.toString()%>"><%=clone.getCatalogNumber()%></a>
created (cloned) from
<a target="_new" href="../obrowse.jsp?type=Encounter&id=<%=origEnc.getCatalogNumber()%>" title="<%=origEnc.toString()%>"><%=origEnc.getCatalogNumber()%></a>
and moved <%=ann%> to clone.
</div>
<%
    return;
}


MediaAsset ma = MediaAssetFactory.load(assetId, myShepherd);
if (ma == null) {
    out.println("Invalid <b>assetId=</b> value");
    myShepherd.rollbackDBTransaction();
    myShepherd.closeDBTransaction();
    return;
}

List<Annotation> annots = ma.getAnnotations();
if (Util.collectionIsEmptyOrNull(annots)) {
    out.println("No annotations");
    myShepherd.rollbackDBTransaction();
    myShepherd.closeDBTransaction();
    return;
}

%>
<p>
   <a target="_new" href="../obrowse.jsp?type=MediaAsset&id=<%=assetId%>"><%=ma%></a>
</p>
<ul>
<%
Set<Encounter> encs = new HashSet<Encounter>();
for (Annotation ann : annots) {
    Encounter enc = ann.findEncounter(myShepherd);
    if (enc == null) continue;
    encs.add(enc);
    Occurrence occ = myShepherd.getOccurrence(enc);
    if ((occ != null) && !Util.collectionIsEmptyOrNull(occ.getEncounters())) {
        for (Encounter e : occ.getEncounters()) {
            encs.add(e);
        }
    }
}
for (Annotation ann : annots) {
    Encounter enc = ann.findEncounter(myShepherd);
%>
<li>
<b><%=ann%></b><%=((enc == null) ? "" : " on enc=" + enc.getCatalogNumber())%>
<ul>
    <% if (enc != null) { %>
    <li><a href="moveAnnotation.jsp?annotationId=<%=ann.getId()%>&clone=<%=enc.getCatalogNumber()%>">move</a> to <b><i>new clone</i></b> of <%=enc.getCatalogNumber()%></li>
    <% } %>
<%
    for (Encounter enc2 : encs) {
        if (enc.getCatalogNumber().equals(enc2.getCatalogNumber())) continue;
        %>
        <li><a href="moveAnnotation.jsp?annotationId=<%=ann.getId()%>&onto=<%=enc2.getCatalogNumber()%>">move</a> <b>onto</b> existing <%=enc2%></li>
        <%
    }
%>
</ul>
</li>
<%
}

out.println("</ul>");
myShepherd.rollbackDBTransaction();
myShepherd.closeDBTransaction();


%>
