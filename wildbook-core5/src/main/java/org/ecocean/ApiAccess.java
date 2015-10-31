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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.math.NumberUtils;
import org.datanucleus.api.rest.orgjson.JSONObject;
import org.ecocean.servlet.ServletUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.samsix.database.DatabaseException;


public class ApiAccess {
  private final int day = 0;
    private Document configDoc = null;



    public Document initConfig(final HttpServletRequest request) {
        if (this.configDoc != null) return this.configDoc;
        HttpSession session = request.getSession(true);
        String context = "context0";
        context = ServletUtilities.getContext(request);

        ServletContext sc = session.getServletContext();
        File afile = new File(sc.getRealPath("/") + "/WEB-INF/classes/apiaccess.xml");

        // h/t http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            this.configDoc = dBuilder.parse(afile);
            this.configDoc.getDocumentElement().normalize();
        } catch (Exception ex) {
            System.out.println("could not read " + afile.toString() + ": " + ex.toString());
            this.configDoc = null;
        }
        return this.configDoc;
    }


    //this does the real work.  returns null if ok, or string with error message if request is disallowed on object
    public String checkRequest(final Object obj, final HttpServletRequest request, final JSONObject jsonobj) throws DatabaseException {
        String err = null;
        HashMap<String, String> perm = permissions(obj, request);

        if ((perm.get("_objectRead") != null) && perm.get("_objectRead").equals("deny")) return "objectRead denied by permissions";

        //for now (TODO) we only care about post/put
        if (!request.getMethod().equals("POST") && !request.getMethod().equals("PUT")) return null;

        if ((perm.get("_objectUpdate") != null) && perm.get("_objectUpdate").equals("deny")) return "objectUpdate denied by permissions";

        Iterator<?> keys = jsonobj.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();

            //we dont care what the value is, just if it is being set at all and shouldnt be
            if (perm.containsKey(key) && perm.get(key).equals("deny")) {
                err = "altering value for " + key + " disallowed by permissions: " + perm.toString();
                break;
            }
        }
        return err;
    }


    //returns map of (negative) permissions for this user (based on role) for this object class
    // note: no hash key for a property means all access, therefore a null value means user CANNOT write
    // TODO this structure is subject to change for sure!
    public HashMap<String, String> permissions(final Object o, final HttpServletRequest request) throws DatabaseException {
        return permissions(o.getClass().getName(), request);
    }

    //does the real work (pass class name string)
    public HashMap<String, String> permissions(final String cname, final HttpServletRequest request) throws DatabaseException {
        this.initConfig(request);

        Set<String> roles;
        Integer userid;
        if (request.getUserPrincipal() != null) {
            userid = NumberUtils.createInteger(request.getUserPrincipal().getName());
        } else {
            userid = null;
        }

        String context = ServletUtilities.getContext(request);
        roles = Global.INST.getUserService().getAllRolesForUserInContext(userid, context);

        HashMap<String, String> perm = new HashMap<String, String>();
        NodeList nlist = this.configDoc.getDocumentElement().getElementsByTagName("class");
        if (nlist.getLength() < 1) {
            return perm;
        }

        for (int i = 0 ; i < nlist.getLength() ; i++) {
            Node n = nlist.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;

                if (el.getAttribute("name").equals(cname)) {  //we got our class
                    Node p = el.getElementsByTagName("object").item(0);
                    if (!canUser(p, "create", false, roles)) perm.put("_objectCreate", "deny");
                    if (!canUser(p, "read", true, roles)) perm.put("_objectRead", "deny");
                    if (!canUser(p, "update", false, roles)) perm.put("_objectUpdate", "deny");
                    if (!canUser(p, "delete", false, roles)) perm.put("_objectDelete", "deny");

                    ////////// now properties ///////////
                    p = el.getElementsByTagName("properties").item(0);
                    if (p == null) return perm;
                    Element propsEl = (Element) p;
                    NodeList props = propsEl.getElementsByTagName("property");
                    for (int j = 0 ; j < props.getLength() ; j++) {
                        if (props.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element pel = (Element) props.item(j);
                            String propName = pel.getAttribute("name");
                            if (propName != null) {
                                if (!canUser(props.item(j), "update", false, roles)) perm.put(propName, "deny");
                                if (!canUser(props.item(j), "read", true, roles)) perm.put(propName, "deny");
                            }
                        }
                    }
                }
            }
        }

        return perm;
    }


    public boolean canUser(final Node n, final String action, final boolean byDefault, final Set<String> roles) {
        if (n == null) return byDefault;
        Element el = (Element) n;
        NodeList o = el.getElementsByTagName(action);
        if ((o == null) || (o.getLength() < 1)) return byDefault;
        boolean allowed = false;  //if we get this far, we have to prove it cuz we are not non-existent, which is when byDefault applies
        Element oel = (Element) o.item(0);
        NodeList proles = oel.getElementsByTagName("role");
        for (int k = 0 ; k < proles.getLength() ; k++) {
            if (roles.contains(proles.item(k).getTextContent()) || proles.item(k).getTextContent().equals("*")) {
                allowed = true;
                k = proles.getLength() + 1;
            }
        }
        return allowed;
    }
}


