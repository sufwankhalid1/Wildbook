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
%>

<html><head>
<title>importing from <%=sourceFile.toString()%></title>
</head><body>


<%


	Shepherd myShepherd=null;
	myShepherd = new Shepherd("context0");
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
				existed = true;
				i = fields.length + 1;
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
			//process and save these
			for (MediaAsset ma : mas) {
				out.println("<p>" + ma.toString() + "</p>");
/*
        			try {
            				ma.updateMetadata();
        			} catch (IOException ioe) {
            				//we dont care (well sorta) ... since IOException usually means we couldnt open file or some nonsense that we cant recover from
					System.out.println("could not updateMetadata() on " + ma);
        			}

	myShepherd.beginDBTransaction();
        MediaAssetFactory.save(ma, myShepherd);
	ma.updateStandardChildren(myShepherd);
	myShepherd.commitDBTransaction();
	out.println("<p>created <a target=\"_new\" title=\"" + ma.toString() + "\" href=\"obrowse.jsp?type=MediaAsset&id=" + ma.getId() + "\">" + ma.getId() + "</a></p>");
	System.out.println("localFilesToMediaAssets: " + f.toString() + " --> " + ma.getId());
*/

			}
		}
break;
	}


/*
for (File f : files) {
        JSONObject sp = astore.createParameters(f, grouping);
	sp.put("_localDirect", f.toString());
        MediaAsset ma = astore.find(sp, myShepherd);
	if (ma != null) {
		if (allowDuplicates) {
			System.out.println("NOTE: " + ma.toString() + " already exists matching " + sp.toString() + " but duplicates are being allowed");
		} else {
			out.println("<p><a target=\"_new\" title=\"" + ma.toString() + "\" href=\"obrowse.jsp?type=MediaAsset&id=" + ma.getId() + "\">[" + ma.getId() + "]</a> already exists for " + sp.toString() + "; skipping</p>");
			continue;
		}
	}

System.out.println("trying to create MediaAsset with sp = " + sp);
        try {
            ma = astore.copyIn(f, sp);
        } catch (IOException ioe) {
            out.println("<p>could not create MediaAsset for " + sp.toString() + ": " + ioe.toString() + "</p>");
            continue;
        }
        try {
            ma.updateMetadata();
        } catch (IOException ioe) {
            	//we dont care (well sorta) ... since IOException usually means we couldnt open file or some nonsense that we cant recover from
		System.out.println("could not updateMetadata() on " + ma);
        }
        ma.addLabel("_original");
	myShepherd.beginDBTransaction();
        MediaAssetFactory.save(ma, myShepherd);
	ma.updateStandardChildren(myShepherd);
	myShepherd.commitDBTransaction();
	out.println("<p>created <a target=\"_new\" title=\"" + ma.toString() + "\" href=\"obrowse.jsp?type=MediaAsset&id=" + ma.getId() + "\">" + ma.getId() + "</a></p>");
	System.out.println("localFilesToMediaAssets: " + f.toString() + " --> " + ma.getId());

}
*/


///////myShepherd.commitDBTransaction();
//myShepherd.closeDBTransaction();



%>


</body></html>
