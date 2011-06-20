package org.ecocean;

import org.apache.wicket.markup.html.border.Border;

public class Template extends Border {
  private static final long serialVersionUID = 2056641285871588650L;

  public Template(String id) {
    super(id);
    add(new Header("header"));
    add(new Footer("footer"));
  }

  @Override
  public boolean isTransparentResolver() {
    return true;
  }
}
