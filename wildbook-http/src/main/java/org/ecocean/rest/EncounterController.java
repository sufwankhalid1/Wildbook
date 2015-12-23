package org.ecocean.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;

@RestController
@RequestMapping(value = "/obj/encounter")
public class EncounterController
{
    public static List<Encounter> searchEncounters(final HttpServletRequest request,
                                                   final EncounterSearch search) throws DatabaseException {
        SqlStatement sql = EncounterFactory.getEncounterStatement();

        if (search.getEncdate() != null) {
            sql.addCondition(EncounterFactory.ALIAS_ENCOUNTERS, "encdate", SqlRelationType.EQUAL, search.getEncdate().toString());
        }

        if (! StringUtils.isBlank(search.getLocationid())) {
            sql.addContainsCondition(EncounterFactory.ALIAS_ENCOUNTERS, "locationid", search.getLocationid());
        }

        try (Database db = ServletUtils.getDb(request)) {
            return db.selectList(sql, (rs) -> {
                return EncounterFactory.readEncounter(rs);
            });
        }
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public Integer saveEncounter(final HttpServletRequest request,
                                 @RequestBody @Valid final Encounter encounter) throws DatabaseException {
        if (encounter == null) {
            return null;
        }

        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                EncounterFactory.saveEncounter(db, encounter);
            });
            return encounter.getId();
        }
    }

    @RequestMapping(value = "addmedia/{id}", method = RequestMethod.POST)
    public void addMedia(final HttpServletRequest request,
                         @PathVariable("id") final int encounterid,
                         @RequestBody @Valid final Integer[] mediaids) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                for (int ii=0; ii < mediaids.length; ii++) {
                    EncounterFactory.addMedia(db, encounterid, mediaids[ii]);
                }
            });
        }
    }

    @RequestMapping(value = "detachmedia/{id}", method = RequestMethod.POST)
    public void detacchMedia(final HttpServletRequest request,
                             @PathVariable("id") final int encounterid,
                             @RequestBody @Valid final Integer[] mediaids) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                for (int ii=0; ii < mediaids.length; ii++) {
                    EncounterFactory.detachMedia(db, encounterid, mediaids[ii]);
                }
            });
        }
    }


    @RequestMapping(value = "getmedia/{id}", method = RequestMethod.GET)
    public List<SimplePhoto> addMedia(final HttpServletRequest request,
                                      @PathVariable("id") final int encounterid) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            return EncounterFactory.getMedia(db, encounterid);
        }
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public void deleteEncounter(final HttpServletRequest request,
                                @RequestBody final Encounter encounter) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            EncounterFactory.deleteEncounter(db, encounter.getId());
        }
    }
}
