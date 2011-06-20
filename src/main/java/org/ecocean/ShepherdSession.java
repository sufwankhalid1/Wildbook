package org.ecocean;

import org.apache.wicket.Request;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;

public class ShepherdSession extends AuthenticatedWebSession {

  private static final long serialVersionUID = 1643857043439000385L;

  /**
   * Construct.
   * 
   * @param request
   *          The current request object
   */
  public ShepherdSession(Request request) {
    super(request);
  }

  /**
   * @see org.apache.wicket.authentication.AuthenticatedWebSession#authenticate(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public boolean authenticate(final String username, final String password) {
    // Check username and password
    System.out.println(String.format("authenticating: username/password = %s/%s", username, password));
    boolean matched = username.equals("wicket") && password.equals("wicket");
    System.out.println("matched?" + matched);
    return matched;
  }

  /**
   * @see org.apache.wicket.authentication.AuthenticatedWebSession#getRoles()
   */
  @Override
  public Roles getRoles() {
    if (isSignedIn()) {
      // If the user is signed in, they have these roles
      return new Roles(Roles.ADMIN);
    }
    return null;
  }
}
