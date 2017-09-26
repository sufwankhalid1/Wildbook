/*
 * Wildbook - A Mark-Recapture Framework
 * Copyright (C) 2017 Jason Holmberg
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

package org.ecocean.plugins.WildbookIA;

import java.util.UUID;
//import org.apache.commons.lang3.builder.ToStringBuilder;

public class Staging extends Annotmatch {
    private UUID reviewUuid;

    //note that the following 3-tuple of (annot1, annot2, count) should be unique *regardless of review order*
        //TODO try to make this work with psql constraint + trigger??
    private long timestampServerStart;
    private long timestampClientStart;
    private long timestampServerEnd;
    private long timestampClientEnd;

    //TODO how to let count automagically incremented?
    public Staging(UUID a1, UUID a2, int ct) {
        super(a1, a2);
        reviewUuid = Util.generateUUID();
        count = ct;
    }

    public Staging(UUID a1, UUID a2) {
        Staging(a1, a2, 0);
    }

}
