
package org.ecocean;

import org.ecocean.Shepherd;
import org.ecocean.Annotation;
import org.ecocean.Util;
import org.ecocean.User;
import org.ecocean.ia.Task;
import org.ecocean.media.MediaAsset;
import org.ecocean.identity.IBEISIA;
//import java.util.UUID;   :(
import java.util.List;
import java.util.ArrayList;
import org.joda.time.DateTime;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.JSONObject;
import org.json.JSONArray;
import javax.jdo.Query;

public class Objective implements java.io.Serializable {

    private String id = null;
    private long created = -1;
    private long modified = -1;
    private User owner = null;
    private boolean complete = false;
    private Task task = null;  //TODO someday probably should expand this to allow ImportTask and other things?
    private String status = null;

    public Objective() {
        this(Util.generateUUID());
    }
    public Objective(String id) {
        this.id = id;
        created = System.currentTimeMillis();
        modified = System.currentTimeMillis();
    }
    public Objective(User user) {
        this();
        this.owner = user;
    }
    public Objective(String id, User user) {
        this(id);
        this.owner = user;
    }

    public String getId() {
        return id;
    }

    public long getCreatedLong() {
        return created;
    }

    public long getModifiedLong() {
        return modified;
    }

    public void updateModified() {
        modified = System.currentTimeMillis();
    }

    public JSONObject getStatus() {
        if (status == null) return null;
        return Util.stringToJSONObject(status);
    }
    // see comment above: should this even be public?  (or exist)
    public void setStatus(String s) {  //best be json, yo
        status = s;
        this.updateModified();
    }
    public void setStatus(JSONObject j) {
        if (j == null) {
            status = null;
        } else {
            status = j.toString();
        }
        this.updateModified();
    }

    //this does the magic of populating the status
    public JSONObject updateStatus() {
        JSONObject stat = this.getStatus();
        if (stat == null) stat = new JSONObject();
        JSONObject taskStat = new JSONObject();
        //this assumes we only have one root task
        if ((stat.optJSONArray("tasks") != null) && (stat.getJSONArray("tasks").length() > 0) && (stat.getJSONArray("tasks").optJSONObject(0) != null)) {
            taskStat = stat.getJSONArray("tasks").getJSONObject(0);
        }
        traverseTask(this.task, taskStat);  //this uses and modifies taskStat
        JSONArray t = new JSONArray();
        t.put(taskStat);
        stat.put("tasks", t);
        this.setStatus(stat);
        this.updateModified();
        if (taskStat.optBoolean("complete", false)) this.complete = true;
        return stat;
    }

    //here we assume that stat is relative to task
    public static void traverseTask(Task task, JSONObject stat) {
        if (task == null) return;
        if (stat == null) stat = new JSONObject();
        stat.put("_traverseTask_touch", System.currentTimeMillis());
        if (stat.optBoolean("complete", false)) return;  //if a high up task is marked complete, we dont traverse (and are done)
System.out.println(((task.getParent() == null) ? "ROOT=" : task.getParent().getId() + " > ") + task.getId() + " NOT complete in Objective.traverseTask(); digging deeper.....");

        String sid = stat.optString("id", null);
        if (sid == null) {
            stat.put("id", task.getId());
        } else if (!sid.equals(task.getId())) {
            System.out.println("ERROR: traverseTask() passed task and status have different IDs (" + task.getId() + " vs " + sid + "); failing");
            return;
        }

        if (stat.optJSONArray("mediaAssets") == null) {
            JSONArray arr = new JSONArray();
            if (!Util.collectionIsEmptyOrNull(task.getObjectMediaAssets())) for (MediaAsset ma : task.getObjectMediaAssets()) {
                JSONObject j = new JSONObject();
                j.put("id", ma.getId());
                j.put("acmId", ma.getAcmId());
                j.put("url", ma.safeURL());
                arr.put(j);
            }
            stat.put("mediaAssets", arr);
        }
        if (stat.optJSONArray("annotations") == null) {
            JSONArray arr = new JSONArray();
            if (!Util.collectionIsEmptyOrNull(task.getObjectAnnotations())) for (Annotation ann : task.getObjectAnnotations()) {
                JSONObject j = new JSONObject();
                j.put("id", ann.getId());
                j.put("acmId", ann.getAcmId());
                arr.put(j);
            }
            stat.put("annotations", arr);
        }

        JSONArray log = stat.optJSONArray("log");
        if (log == null) log = new JSONArray();
        /// DO SOMETHING HERE !!!  FIXME

        if (!task.hasChildren()) return;

        JSONArray tasksStat = stat.optJSONArray("tasks");
System.out.println("******* tasksStat[" + task.getId() + "] => " + tasksStat);
        if (tasksStat == null) tasksStat = new JSONArray();
        for (Task childTask : task.getChildren()) {
            JSONObject childStat = new JSONObject();
            int statOffset = -1;
            for (int i = 0 ; i < tasksStat.length() ; i++) {
System.out.println(i + " ==> " + tasksStat.optJSONObject(i));
                if ((tasksStat.optJSONObject(i) == null) ||
                    (tasksStat.getJSONObject(i).optString("id", null) == null) ||
                    !tasksStat.getJSONObject(i).getString("id").equals(childTask.getId()) ) continue;
                //if we get here, we found the status corresponding to our task
                statOffset = i;
                childStat = tasksStat.getJSONObject(i);
System.out.println("got statOffset=" + i + " for childTask=" + childTask.getId());
                break;
            }
            traverseTask(childTask, childStat);
            if (statOffset < 0) {
                tasksStat.put(childStat);
            } else {
                tasksStat.put(statOffset, childStat);
            }
        }
        stat.put("tasks", tasksStat);
    }

/*
    //convenience method to construct the JSONObject from key/value
    public void setParameters(String key, Object value) {
        if (key == null) return;  //nope
        JSONObject j = new JSONObject();
        j.put(key, value);  //value object type better be kosher for JSONObject.  :/
        parameters = j.toString();
    }
    //like above, but doesnt (re)set .parameters, will only append/alter the key'ed one
    public void addParameter(String key, Object value) {
        if (key == null) return;
        JSONObject j = this.getParameters();
        if (j == null) j = new JSONObject();
        j.put(key, value);
        parameters = j.toString();
    }
*/

    public JSONObject toJSONObject() {
        JSONObject j = new JSONObject();
        j.put("id", id);
        j.put("created", created);
        j.put("modified", modified);
        j.put("createdDate", new DateTime(created));
        j.put("modifiedDate", new DateTime(modified));
        j.put("complete", complete);
        j.put("status", this.getStatus());
        return j;
    }


    public String toString() {
        return new ToStringBuilder(this)
                .append(id)
                .append("complete=" + complete)
                .append("(" + new DateTime(created) + "|" + new DateTime(modified) + ")")
                .toString();
    }

    public static Objective load(String objId, Shepherd myShepherd) {
        Objective o = null;
        try {
            o = ((Objective) (myShepherd.getPM().getObjectById(myShepherd.getPM().newObjectIdInstance(Objective.class, objId), true)));
        } catch (Exception ex) {};  //swallow jdo not found noise
        return o;
    }

}

