package org.ecocean.api;

import org.ecocean.Encounter;
import org.ecocean.ShepherdPMF;
import org.ecocean.servlet.EncounterSetVerbatimEventDate;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;

@Path("/encounters.json")
public class EncounterResource {
  @GET
  @Produces("application/json")
  public Collection<Encounter> getEncounters() throws Exception {
    PersistenceManager pm = ShepherdPMF.getPMF().getPersistenceManager();
    Extent<Encounter> encClass= pm.getExtent(Encounter.class, true);
    Query acceptedEncounters = pm.newQuery(encClass);
    Object o = acceptedEncounters.execute();
    if (o instanceof Collection) {
      return (Collection<Encounter>)acceptedEncounters.execute();
    } else {
      throw new Exception("got a non-encounter collection from query layer");
    }
  }

}
