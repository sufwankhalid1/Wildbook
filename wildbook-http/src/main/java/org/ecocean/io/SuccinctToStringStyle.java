package org.ecocean.io;

import org.apache.commons.lang3.builder.ToStringStyle;

public class SuccinctToStringStyle extends ToStringStyle {
    private static final long serialVersionUID = 1L;

    public SuccinctToStringStyle() {
        super();
        this.setUseClassName(false);
        this.setUseIdentityHashCode(false);
//        this.setUseFieldNames(true);
    }
}
