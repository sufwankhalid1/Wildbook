<%@ page contentType="text/html; charset=utf-8" language="java"
     import="org.ecocean.*,
org.ecocean.servlet.ServletUtilities,
org.ecocean.media.*,
java.util.Map,
java.io.BufferedReader,
java.io.IOException,
java.io.InputStream,
java.io.InputStreamReader,
java.io.File,
java.util.Collection,
java.util.List,
java.util.ArrayList,
org.json.JSONObject,
javax.jdo.Query,
javax.jdo.Extent,
java.util.HashMap,

org.ecocean.Annotation,
org.ecocean.media.*
              "
%>

<%!
	private List<File> inDir(File d, boolean recurse) {
		List<File> files = new ArrayList<File>();
		if (!d.isDirectory()) return files;
		for (final File f : d.listFiles()) {
			if (f.isDirectory()) {
				if (recurse) files.addAll(inDir(f, recurse));
			} else {
				files.add(f);
			}
		}
		return files;
	}
%>

<%

	File sourceFile = new File("/efs/import/data.txt");
	String grouping = "A";
	boolean reuseMediaAssets = true;  //probably want false
%>

<html><head>
<title>importing from <%=sourceFile.toString()%></title>
</head><body>


<%


	Shepherd myShepherd=null;
	String context = "context0";
	myShepherd = new Shepherd(context);
        AssetStore astore = AssetStore.getDefault(myShepherd);
	FeatureType.initAll(myShepherd);

	List<String> dataIn = Util.readAllLines(sourceFile);
	for (String row : dataIn) {
		String[] fields = row.split("\t");
		boolean existed = false;
		List<MediaAsset> mas = new ArrayList<MediaAsset>();
		for (int i = 8 ; i < fields.length ; i++) {
			File f = new File(fields[i]);
			if (!f.exists()) {
				System.out.print(f.toString() + " does not exists; skipping");
				continue;
			}
        		JSONObject sp = astore.createParameters(f, grouping + "/" + f.toString().hashCode());
System.out.println(f.toString() + " --> " + sp.toString());
        		MediaAsset ma = astore.find(sp, myShepherd);
			if (ma != null) {
				if (reuseMediaAssets) {
					mas.add(ma);
				} else {
					existed = true;
					i = fields.length + 1;
				}
			} else {
        			try {
            				ma = astore.copyIn(f, sp);
        			} catch (IOException ioe) {
					System.out.println("failed copyIn of " + f + ": " + ioe.toString());
				}
				if (ma != null) {
        				ma.addLabel("_original");
					mas.add(ma);
				}
			}
		}
		if (existed) {
			out.println("<p>MediaAsset(s) already existed for this row, so skipping. <br /><pre>" + row + "</pre></p>");
		} else {
			myShepherd.beginDBTransaction();
			//process and save these
			ArrayList<Annotation> anns = new ArrayList<Annotation>();
			for (MediaAsset ma : mas) {
				out.println("<p>" + ma.toString() + "</p>");
        			try {
            				ma.updateMetadata();
        			} catch (IOException ioe) {
            				//we dont care (well sorta) ... since IOException usually means we couldnt open file or some nonsense that we cant recover from
					System.out.println("could not updateMetadata() on " + ma);
        			}

        			MediaAssetFactory.save(ma, myShepherd);
				ma.updateStandardChildren(myShepherd);
				out.println("<p>created <a target=\"_new\" title=\"" + ma.toString() + "\" href=\"obrowse.jsp?type=MediaAsset&id=" + ma.getId() + "\">" + ma.getId() + "</a></p>");
				Annotation ann = new Annotation("Carcharodon carcharias", ma);
				anns.add(ann);
			}
			Encounter enc = new Encounter(anns);
			enc.setYear(Integer.parseInt(fields[0].substring(0,4)));
			enc.setMonth(Integer.parseInt(fields[0].substring(4,6)));
			enc.setDay(Integer.parseInt(fields[0].substring(6,8)));
			enc.setHour(0);
			enc.setMinutes("00");
			String sex = null;
			if (!fields[1].equals("NA")) sex = fields[1].toLowerCase();
			enc.setSex(sex);
			enc.setRecordedBy(fields[2]);
			enc.setVerbatimLocality(fields[3]);
			enc.setDecimalLatitude(Double.parseDouble(fields[6]));
			enc.setDecimalLongitude(Double.parseDouble(fields[7]));
			enc.setState("approved");

			String indivId = fields[5];
			MarkedIndividual indiv = myShepherd.getMarkedIndividualQuiet(indivId);
			if (indiv == null) {
				indiv = new MarkedIndividual(indivId, enc);
				myShepherd.storeNewMarkedIndividual(indiv);
				System.out.println("+ created new " + indiv);
			} else {
				indiv.addEncounter(enc, context);
			}
			enc.setIndividualID(indivId);
			myShepherd.storeNewEncounter(enc, enc.getCatalogNumber());
			System.out.println("+ created " + enc);
			out.println("<p>created <a target=\"_new\" title=\"" + enc.toString() + "\" href=\"obrowse.jsp?type=Encounter&id=" + enc.getCatalogNumber() + "\">" + enc.getCatalogNumber() + "</a></p>");
			myShepherd.commitDBTransaction();
		}
break;
	}



///////myShepherd.commitDBTransaction();
//myShepherd.closeDBTransaction();



%>


</body></html>
