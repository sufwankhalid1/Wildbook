package org.ecocean;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import org.ecocean.identity.IBEISIA;
import org.ecocean.media.MediaAsset;

public class IAJsonProperties extends JsonProperties {

	// maps IA.properties keys to IA.json keys that should return the same value
	private static final Map<String, String> globalBackCompatibleKeyMap;
	static {
		Map<String,String> m = new HashMap<String,String>();
		m.put("IBEISIARestUrlAddImages", "_global.add_images");
		m.put("IBEISIARestUrlAddAnnotations", "_global.add_annotations");
		m.put("IBEISIARestUrlStartIdentifyAnnotations", "_global.start_identify");
		m.put("IBEISIARestUrlStartDetectImages", "_global.start_detect");
		m.put("IBEISIARestUrlDetectReview", "_global.detect_review");
		m.put("IBEISIARestUrlGetJobStatus", "_global.get_job_status");
		m.put("IBEISIARestUrlGetJobResult", "_global.get_job_result");
		globalBackCompatibleKeyMap = Collections.unmodifiableMap(m);
	}

	public static final String DETECT_URL_KEY = "start_detect";

	public IAJsonProperties() throws FileNotFoundException {
		super("IA.json");
	}

	// hackey constructor so we don't need to catch an exception every time we use these. Is there a better way?
	public static IAJsonProperties iaConfig() {
		try {
			return new IAJsonProperties();
		} catch (Exception e) {
			System.out.println("IAJsonProperties ERROR: Could not find IA.json file! returning null and therefore about to hit an NPE elsewhere");
			return null;
		}
	}

	// naming convention: not using 'get' on static methods
	public static String taxonomyKey(Taxonomy taxy) {
		return taxy.getScientificName().replace(' ', '.');
	}

	public boolean hasIA(Taxonomy taxy) {
		Object conf = this.get(taxonomyKey(taxy));
		return (conf!=null);
	}

	public boolean hasIA(Encounter enc, Shepherd myShepherd) {
		Taxonomy taxy = enc.getTaxonomy(myShepherd);
		return hasIA(taxy);
	}


	// Detection methods
	public static String detectionKey(Taxonomy taxy) {
		return taxonomyKey(taxy) + "._detect_conf";
	}

	public JSONArray getDetectionConfigs(Taxonomy taxy) {
		return (JSONArray) this.get(detectionKey(taxy));
	}

	public JSONArray getDetectionConfigs(MediaAsset ma, Shepherd myShepherd) {
		JSONArray allConfs = new JSONArray();
		for (Taxonomy taxy: ma.getTaxonomies(myShepherd)) {
			JSONArray theseConfs = getDetectionConfigs(taxy);
			for (int i=0; i<theseConfs.length(); i++) {
				allConfs.put(theseConfs.get(i));
			}
		}
		return allConfs;
	}

	public int numDetectionAlgos(Taxonomy taxy) {
		JSONArray detectConfs = getDetectionConfigs(taxy);
		return detectConfs.length();
	}

	public JSONObject getDetectionConfig(Taxonomy taxy) {
		return getDetectionConfig(taxy, 0);
	}
	// all detection methods take an int i, bc there may be multiple detection algos for a taxonomy
	// DetectionArgs are what we pass to IA. DetectionConfig is all of those, plus the target url, minus the callback (which is generated by each wildbook vis a vis its baseUrl, and not a config value)
	public JSONObject getDetectionConfig(Taxonomy taxy, int i) {
		JSONArray confs = getDetectionConfigs(taxy);
		System.out.println("IAJsonProperties.getDetectionConfigs returned "+confs.toString());
		return confs.getJSONObject(i);
	}

	// DetectionArgs are what we pass to IA.
	public JSONObject getDetectionArgs(Taxonomy taxy, String baseUrl) {
		return getDetectionArgs(taxy, baseUrl, 0);
	}
	public JSONObject getDetectionArgs(Taxonomy taxy, String baseUrl, int i) {
		JSONObject config = getDetectionConfig(taxy, i);
		// we can't just mess with config bc changes aren't scoped to this method
		// and this is the stupid-ass way to clone JSONObjects
		JSONObject args = new JSONObject(config, JSONObject.getNames(config));
		args.remove(DETECT_URL_KEY);
		String callbackUrl = IBEISIA.callbackUrl(baseUrl);
		args.put("callback_url", callbackUrl);
		return args;
	}

	public String getDetectionUrl(Taxonomy taxy) {
		return getDetectionUrl(taxy, 0);
	}
	public String getDetectionUrl(Taxonomy taxy, int i) {
		JSONObject conf = getDetectionConfig(taxy, i);
		System.out.println("getDetectionUrl looking for "+DETECT_URL_KEY+" in conf "+conf.toString());
		return conf.getString(DETECT_URL_KEY);
	}

	// e.g. if a humpback whale detection returns ia class sperm_whale_fluke this will return humpback_fluke or whatever
	public String convertIAClassForTaxonomy(String returnedIAClass, Taxonomy taxy) {
		String taxKey = taxonomyKey(taxy);
		String lookupKey = taxKey + "." + returnedIAClass + "._save_as";
		String ans = (String) get(lookupKey);
		System.out.println("IAJsonProperties.convertIAClassForTaxonomy called on "+returnedIAClass+" for taxonomy "+taxy.toString());
		System.out.println(".................convertIAClassForTaxonomy made lookupKey "+lookupKey+" and found "+ans);
		if (!Util.stringExists(ans)) {
			String defaultLookupKey = taxKey+"._default._save_as";
			ans = (String) get(defaultLookupKey);
		  System.out.println("........fallback convertIAClassForTaxonomy made defaulLookupKey "+defaultLookupKey+" and found "+ans);
		}
		if (!Util.stringExists(ans)) ans = returnedIAClass;
		return ans;
	}

        //just the stings, not Taxonomys
        public List<String> getAllTaxonomyStrings() {
            List<String> taxs = new ArrayList<String>();
            Iterator<String> it1 = this.getJson().keys();
            while (it1.hasNext()) {
                String genus = it1.next();
                if (genus.startsWith("_")) continue;
                JSONObject second = this.getJson().optJSONObject(genus);
                if (second == null) continue;
                Iterator<String> it2 = second.keys();
                while (it2.hasNext()) {
                    String species = it2.next();
                    if (species.startsWith("_")) continue;
                    taxs.add(genus + " " + species);
                }
            }
            return taxs;
        }
        public List<Taxonomy> getAllTaxonomies(Shepherd myShepherd) {
            List<Taxonomy> taxs = new ArrayList<Taxonomy>();
            for (String taxy : getAllTaxonomyStrings()) {
                taxs.add(myShepherd.getOrCreateTaxonomy(taxy, false));
            }
            return taxs;
        }

        //this skips a non-specific taxonomy with "sp" as the second part
        public Taxonomy taxonomyFromIAClass(String iaClass, Shepherd myShepherd) {
            if (iaClass == null) return null;
            for (Taxonomy taxy : getAllTaxonomies(myShepherd)) {
                if (taxy.getScientificName().endsWith(" sp")) continue;
	        if (isValidIAClass(taxy, iaClass)) return taxy;  //first one wins!
            }
            return null;
        }

	// Identification methods
	public static String identKey(Taxonomy taxy) {
		return identKey(taxy, "_default");
	}
	public static String identKey(Taxonomy taxy, String iaClass) {
		return taxonomyKey(taxy) + "." + iaClass;
	}
	public static String identConfigKey(Taxonomy taxy) {
		return identConfigKey(taxy, "_default");
	}
	public static String identConfigKey(Taxonomy taxy, String iaClass) {
		return identKey(taxy, iaClass)+"._id_conf";
	}

	public JSONArray getIdentConfig(Taxonomy taxy) {
		return getIdentConfig(taxy, "_default");
	}

	public JSONArray getIdentConfig(Taxonomy taxy, String iaClass) {
		String configKey = identConfigKey(taxy, iaClass);
		JSONArray config = (JSONArray) this.get(configKey);
		if (config==null) {
			System.out.println("IAJsonProperties: could not find ident config for taxonomy "
				+taxy.toString()+" and iaClass "+iaClass+". Trying _default iaClass instead.");
			config = (JSONArray) this.get(identConfigKey(taxy, "_default"));
		}
		if (config==null) System.out.println("IAJsonProperties WARNING: could not find any identConfig for taxonomy "+taxy.getScientificName()+". Tried configKey="+configKey+" Returning null.");
		return config;
	}

	public JSONArray getAllIdentConfigs(Taxonomy taxy) {
		JSONArray result = new JSONArray();
		// to make sure we don't double-enter algo configs, which might live under multiple iaClasses
		Set<String> alreadyAdded = new HashSet<String>();
		for (String iaClass: getValidIAClasses(taxy)) {
			JSONArray idConfigs = getIdentConfig(taxy, iaClass);
			for (int i=0; i<idConfigs.length(); i++) {
				Object conf = idConfigs.get(i);
				if (!alreadyAdded.contains(conf.toString())) {
					result.put(conf);
					alreadyAdded.add(conf.toString());
				}
			}
		}
		return result;
	}

	public List<String> getValidIAClasses(Taxonomy taxy) {
		List<String> result = new ArrayList<String>();
		if (!hasIA(taxy)) return result;
		JSONObject underTaxy = (JSONObject) this.get(taxonomyKey(taxy));
		Iterator<String> keys = underTaxy.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (!key.startsWith("_")) {
				result.add(key); //iaClasses are keys, not values, in this format
			}
		}
		return result;
	}


	// for a given taxonomy (input during submission) and an iaClass (returned from IA), we'll save the annotation only if this check is passed.
	// e.g., if we get a whale_shark detection on a humpback whale: false. If we get an orca_dorsal detection on a bottlenose dolphin: true.
	// this just checks if the iaClass has a defined behavior in the IA.json file.
	public boolean isValidIAClass(Taxonomy taxyBeforeDetection, String iaClass) {
		if (taxyBeforeDetection == null) return false;
		String taxyKey = taxonomyKey(taxyBeforeDetection);
		String fullKey = taxyKey+"."+iaClass;
		JSONObject conf = (JSONObject) this.get(fullKey);
		return (conf != null);
	}

	// mimics an old IBEISIA method for easy migration
	// note the key discrepancy between queryConfigDict and query_config_dict in old world vs new
  public List<JSONObject> identOpts(Shepherd myShepherd, Annotation ann) {
  	Taxonomy taxy = ann.getTaxonomy(myShepherd);
  	String iaClass = ann.getIAClass();

  	return identOpts(taxy, iaClass);

  }

  public List<JSONObject> identOpts(Taxonomy taxy, String iaClass) {
  	JSONArray identConfig = getIdentConfig(taxy, iaClass);
  	if (identConfig==null) return null;

  	List<JSONObject> identOpts = new ArrayList<JSONObject>();
  	for (int i=0; i<identConfig.length(); i++) {
  		JSONObject thisIdentOpt = copyJobj(identConfig.getJSONObject(i));
  		// so we don't break lookups for queryConfigDict downstream (old world)
  		thisIdentOpt.put("queryConfigDict", thisIdentOpt.get("query_config_dict"));
  		identOpts.add(thisIdentOpt);
  	}
  	return identOpts;
  }

  public JSONArray getAllIdentOpts(Taxonomy taxy) {
		JSONArray result = new JSONArray();
		// to make sure we don't double-enter algo configs, which might live under multiple iaClasses
		Set<String> alreadyAdded = new HashSet<String>();
		for (String iaClass: getValidIAClasses(taxy)) {
			for (JSONObject idOpt: identOpts(taxy, iaClass)) {
				if (!alreadyAdded.contains(idOpt.toString())) {
					result.put(idOpt);
					alreadyAdded.add(idOpt.toString());
				}
			}
		}
		return result;
	}

	// in some cases we want to save a keyword whenever detection returns a certain IA class.
	// if a taxonomy+iaClass has a defined _save_keyword field in the config, this returns the value.
	public String getKeywordString(Taxonomy taxy, String iaClass) {
		String viewpointKeywordMapKey = taxonomyKey(taxy) + "." + iaClass + "._save_keyword";
		String keywordString = (String) this.get(viewpointKeywordMapKey);
		System.out.println("iaConf.getKeywordString found string "+keywordString+" for taxonomy "+taxy.getScientificName()+" and iaClass "+iaClass);
		return keywordString;
	}


	public static final Map<String, String> getGlobalBackCompatibleKeyMap() {
	    return globalBackCompatibleKeyMap;
	}

	public static JSONObject copyJobj(JSONObject jobj) {
		return new JSONObject(jobj, JSONObject.getNames(jobj));
	}
	
	private boolean isUserSelectableIAClass(Taxonomy taxy, String key, JSONObject underTaxy){
	  if(key.startsWith("_")){return false;}
	  else if(key.equals("common_name")){return false;}
	  else if(key.equals("match_trivial")){return false;}
	  else if( underTaxy.get(key) instanceof String &&  ((String)underTaxy.get(key)).startsWith("@")){return false;}
	  else if(underTaxy.get(key) instanceof JSONObject){
	    JSONObject obj=(JSONObject)underTaxy.get(key);
	    if(!obj.isNull("_save_as")){
	      String saveAs=(String)obj.get("_save_as");
	      if(saveAs!=null && !key.equals(saveAs))return false;
	    }
	  }
	  return true;
	}
	
	public List<String> getValidIAClassesIgnoreRedirects(Taxonomy taxy) {
    List<String> result = new ArrayList<String>();
    if (!hasIA(taxy)) return result;
    JSONObject underTaxy = (JSONObject) get(taxonomyKey(taxy));
    Iterator<String> keys = underTaxy.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      if(isUserSelectableIAClass(taxy,key,underTaxy)) {
        result.add(key); //iaClasses are keys, not values, in this format
      }
    }
    return result;
  }

}

