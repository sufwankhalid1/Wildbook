package org.ecocean.rest.admin;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.Individual;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/admin/api/individual")
public class AdminIndividualController {
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public Individual saveEncounter(final HttpServletRequest request,
                                    @RequestBody @Valid final Individual individual) throws DatabaseException {
        if (individual == null) {
            return null;
        }

        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                EncounterFactory.saveIndividual(db, individual);
            });
            return individual;
        }
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public void deleteEncounter(final HttpServletRequest request,
                                @RequestBody final Individual individual) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            EncounterFactory.deleteIndividual(db, individual.getId());
        }
    }
}
