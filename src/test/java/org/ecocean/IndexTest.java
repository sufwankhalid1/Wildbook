package org.ecocean;

import org.apache.wicket.util.tester.WicketTester;

import junit.framework.TestCase;

public class IndexTest extends TestCase {
  public void testRender() {
    WicketTester tester = new WicketTester(new ShepherdApplication());
    tester.startPage(Index.class);
    tester.assertRenderedPage(Index.class);
  }
}
