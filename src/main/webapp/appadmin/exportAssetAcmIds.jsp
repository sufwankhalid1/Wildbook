<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,
org.ecocean.datacollection.*,
java.util.*,
org.ecocean.media.*,
org.ecocean.ia.*,
org.ecocean.identity.*,
org.ecocean.*,
org.json.JSONObject,
javax.jdo.*,
java.io.BufferedWriter,
java.io.File,
java.io.FileWriter,
java.io.IOException,
java.nio.file.Files,
java.nio.file.Path,
java.nio.file.Paths,
java.io.*,java.util.*, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%

String context="context0";
context=ServletUtilities.getContext(request);

Shepherd myShepherd=new Shepherd(context);
myShepherd.setAction("getImportACMIds.jsp");
String filter = "this.submitterOrganization == 'Olive Ridley Project' && this.dwcDateAdded.startsWith('2019-08-19') ";
Collection c = null;
List<Encounter> encs = null;
try {
    Extent encClass = myShepherd.getPM().getExtent(Encounter.class, true);
    Query acceptedEncounters = myShepherd.getPM().newQuery(encClass, filter);
    c = (Collection) (acceptedEncounters.execute());
    encs = new ArrayList<>(c);          
    //int size=c.size();
    acceptedEncounters.closeAll();
        
} catch (Exception e) {
    System.out.println("Exception retrieving encounters...");
    e.printStackTrace();
}
System.out.println();
System.out.println("Retrieving ACM Id's for "+encs.size()+" encounters. ");
System.out.println("Filter = "+filter);
System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
System.out.println();

%>

<html>
<head>
<title>Get ACM Ids for Import</title>

</head>

<body>
<ul>
<%


try {
    int hasHead = 0;
    //int isTrivial = 0;
    int hasAcmId = 0;
    int allMAs = 0;	

    List<Annotation> trivs = new ArrayList<>();
    List<String> ids = new ArrayList<>();
    List<String> missing = new ArrayList<>();
    for (Encounter enc : encs) {
    	//System.out.println("Count: "+allAnns);
        List<MediaAsset> mas = enc.getMedia();
        if (mas.size()>0) {
            for (MediaAsset ma : mas) {
                allMAs++;
                //if (ma!=null) {
                //    isTrivial++;
                //    trivs.add(ann);
                //    continue;
                //}

                //String iaClass = ann.getAcmId();
                //if (iaClass!=null&&(iaClass.equals("turtle_green+head")||iaClass.equals("turtle_hawksbill+head"))) hasHead++;
                //allAnns++;
		        if (ma.getAcmId()!=null) {
			        hasAcmId++;
		            System.out.println("Good ID: "+ma.getAcmId());
			        ids.add((ma.getAcmId());	
		        } else {
                    System.out.println("Missing: "+String.valueOf(ma.getId()));
                    missing.add(String.valueOf(ma.getId()));
                }
            }
        }
    }

    System.out.println();
    System.out.println("Total MAs= "+allMAs+" MAs with ACMId = "+ids.size());
    System.out.println();
            BufferedWriter bw = null;
            Path dir = Paths.get("webapps/wildbook_data_dir/idDump/");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            File f = new File("webapps/wildbook_data_dir/idDump/", "acmIds2019-08-19.csv");
            f.getAbsolutePath();
            if (!f.exists()) {
                f.createNewFile();
            }
            System.out.println("Isfile? "+f.isFile()+"  IsDirectory? "+f.isDirectory()+" ABS Path: "+f.getAbsolutePath());
            bw = new BufferedWriter(new FileWriter(f));

            for (String id : ids) {
                bw.write(id);
                bw.newLine();
            }

            bw.flush();

            File m = new File("webapps/wildbook_data_dir/idDump/", "MISSING-acmIds2019-08-19.csv");
            m.getAbsolutePath();
            if (!m.exists()) {
                m.createNewFile();
            }
            System.out.println("Isfile? "+m.isFile()+"  IsDirectory? "+m.isDirectory()+" ABS Path: "+m.getAbsolutePath());
            bw = new BufferedWriter(new FileWriter(m));

            for (String missed : missing) {
                bw.write(missed);
                bw.newLine();
            }

            bw.close();
    System.out.println("Should be done with ACMId csv output!");


} catch (Exception e){
	myShepherd.rollbackDBTransaction();
	e.printStackTrace();
}

myShepherd.closeDBTransaction();



%>

</ul>

</body>
</html>
