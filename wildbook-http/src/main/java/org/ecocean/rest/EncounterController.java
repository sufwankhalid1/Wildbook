package org.ecocean.rest;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.encounter.EncounterFactory;
import org.ecocean.encounter.EncounterObj;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.PathVariable;
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
}
