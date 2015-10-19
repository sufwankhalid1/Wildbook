package org.ecocean;

import org.junit.Before;

public class TestBase {
    @Before
    public void bootstrap() {
        //
        // TODO: Here is where you can pass in overriding properties
        // so that we can have a Test database as some of the classes are
        // looking for.
        //
        Global.INST.init(null, null);
    }
}
