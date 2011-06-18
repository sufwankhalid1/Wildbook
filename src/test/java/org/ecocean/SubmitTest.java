package org.ecocean;

import junit.framework.TestCase;

import org.apache.wicket.util.tester.WicketTester;

public class SubmitTest extends TestCase {
  public void testRender() {
    WicketTester tester = new WicketTester(new ShepherdApplication());
    tester.startPage(Submit.class);
    tester.assertRenderedPage(Submit.class);
  }
}
