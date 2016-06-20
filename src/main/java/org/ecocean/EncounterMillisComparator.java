/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2011 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ecocean;

import java.util.Comparator;
import java.util.GregorianCalendar;
import org.joda.time.DateTime;


public class EncounterMillisComparator implements Comparator {

  /**
   * If reverse is true, encounters will be sorted with earliest to most recent.
   * If reverse is true, encounters will be sorted most recent first and then back through time.
   */
  private boolean reverse=false;

  EncounterMillisComparator() {
  }

  EncounterMillisComparator(boolean reverse) {
    this.reverse=reverse;
  }

  public int compare(Object a, Object b) {
    Encounter a_enc = (Encounter) a;
    Encounter b_enc = (Encounter) b;
    Long a_millis = a_enc.getDateInMilliseconds();
    Long b_millis = b_enc.getDateInMilliseconds();

    if (a_millis==null || b_millis==null) return 0;

    if(!reverse){
      return (-1*a_millis.compareTo(b_millis));
    }
    else{
      return a_millis.compareTo(b_millis);
    }
  }


}
