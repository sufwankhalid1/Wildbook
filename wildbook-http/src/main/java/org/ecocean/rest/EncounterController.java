package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
@RequestMapping(value = "/api/encounter")
public class EncounterController {
    @RequestMapping(value = "/full/{id}", method = RequestMethod.GET)
    private EncounterObj getFullEncounter(final HttpServletRequest request,
                                          @PathVariable("id")
                                          final int id) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            return EncounterFactory.getEncounterObj(db, id);
        }
    }

    @RequestMapping(value = "getmedia/{id}", method = RequestMethod.GET)
    public List<SimplePhoto> addMedia(final HttpServletRequest request,
                                      @PathVariable("id") final int encounterid) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            return EncounterFactory.getMedia(db, encounterid);
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
}
