package org.ecocean.identity;

import java.util.List;
import java.io.File;
import java.io.IOException;
import org.ecocean.Shepherd;
import org.ecocean.Encounter;
import org.ecocean.Annotation;
import org.ecocean.Util;
import org.ecocean.CommonConfiguration;
import org.ecocean.media.MediaAsset;
import org.json.JSONObject;

/*
import org.ecocean.ImageAttributes;
import org.ecocean.Util;
import org.ecocean.Shepherd;
import org.ecocean.Encounter;
import org.ecocean.Occurrence;
import org.ecocean.MarkedIndividual;
import org.ecocean.servlet.ServletUtilities;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import java.net.URL;
import org.ecocean.media.*;
import org.ecocean.RestClient;
import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import org.joda.time.DateTime;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.http.HttpServletRequest;
*/
import java.util.ArrayList;
import java.util.Collection;
import java.io.FileWriter;
import java.io.BufferedWriter;
import javax.jdo.Extent;
import javax.jdo.Query;


public class BenWhiteshark {
//#BenWhitesharkJobStartDirectory = /efs/job/start
//#BenWhitesharkJobResultsDirectory = /efs/job/results


    public static boolean enabled() {
        return ((getJobStartDir() != null) && (getJobResultsDir() != null));
    }
    public static JSONObject iaStatus() {
        JSONObject j = new JSONObject();
        j.put("enabled", enabled());
        return j;
    }

    //right now we use .isExemplar on Annotations; but later we may shift to some other logic, including (discussed with ben):
    //  quality keywords on image, features approved/input by manual method (e.g. end-points of fin) etc....
    //  also: the choice to focus on Annotation vs MediaAsset feels a little arbitrary; am choosing Annotation... for now?
    public static List<Annotation> getExemplars(Shepherd myShepherd) {
        Extent all = myShepherd.getPM().getExtent(Annotation.class, true);
        Query qry = myShepherd.getPM().newQuery(all, "isExemplar");
        Collection results = (Collection)qry.execute();
        List<Annotation> rtn = new ArrayList<Annotation>();
        for (Object o : results) {
            Annotation ann = (Annotation)o;
            if (ann.getMediaAsset() != null) rtn.add(ann);
        }
        return rtn;
    }

    public static File getJobStartDir() {
        String d = CommonConfiguration.getProperty("BenWhitesharkJobStartDirectory", "context0");
        if (d == null) return null;
        return new File(d);
    }
    public static File getJobResultsDir() {
        String d = CommonConfiguration.getProperty("BenWhitesharkJobResultsDirectory", "context0");
        if (d == null) return null;
        return new File(d);
    }

    //TODO support taxonomy!
    public static String startJob(List<MediaAsset> queryMAs, Shepherd myShepherd) {
        List<Annotation> exs = getExemplars(myShepherd);
        if ((exs == null) || (exs.size() < 1)) throw new RuntimeException("getExemplars() returned no results");
        List<MediaAsset> tmas = new ArrayList<MediaAsset>();
        for (Annotation ann : exs) {
            if (!queryMAs.contains(ann.getMediaAsset())) tmas.add(ann.getMediaAsset());
        }
        return startJob(queryMAs, tmas);
    }
    //single queryMA convenience method
    public static String startJob(MediaAsset queryMA, Shepherd myShepherd) {
        List<MediaAsset> mas = new ArrayList<MediaAsset>();
        mas.add(queryMA);
        return startJob(mas, myShepherd);
    }
    public static String startJob(List<MediaAsset> queryMAs, List<MediaAsset> targetMAs) {
        String taskId = Util.generateUUID();
        String contents = "";
        for (MediaAsset ma : queryMAs) {
            contents += jobdata(ma);
        }
        contents += "-1\t-1\t-1\t-1\n";   //agreed divider between queryMA(s) and targetMA(s)
        for (MediaAsset ma : targetMAs) {
            contents += jobdata(ma);
        }
        writeFile(taskId, contents);
        return taskId;
    }

    static String jobdata(MediaAsset ma) {
        if (ma == null) return "# null MediaAsset passed\n";
        Shepherd myShepherd = new Shepherd("context0");
        //i guess technically we only need encounter to get individual... which maybe we dont need?
        Encounter enc = null;
        for (Annotation ann : ma.getAnnotations()) {
            enc = Encounter.findByAnnotation(ann, myShepherd);
            if (enc != null) break;
        }
        if (enc == null) return "#unable to find Encounter for " + ma.toString() + "; skipping\n";
        //yup, this assumes LocalAssetStore, but thats our magic here
	String filePathString = ma.localPath().toString();
        String pathReplaceRegex = CommonConfiguration.getProperty("BenWhitesharkMediaAssetPathReplaceRegex", "context0");
        String pathReplaceValue = CommonConfiguration.getProperty("BenWhitesharkMediaAssetPathReplaceValue", "context0");
	if ((pathReplaceRegex != null) && (pathReplaceValue != null)) {
		filePathString = filePathString.replace(pathReplaceRegex, pathReplaceValue);
	}
        return ma.getUUID() + "\t" + filePathString + "\t" + (enc.hasMarkedIndividual() ? enc.getIndividualID() : "-1") + "\t" + enc.getCatalogNumber() + "\n";
    }

    static void writeFile(String taskId, String contents) {
        File dir = getJobStartDir();
        if (dir == null) throw new RuntimeException("no defined BenWhitesharkJobStartDirectory");
        File ftmp = new File(dir, taskId + ".tmp");  //dissuade race condition of reading before done
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(ftmp));
            writer.write(contents);
            writer.close();
            ftmp.renameTo(new File(dir, taskId));
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }
}
