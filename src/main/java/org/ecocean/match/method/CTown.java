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

package org.ecocean.match.method;
import java.util.*;
import org.ecocean.match.*;
import org.json.*;

public class CTown implements Method {

	private Vector testData;
	private Vector targetData;
	private JSONObject options;

	public CTown() {
	}

	public void setTestData(Vector d) {
		testData = d;
	}
	public void setTargetData(Vector d) {
		targetData = d;
	}
	public void setOptions(JSONObject opts) {
		options = opts;
	}
  public boolean isComplete() {
		return false;
	}
	public JSONObject getStatus() {
		return null;
	}
	public void start() {
	}
	public void stop() {
	}

}
