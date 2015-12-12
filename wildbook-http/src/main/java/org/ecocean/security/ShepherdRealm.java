package org.ecocean.security;



import javax.servlet.http.HttpServletRequest;

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
import org.ecocean.Global;
import org.ecocean.servlet.ServletUtils;
import org.ecocean.util.LogBuilder;
import org.ecocean.util.WildbookUtils;
import org.slf4j.Logger;

public class ShepherdRealm extends AuthorizingRealm {
    private static Logger logger = UserService.logger;

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

        User user = Global.INST.getUserService().getUserById(userid);

        if (user == null) {
            throw new UnknownAccountException("No account found for user [" + userid + "]");
        }

        //
        // TODO: Shouldn't this just pass in the entire User object as the principal? And fullname
        // as realmName? I don't get it. --ken
        //
        String realmName = user.getFullName();
        if (realmName == null) {
            realmName = "";
        }

        if (logger.isDebugEnabled()) {
            LogBuilder.get("Authenticating...").appendVar("userid", userid)
                .appendVar("hashedpassword", user.getHashedPass())
                .appendVar("password", upToken.getPassword())
                .appendVar("hashedAndSaltedPass", WildbookUtils.hashAndSaltPassword(upToken.getPassword().toString(), user.getSalt()))
                .appendVar("realmName",  realmName)
                .debug(logger);
        }

        SimpleAuthenticationInfo info;
        info = new SimpleAuthenticationInfo(userid, user.getHashedPass().toCharArray(), realmName);
        return info;
    }


    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        HttpServletRequest request = WebUtils.getHttpRequest(SecurityUtils.getSubject());

        String userid = (String) principals.getPrimaryPrincipal();

        if (userid == null) {
            return new SimpleAuthorizationInfo();
        }

        String context = ServletUtils.getContext(request);
        return new SimpleAuthorizationInfo(Global.INST.getUserService().getAllRolesForUserInContext(userid, context));
    }
}