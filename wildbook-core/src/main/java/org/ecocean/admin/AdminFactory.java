package org.ecocean.admin;

import org.ecocean.Global;
import org.ecocean.Species;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class AdminFactory {
    public final static String TABLENAME_SPECIES = "species";
    
    
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
            fillFormatter(db, formatter, species);
            table.insertRow(formatter);
        } else {
            SqlUpdateFormatter formatter;
            formatter = new SqlUpdateFormatter();
            fillFormatter(db, formatter, species);
            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("code", oldSpecies.getCode());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
        Global.INST.refreshSpecies();
    }
    
    private static void fillFormatter(final Database db,
            final SqlFormatter formatter,
            final Species species)
    {
        formatter.append("code", species.getCode());
        formatter.append("icon", species.getIcon());
        formatter.append("name", species.getName());
    }
}
