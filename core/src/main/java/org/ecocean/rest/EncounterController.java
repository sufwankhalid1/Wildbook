package org.ecocean.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.servlet.ServletUtilities;
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
    public void saveEncounter(final HttpServletRequest request,
                              @RequestBody @Valid final Encounter encounter) throws DatabaseException {
        if (encounter == null) {
            return;
        }

        try (Database db = ServletUtilities.getDb(request)) {
            db.performTransaction(() -> {
                EncounterFactory.saveEncounter(db, encounter);
            });
        }
    }
}
