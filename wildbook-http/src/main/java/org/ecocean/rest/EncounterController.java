package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.encounter.EncounterObj;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/obj/encounter")
public class EncounterController {
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public EncounterSaveResult saveEncounter(final HttpServletRequest request,
                                             @RequestBody @Valid final Encounter encounter) throws DatabaseException {
        if (encounter == null) {
            return null;
        }

        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                EncounterFactory.saveEncounter(db, encounter);
            });
            EncounterSaveResult results = new EncounterSaveResult();
            results.encounterid = encounter.getId();
            results.individualid = encounter.getIndividual().getId();
            return results;
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

    @RequestMapping(value = "/full/{id}", method = RequestMethod.POST)
    private EncounterObj getFullEncounter(final HttpServletRequest request,
                                          @PathVariable("id")
                                          final int id) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            return EncounterFactory.getEncounterObj(db, id);
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

    @RequestMapping(value = "checkDuplicateImage", method = RequestMethod.POST)
    public List<Encounter> checkDuplicateImage(final HttpServletRequest request,
                                @RequestBody final List<SimplePhoto> photos) throws DatabaseException {
        List<Encounter> encounters = new ArrayList<Encounter>();
        try (Database db = ServletUtils.getDb(request)) {
            for(SimplePhoto photo : photos) {
                encounters.add(EncounterFactory.getEncountersByMedia(db, photo.getId()));
            }
        }

        return encounters;
    }

    static class EncounterSaveResult {
        public int individualid;
        public int encounterid;
    }
}
