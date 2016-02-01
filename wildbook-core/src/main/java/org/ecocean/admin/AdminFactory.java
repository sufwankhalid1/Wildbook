package org.ecocean.admin;

import java.util.ArrayList;
import java.util.List;

import org.ecocean.Global;
import org.ecocean.Species;
import org.ecocean.survey.Vessel;
import org.ecocean.util.NotificationException;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class AdminFactory {
    public final static String TABLENAME_SPECIES = "species";
    public final static String TABLENAME_INDIVIDUALS = "individuals";
    public final static String TABLENAME_VESSEL = "vessel";

    public final static String PK_VESSELS = "vesselid";

    public static void saveSpecies(final Database db,
                            final String code,
                            final Species species)
        throws DatabaseException
    {
        Table table = db.getTable(TABLENAME_SPECIES);

        Species oldSpecies = Global.INST.getSpecies(code);

        if (oldSpecies == null){
            SqlInsertFormatter formatter;
            formatter = new SqlInsertFormatter();
            speciesFillFormatter(db, formatter, species);
            table.insertRow(formatter);
        } else {
            SqlUpdateFormatter formatter;
            formatter = new SqlUpdateFormatter();
            speciesFillFormatter(db, formatter, species);
            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("code", oldSpecies.getCode());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
        Global.INST.refreshSpecies();
    }

    public static void deleteSpecies(final Database db, final String code)
        throws DatabaseException, Throwable {

            if (code == null) {
                return;
            }

            Table species_tab = db.getTable(TABLENAME_SPECIES);
            Table indiv_tab = db.getTable(TABLENAME_INDIVIDUALS);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("species", code);
            if (indiv_tab.getCount(where.getWhereClause()) > 0) {
                throw new NotificationException("Cannot delete. This species is currently in use.");
            }

            where = new SqlWhereFormatter();
            where.append("code", code);
            species_tab.deleteRows(where.getWhereClause());
            Global.INST.refreshSpecies();
    }

    public static Vessel readVessels(final RecordSet rs) throws DatabaseException {
        if (!rs.hasColumn(PK_VESSELS)) {
            return null;
        }

        Integer id = rs.getInteger(PK_VESSELS);
        if (id == null) {
            return null;
        }

        Vessel vessel = new Vessel(id, rs.getInt("orgid"), rs.getInt("vesseltypeid"), rs.getString("vesselname"));

        return vessel;
    }


    public static List<Vessel> getVessels(final Database db) throws DatabaseException {
        List<Vessel> vessels = new ArrayList<>();
        db.getTable(TABLENAME_VESSEL).select((rs) -> {
            vessels.add(readVessels(rs));
        });
        return vessels;
    }

    public static void saveVessel(final Database db, final Vessel vessel) throws DatabaseException {
        Table table = db.getTable(TABLENAME_VESSEL);

        if (vessel.getOrgId() == null) {
            //throw new NotificationException("Please choose an organization for this vessel.");
        }

        if (vessel.getVesselId() == null) {
            SqlInsertFormatter formatter;
            formatter = new SqlInsertFormatter();
            vesselFillFormatter(db, formatter, vessel);
            table.insertRow(formatter);
        } else {
            SqlUpdateFormatter formatter;
            formatter = new SqlUpdateFormatter();
            vesselFillFormatter(db, formatter, vessel);
            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("vesselid", vessel.getVesselId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }

//    public static void deleteVessel(final Database db, final int vesselid) throws DatabaseException, Throwable {
//    }

    private static void speciesFillFormatter(final Database db,
            final SqlFormatter formatter,
            final Species species)
    {
        formatter.append("code", species.getCode());
        formatter.append("icon", species.getIcon());
        formatter.append("name", species.getName());
    }

    private static void vesselFillFormatter(final Database db,
            final SqlFormatter formatter,
            final Vessel vessel)
    {
        formatter.append("orgid", vessel.getOrgId());
        formatter.append("vesselname", vessel.getName());
        formatter.append("vesseltypeid", vessel.getTypeId());
    }
}
