/*
 * Wildbook - A Mark-Recapture Framework
 * Copyright (C) 2017 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ecocean.plugins.WildbookIA;

import java.util.UUID;
import java.net.URL;
import java.util.Collections;
import org.ecocean.Annotation;
import org.ecocean.Util;
import org.ecocean.RestClient;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.identity.IBEISIA;
import java.util.HashMap;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.builder.ToStringBuilder;

/*
    note: both Annotmatch and Staging (which extends Annotmatch) have a uuid as primary key, but *constraints* as follow:
        Annotmatch: The 2-tuple (match_annot_uuid1, match_annot_uuid2) should uniquely identify a row in this table.
        Staging: The 3-tuple (review_annot_uuid1, review_annot_uuid2, review_count) should uniquely identify a row in this table. 
    these constraints should be reflected (enforced) in the db -- hopefully we can do that via datanucleus setup???  TODO
*/
public class Annotmatch implements java.io.Serializable {
    private enum EvidenceDecisionValue { MATCH, NOMATCH, NOTCOMP, UNKNOWN };
    private enum MetaDecisionValue { SAME, DIFFERENT };

    private static String urlNameAdderAnnotmatch = "IBEISIARestUrlV2MatchAdder";
    private static String urlNameAdderStaging = "IBEISIARestUrlV2ReviewAdder";
    private static String urlNameReviewIdentify = "IBEISIARestUrlIdentifyV2Review";
    private static String urlNameStartIdentify = "IBEISIARestUrlStartIdentifyV2Annotations";

    private static String CALLBACK_GRAPH_START_REVIEW = "graph_start_review";
    private static String CALLBACK_GRAPH_START_FINISHED = "graph_start_finished";
    private static String CALLBACK_GRAPH_REVIEW_FORM = "graph_review_form";

    //to know if we think IA already has these
    private static HashMap<UUID,Boolean> alreadySent = new HashMap<UUID,Boolean>();

    //TODO i think(?) this is "the" graph infr id for our species. (TODO add multiple species support?)
    private static String INFR_UUID = null;


    private UUID id;  //TODO java.util.UUID ?

    private Annotation annot1;
    private Annotation annot2;

    private String evidenceDecision;  //match|nomatch|notcomp|unknown|NULL
    private String metaDecision;  //same|different|NULL
    private String tags;  //semicolon-delimited
    private Double confidence;
    private String userId;
    protected int count;
    private long timestampModified;


    public Annotmatch() {}  //empty for jdo

    public Annotmatch(Annotation annotA, Annotation annotB) {
        this.id = UUID.randomUUID();
        if ((annotA == null) || (annotB == null)) throw new RuntimeException("Both Annotations must be non-null.");
        this.annot1 = annotSort(annotA, annotB, false);
        this.annot2 = annotSort(annotA, annotB, true);
        this.setTimestampModified();
        this.count = 0;
    }

    public UUID getId() {
        return id;
    }

    public Annotation getAnnot1() {
        return annot1;
    }
    public Annotation getAnnot2() {
        return annot2;
    }


    public void setEvidenceDecision(String s) {
        evidenceDecision = verifyEvidenceDecisionValue(s);
    }
    public String getEvidenceDecision() {
        return evidenceDecision;
    }

    public void setMetaDecision(String s) {
        metaDecision = verifyMetaDecisionValue(s);
    }
    public String getMetaDecision() {
        return metaDecision;
    }

    public void setTags(String s) {
        tags = s;
    }
    public String getTags() {
        return tags;
    }
    public void addTag(String s) {
        if (s == null) return;
        tags = (tags == null) ? s : tags + ";" + s;
    }
    public String[] getTagsAsArray() {
        if (tags == null) return null;
        return tags.split(";");
    }


    public void setUserId(String s) {
        userId = s;
    }
    public String getUserId() {
        return userId;
    }

    public void setConfidence(Double s) {
        confidence = s;
    }
    public Double getConfidence() {
        return confidence;
    }

    public void setTimestampModified() {
        timestampModified = System.currentTimeMillis();
    }
    public long getTimestampModified() {
        return timestampModified;
    }

    public void setCount(int c) {
        count = c;
    }
    public int incrementCount() {
        return count++;
    }
    public int getCount() {
        return count;
    }


    //returns the "smallest" annot (id) or "largest" if reverse==true
    private Annotation annotSort(Annotation annotA, Annotation annotB, boolean reverse) {
        if ((annotA == null) || (annotB == null) || (annotA.getId() == null) || (annotB.getId() == null)) return null;
        int c = annotA.getId().compareTo(annotB.getId());
        if (c == 0) throw new RuntimeException("annotSort() received identical Annotation IDs! " + annotA.getId());
        return (((c < 0) && !reverse) || ((c > 0) && reverse)) ? annotA : annotB;
    }

    private String verifyEvidenceDecisionValue(String in) {
        if (in == null) return null;
        for (EvidenceDecisionValue val : EvidenceDecisionValue.values()) {
            if (in.equals(val.toString().toLowerCase())) return in;
        }
        System.out.println("WARNING: verifyEvidenceDecisionValue() was given value '" + in + "' which was invalid; returning null");
        return null;
    }

    private String verifyMetaDecisionValue(String in) {
        if (in == null) return null;
        for (MetaDecisionValue val : MetaDecisionValue.values()) {
            if (in.equals(val.toString().toLowerCase())) return in;
        }
        System.out.println("WARNING: verifyMetaDecisionValue() was given value '" + in + "' which was invalid; returning null");
        return null;
    }

    public boolean alreadySentToIA() {
        if (alreadySent.get(this.getId()) == null) return false;
        return alreadySent.get(this.getId());
    }

/*  not so sure we ever need to send a single one?  lets use multiple via static?
    public boolean sendToIA() {
        return sendToIA(false);
    }
    public boolean sendToIA(boolean force) {
        if (!force && this.alreadySentToIA()) return true;
        //TODO actual send and set alreadySent
        return true;
    }
*/

    //this gets a little hacky to handle also Staging.  :( 
    public static JSONObject sendToIA(List<Annotmatch> list, String context) {
        if ((list == null) || (list.size() < 1)) return null;
        boolean isStaging = (list.get(0) instanceof Staging);
        JSONObject rtn = null;
        HashMap<String,Object> data = null;
        try {
            rtn = Comm.post(isStaging ? urlNameAdderStaging : urlNameAdderAnnotmatch, context, adderData(list));
        } catch (Exception ex) {
        }
        return rtn;
    }

    protected static HashMap<String,Object> adderData(List<Annotmatch> list) {
        if ((list == null) || (list.size() < 1)) return null;
        boolean isStaging = (list.get(0) instanceof Staging);
        String prefix = isStaging ? "review" : "match";
        HashMap<String,Object> map = new HashMap<String,Object>();
        List<JSONObject> a1 = new ArrayList<JSONObject>();
        List<JSONObject> a2 = new ArrayList<JSONObject>();
        List<String> ed = new ArrayList<String>();

        for (Annotmatch am : list) {
            a1.add(Comm.toFancyUUID(am.getAnnot1().getId()));
            a2.add(Comm.toFancyUUID(am.getAnnot2().getId()));
            ed.add(am.getEvidenceDecision());
        }

        map.put(prefix + "_annot_uuid1_list", a1);
        map.put(prefix + "_annot_uuid2_list", a2);
        map.put(prefix + "_evidence_decision_list", ed);
        return map;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("annots", new String[]{annot1.getId(), annot2.getId()})
                .append("count", getCount())
                .append("modified", new org.joda.time.DateTime(getTimestampModified()))
                .toString();
    }


}
