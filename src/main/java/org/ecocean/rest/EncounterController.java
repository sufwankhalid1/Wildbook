package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.ShepherdPMF;
import org.ecocean.Shepherd;
//import org.ecocean.SinglePhotoVideo;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.Encounter;
import org.ecocean.MarkedIndividual;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import javax.jdo.*;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

@RestController
@RequestMapping(value = "/obj/encounter")
public class EncounterController
{

		public PersistenceManager getPM(final HttpServletRequest request) {
        String context = "context0";
        context = ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
				return myShepherd.getPM();
		}


    @RequestMapping(value = "/setMarkedIndividual/{encounterID}/{indivID}", method = RequestMethod.GET)
    //public ResponseEntity<MarkedIndividual> appendPoints(final HttpServletRequest request,
    public void appendPoints(final HttpServletRequest request,
                                                    @PathVariable("encounterID") final String encounterID,
                                                    @PathVariable("indivID") final String indivID) {

        String context = "context0";
        context = ServletUtilities.getContext(request);
				PersistenceManager pm = getPM(request);
        Encounter enc = null;
        MarkedIndividual indiv = null;
        try {
          enc = (Encounter) pm.getObjectById(Encounter.class, encounterID);
        } catch (Exception ex) {
				}

				if (enc == null) {
        	return;

				} else {
        	try {
          	indiv = (MarkedIndividual) pm.getObjectById(MarkedIndividual.class, indivID);
        	} catch (Exception ex) {
					}
					if (indiv == null) {
						indiv = new MarkedIndividual();
						indiv.setIndividualID(indivID);
					}
					indiv.addEncounter(enc, context);
					pm.makePersistent(indiv);
        	return;
				}
    }


}
