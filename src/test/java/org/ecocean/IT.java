package org.ecocean;

import junit.framework.*;
import com.meterware.httpunit.*;
import com.meterware.servletunit.*;

public class IT extends TestCase {
    public static TestSuite suite() {
        return new TestSuite(IT.class);
    }

    public IT(String s) {
        super(s);
    }

    public void testHome() throws Exception {
        HttpUnitOptions.setScriptingEnabled(false);
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        WebConversation wc = new WebConversation();
        WebRequest     req = new GetMethodWebRequest( "http://localhost:8080/shepherd/" );
        WebResponse   resp = wc.getResponse( req );
    }
}
