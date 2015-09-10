package org.ecocean.security;



import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.util.WebUtils;
import org.ecocean.ShepherdPMF;
import org.ecocean.servlet.ServletUtilities;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

public class ShepherdRealm extends AuthorizingRealm {

    public ShepherdRealm() {
        super();
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
            throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String userid = upToken.getUsername();

        // Null username is invalid
        if (userid == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }

        User user = null;
        try (Database db = ShepherdPMF.getDb()) {
            user = UserFactory.getUserById(db, Integer.parseInt(userid));
        } catch (DatabaseException ex) {
            throw new AuthenticationException("Trouble authenticating user [" + userid + "]", ex);
        }

        if (user == null) {
            throw new UnknownAccountException("No account found for user [" + userid + "]");
        }

        //
        // TODO: Shouldn't this just pass in the entire User object as the principal? And fullname
        // as realmName? I don't get it. --ken
        //
        return new SimpleAuthenticationInfo(userid, user.getHashedPass().toCharArray(), user.getFullName());
    }


    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        HttpServletRequest request = WebUtils.getHttpRequest(SecurityUtils.getSubject());

        Integer userid = NumberUtils.createInteger((String) principals.getPrimaryPrincipal());

        if (userid == null) {
            return new SimpleAuthorizationInfo();
        }

        String context = ServletUtilities.getContext(request);
        //
        // WARN: always use context0 to get the user roles as all users are stored there
        // TODO: Need to make the samsix connection info be context sensitive and then
        // we can get a context0 db connection. Right now it will be getting the one and only
        // connection for the db.
        //
        try (Database db = ShepherdPMF.getDb()) {
            return new SimpleAuthorizationInfo(UserFactory.getAllRolesForUserInContext(db, userid, context));
        } catch (DatabaseException ex) {
            ex.printStackTrace();
            return new SimpleAuthorizationInfo();
        }
    }
}