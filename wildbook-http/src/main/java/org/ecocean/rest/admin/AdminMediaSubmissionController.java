package org.ecocean.rest.admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.media.MediaSubmissionFactory;
import org.ecocean.rest.MediaSubmissionController.MSMEntry;
import org.ecocean.servlet.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.Table;

@RestController
@RequestMapping(value = "/admin/api/mediasubmission")
public class AdminMediaSubmissionController {

    public static Logger logger = LoggerFactory.getLogger(AdminMediaSubmissionController.class);

    @RequestMapping(value = "/deletemedia", method = RequestMethod.POST)
    public void deleteMedia(final HttpServletRequest request,
                            @RequestBody final MSMEntry msm)
        throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                for (int mediaid : msm.mediaids) {
                    try {
                        MediaAssetFactory.deleteMedia(db, msm.submissionid, mediaid);
                    } catch (Exception ex) {
                        throw new DatabaseException("Can't delete media.", ex);
                    }
                }
            });
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public void deleteSubmission(final HttpServletRequest request,
                                 @RequestBody final MSMEntry msm)
        throws DatabaseException, IOException
    {
        try (Database db = ServletUtils.getDb(request)) {
            String mWhere = "mediasubmissionid = " + msm.submissionid;

            String sql = "SELECT ma.* FROM mediaasset ma"
                    + " INNER JOIN mediasubmission_media msm ON msm.mediaid = ma.id"
                    + " WHERE " + mWhere;
            List<MediaAsset> media;
            media = db.selectList(sql, (rs) -> {
                return MediaAssetFactory.valueOf(rs);
            });

            db.performTransaction(() -> {
                //
                // Delete all the joins
                //
                Table table = db.getTable("mediasubmission_media");
                table.deleteRows(mWhere);

                //
                // Delete the media submission itself.
                //
                table = db.getTable("mediasubmission");
                table.deleteRows("id = " + msm.submissionid);

                //
                // Delete all the files.
                //
                for (MediaAsset aMedia : media) {
                    MediaAssetFactory.delete(db, aMedia.getID());
                }
            });

            //
            // Now delete the physical files.
            //
            for (MediaAsset aMedia : media) {
                MediaAssetFactory.deleteFromStore(aMedia);
            }
        }
    }

    @RequestMapping(value = "reassign", method = RequestMethod.POST)
    public void reassign(final HttpServletRequest request,
                         @RequestBody @Valid final MSReassign reassign) throws DatabaseException {
        //
        // update all of the photos to point to this new user and then update the mediasubmission itself.
        //
        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                SqlStatement subquery = new SqlStatement(MediaAssetFactory.TABLENAME_MEDIAASSET,
                                                         MediaAssetFactory.ALIAS_MEDIAASSET);
                subquery.setSelectString(MediaAssetFactory.PK_MEDIAASSET);
                subquery.addInnerJoin(MediaAssetFactory.ALIAS_MEDIAASSET,
                                      MediaAssetFactory.PK_MEDIAASSET,
                                      "mediasubmission_media",
                                      "msm",
                                      "mediaid");
                subquery.addCondition("msm", "mediasubmissionid", SqlRelationType.EQUAL, reassign.msid);

                String sql = "update " + MediaAssetFactory.TABLENAME_MEDIAASSET
                        + " set submitterid = " + reassign.userid
                        + " where id in (" + subquery.toString() + ")";
                db.executeUpdate(sql);

                sql = "update " + MediaSubmissionFactory.TABLENAME_MEDIASUBMISSION
                        + " set userid = " + reassign.userid
                        + " where id = " + reassign.msid;
                db.executeUpdate(sql);
            });
        }
    }

    @RequestMapping(value = "/setstatus/{msid}", method = RequestMethod.POST)
    public void setStatus(final HttpServletRequest request,
                        @PathVariable("msid")
                        final int msid,
                        @RequestBody final String status) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            String saveStatus;

            if ("null".equals(status)) {
                saveStatus = null;
            } else {
                saveStatus = status;
            }

            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            formatter.append("status", saveStatus);
            Table table = db.getTable(MediaSubmissionFactory.TABLENAME_MEDIASUBMISSION);
            table.updateRow(formatter.getUpdateClause(), "id = " + msid);
        };
    }

    static class MSReassign {
        public int msid;
        public int userid;
    }

}
