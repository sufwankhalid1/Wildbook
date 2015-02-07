package org.ecocean.match;

import java.util.*;
import java.io.Serializable;
import org.ecocean.*;
import org.json.*;

public class Task implements java.io.Serializable {
  
  
  //private static final long serialVersionUID = -7034712240056255450L;

	private String id;
	private long startTime = 0;
	private long endTime = 0;
	private boolean complete = false;
	private Vector<Method> methods = new Vector<Method>();

	private Vector testData = new Vector();
	private Vector targetData = new Vector(); //what to test against

	public Task() {
		this.id = Util.generateUUID();
	}


	public boolean isComplete() {
		return complete;
	}

	public void start() {
		if (startTime > 0) return;  //already started
		startTime = System.currentTimeMillis();
		for (int i = 0 ; i < methods.size() ; i++) {
			methods.get(i).start();
		}
	}


	public void setMethods(Vector<Method> m) {
		methods = m;
	}

	public JSONObject getStatus() {
//TODO based upon method???
		return null;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

}
