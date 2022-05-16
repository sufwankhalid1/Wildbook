package org.ecocean.customfield;

public class CustomFieldValueBoolean extends CustomFieldValue {
    private Boolean value = null;
    public CustomFieldValueBoolean() {
    }
    public CustomFieldValueBoolean(CustomFieldDefinition def) {
        super(def);
    }
    public CustomFieldValueBoolean(CustomFieldDefinition def, Object val) {
        super(def);
        this.setValue(val);
    }

    public Object getValue() {
        return this.value;
    }
    public void setValue(Object obj) {
        if ((obj == null) || !(obj instanceof Boolean)) {
            this.value = null;
        } else {
            this.value = (Boolean)obj;
        }
    }
}

