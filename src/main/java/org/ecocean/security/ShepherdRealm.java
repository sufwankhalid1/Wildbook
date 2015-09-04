package org.ecocean.security;



import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

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
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.ecocean.Role;
import org.ecocean.Shepherd;
import org.ecocean.User;
import org.ecocean.servlet.ServletUtilities;

public class ShepherdRealm extends AuthorizingRealm {



  public ShepherdRealm() {
    super();
  }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();

        // Null username is invalid
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }

        AuthenticationInfo info = null;
        String context="context0";

            Shepherd myShepherd=new Shepherd(context);
            myShepherd.beginDBTransaction();

            if (myShepherd.getUser(username)==null) {
                myShepherd.rollbackDBTransaction();
                myShepherd.closeDBTransaction();
                myShepherd=null;
                throw new UnknownAccountException("No account found for user [" + username + "]");
            }
            else{

              User user=myShepherd.getUser(username);
              String fullName="";
              if(user.getFullName()!=null){fullName=user.getFullName();}
              info = new SimpleAuthenticationInfo(username, user.getPassword().toCharArray(), fullName);

              myShepherd.rollbackDBTransaction();
              myShepherd.closeDBTransaction();
              myShepherd=null;

            return info;
            }
    }

    protected Set<String> getRoleNamesForUserInContext(final String username,final String context){

        Set<String> roleNames = new TreeSet<>();
        //always use context0 below as all users are stored there
        String actualContext="context0";
        if(context!=null){actualContext=context;}

        Shepherd myShepherd=new Shepherd("context0");
        myShepherd.beginDBTransaction();
        if(myShepherd.getUser(username)!=null){

            ArrayList<Role> roles=myShepherd.getAllRolesForUserInContext(username,actualContext);
            int numRoles=roles.size();
            for(int i=0;i<numRoles;i++){
              roleNames.add(roles.get(i).getRolename());
            }

        }

        myShepherd.rollbackDBTransaction();
        myShepherd.closeDBTransaction();
        myShepherd=null;

        return roleNames;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
     String username = (String) principals.getPrimaryPrincipal();
     Subject subject = SecurityUtils.getSubject();
     HttpServletRequest request = WebUtils.getHttpRequest(subject);
     String context=ServletUtilities.getContext(request);
     //System.out.println("Context in ShepherdReal is: "+context);
     //ServletContainerSessionManager.

     return new SimpleAuthorizationInfo(getRoleNamesForUserInContext(username,context));
}

}