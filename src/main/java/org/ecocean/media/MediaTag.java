
package org.ecocean.media;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.ShepherdPMF;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
//import com.samsix.database.SqlFormatter;

public class MediaTag {
  private String name;
  private List<SinglePhotoVideo> media; 

    public String getName() {
        return name;
    }
    public void setName(String n) {
        name = n;
    }

    public List<SinglePhotoVideo> getMedia() {
        return media;
    }
    public void setMedia(List<SinglePhotoVideo> m) {
        media = m;
    }
    public void addMedia(List<SinglePhotoVideo> m) {  //TODO avoid duplicates!
        if ((m == null) || (m.size() < 1)) return;
        List<SinglePhotoVideo> all = getMedia();
        if (all == null) all = new ArrayList<SinglePhotoVideo>();
        all.addAll(m);
        setMedia(all);
    }
    public void removeMedia(List<SinglePhotoVideo> m) {
        if ((m == null) || (m.size() < 1)) return;
        List<SinglePhotoVideo> all = getMedia();
        if (all == null) return;
        all.removeAll(m);
        setMedia(all);
    }

    public static HashMap<SinglePhotoVideo,List<String>> getTags(List<SinglePhotoVideo> media) {
        if ((media == null) || (media.size() < 1)) return null;
        String sep = "";
        String sql = "";
        HashMap<String,SinglePhotoVideo> mediaMap = new HashMap<String,SinglePhotoVideo>();  //to map id-to-spv for later
        for (SinglePhotoVideo spv : media) {
            mediaMap.put(spv.getDataCollectionEventID(), spv);
            sql += sep + spv.getDataCollectionEventID();
            sep = "', '";
        }
        sql = "SELECT \"NAME_OID\" AS tag, \"DATACOLLECTIONEVENTID_EID\" AS id FROM \"MEDIATAG_MEDIA\" WHERE \"DATACOLLECTIONEVENTID_EID\" IN ('" + sql + "')";
//System.out.println("sql -> " + sql);

        HashMap<SinglePhotoVideo,List<String>> map = new HashMap<SinglePhotoVideo,List<String>>();
        ConnectionInfo ci = ShepherdPMF.getConnectionInfo();
        Database db = new Database(ci);
        try {
            RecordSet rs = db.getRecordSet(sql);
            while (rs.next()) {
//System.out.println(rs.getString("tag") + " - " + rs.getString("id"));
                if (map.get(mediaMap.get(rs.getString("id"))) == null) map.put(mediaMap.get(rs.getString("id")), new ArrayList<String>());
                map.get(mediaMap.get(rs.getString("id"))).add(rs.getString("tag"));
            }
        } catch (DatabaseException ex) {
            System.out.println("MediaTag.getTags() database exception: " + ex.toString());
        } finally {
            db.release();
        }

//System.out.println(map);
        return map;
    }

}
