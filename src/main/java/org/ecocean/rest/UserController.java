package org.ecocean.rest;

import java.util.HashMap;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.Shepherd;
import org.ecocean.User;
import org.ecocean.Util;
import org.ecocean.security.Stormpath;
import org.ecocean.servlet.ServletUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.DatabaseException;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.client.Client;
//import org.ecocean.rest.SimpleUser;


///// TODO should this be returning a SimpleUser now instead?
@RestController
@RequestMapping(value = "/obj/user")
public class UserController {

    private static Logger log = LoggerFactory.getLogger(MediaSubmissionController.class);

    public PersistenceManager getPM(final HttpServletRequest request) {
        String context = "context0";
        context = ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
        return myShepherd.getPM();
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<User> save(final HttpServletRequest request) {
        String context = "context0";
        context = ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
        User user = null;
        String username = null;
        if (request.getUserPrincipal() != null) username = request.getUserPrincipal().getName();
        if ((username != null) && !username.equals("")) user = myShepherd.getUser(username);
        if (user == null) {
            user = new User();
        }
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    @RequestMapping(value = "simple", method = RequestMethod.GET)
    public SimpleUser getSimpleUser(final HttpServletRequest request) throws DatabaseException {
        String username = null;
        if (request.getUserPrincipal() != null) {
            username = request.getUserPrincipal().getName();
        }

        if (StringUtils.isBlank(username)) {
            return null;
        }
        return SimpleFactory.getUser(username);
    }

    @RequestMapping(value = "verify", method = RequestMethod.POST)
    public HashMap<String,Object> verifyEmail(final HttpServletRequest request,
                                              @RequestBody @Valid final String email) {
        Client client = Stormpath.getClient(ServletUtilities.getConfigDir(request));
        HashMap<String,Object> rtn = new HashMap<String,Object>();
        rtn.put("success", false);  //assuming rtn will only be used on errors -- user is returned upon success
        if (client == null) {
            rtn.put("message", "Could not initiate Stormpath client");
            //throw new Exception();
            return rtn;
        }
        HashMap<String,Object> q = new HashMap<String,Object>();
        q.put("email", email);
        AccountList accs = Stormpath.getAccounts(client, q);
        if ((accs == null) || (accs.getSize() < 1)) {
            rtn.put("message", "Unknown user");
            return rtn;
        }
        rtn.put("success", true);
        Account acc = accs.iterator().next();
        rtn.put("user", SimpleFactory.getStormpathUser(acc));
        rtn.put("userInfo", acc.getCustomData());
        return rtn;
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public ResponseEntity<Object> createUser(final HttpServletRequest request,
                                             @RequestBody @Valid final UserInfo user) {
        Client client = Stormpath.getClient(ServletUtilities.getConfigDir(request));
        HashMap<String,Object> rtn = new HashMap<String,Object>();
        rtn.put("success", false);  //assuming rtn will only be used on errors -- user is returned upon success
        if (client == null) {
            rtn.put("message", "Could not initiate Stormpath client");
            return new ResponseEntity<Object>(rtn, HttpStatus.BAD_REQUEST);
        }
        if ((user == null) || Util.isEmpty(user.email)) {
            rtn.put("message", "Bad/invalid user or email passed");
            return new ResponseEntity<Object>(rtn, HttpStatus.BAD_REQUEST);
        }
        if (log.isDebugEnabled()) log.debug("checking on stormpath for username=" + user.email);
        HashMap<String,Object> q = new HashMap<String,Object>();
        q.put("email", user.email);
        AccountList accs = Stormpath.getAccounts(client, q);
        if (accs.getSize() > 0) {
            rtn.put("message", "A user with this email already exists.");
            return new ResponseEntity<Object>(rtn, HttpStatus.BAD_REQUEST);
        }

        String givenName = "Unknown";
        if (!Util.isEmpty(user.fullName)) givenName = user.fullName;
        String surname = "-";
        int si = givenName.indexOf(" ");
        if (si > -1) {
            surname = givenName.substring(si+1);
            givenName = givenName.substring(0,si);
        }
        HashMap<String,Object> custom = new HashMap<String,Object>();
        custom.put("unverified", true);
        String errorMsg = null;
        Account acc = null;
        try {
            acc = Stormpath.createAccount(client, givenName, surname, user.email, Stormpath.randomInitialPassword(), null, custom);
            if (log.isDebugEnabled()) log.debug("successfully created Stormpath user for " + user.email);
        } catch (Exception ex) {
            if (log.isDebugEnabled()) log.debug("could not create Stormpath user for email=" + user.email + ": " + ex.toString());
            errorMsg = ex.toString();
        }
        if (errorMsg == null) {
            //acc.setStatus(AccountStatus.UNVERIFIED);  //seems to have no effect, but also not sure if this is cool by Stormpath
            User wbuser = new User(acc);
            PersistenceManager pm = getPM(request);
            try {
                pm.makePersistent(wbuser);
            } catch (Exception ex) {
                //not sure if this is actually a big deal, as i *think* the only way it could happen is if user already exists in wb???
                log.error("could not create Wildbook User for email=" + user.email + ": " + ex.toString());
            }
            return new ResponseEntity<Object>(SimpleFactory.getStormpathUser(acc), HttpStatus.OK);
        } else {
            rtn.put("message", "There was an error creating the new user: " + errorMsg);
            return new ResponseEntity<Object>(rtn, HttpStatus.BAD_REQUEST);
        }
        //return new ResponseEntity<Object>(user, HttpStatus.OK);
    }

    //
    // LEAVE: This is just a test url that allows us to see if we have the correct
    // setting in our dispatcher-servlet.xml that forces Spring to not make assumptions
    // about a file type of the return value if there is a dot in the path param.
    //
    @RequestMapping(value = "test/{email:.+}", method = RequestMethod.GET)
    public UserVerifyInfo test(final HttpServletRequest request,
                               @PathVariable("email") final String email) {
        UserVerifyInfo info = new UserVerifyInfo();
        info.email = email + " - test";
        return info;
    }

/*
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ResponseEntity<List<Survey>> save(final HttpServletRequest request) {
				PersistenceManager pm = getPM(request);
				Extent ext = pm.getExtent(Survey.class);
				Query q = pm.newQuery(ext);
        ArrayList all = new ArrayList((Collection) q.execute());
        return new ResponseEntity<List<Survey>>(all, HttpStatus.OK);
    }
*/


/*
    @RequestMapping(value = "/login/{username}/{password}", method = RequestMethod.POST)
    public ResponseEntity<MediaTag> appendMedia(final HttpServletRequest request,
                                              @RequestBody @Valid List<SinglePhotoVideo> media,
                                              @PathVariable("tagName") final String tagName) {

				PersistenceManager pm = getPM(request);
        MediaTag tag = null;
        try {
          tag = (MediaTag) pm.getObjectById(MediaTag.class, tagName);
        } catch (Exception ex) {
				}

				if (tag == null) {
        	tag = new MediaTag();
					tag.setName(tagName);
				}

				//for some reason, media does not get populated such that when tag is persisted, it creates all new SPVs.. grrr wtf.. explicitely loading them fixes this
				List<SinglePhotoVideo> med = new ArrayList<SinglePhotoVideo>();
				for (SinglePhotoVideo s : media) {
      		SinglePhotoVideo obj = ((SinglePhotoVideo) (pm.getObjectById(pm.newObjectIdInstance(SinglePhotoVideo.class, s.getDataCollectionEventID()), true)));
					if (obj != null) med.add(obj);
				}

				tag.addMedia(med);
				pm.makePersistent(tag);
        return new ResponseEntity<MediaTag>(tag, HttpStatus.OK);
    }
*/

    public static class UserInfo {
        public String email;
        public String fullName;
    }

    public static class UserVerifyInfo {
        public String email;
        public UserVerifyInfo() {
        }
        public String getEmail() {
            return email;
        }
    }

}

