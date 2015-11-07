package org.ecocean.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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

@RestController
@RequestMapping(value = "/obj/encounter")
public class EncounterController
{
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
                         @RequestBody @Valid final List<Integer> mediaids) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                for (Integer mediaid : mediaids) {
                    EncounterFactory.addMedia(db, encounterid, mediaid);
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
}
