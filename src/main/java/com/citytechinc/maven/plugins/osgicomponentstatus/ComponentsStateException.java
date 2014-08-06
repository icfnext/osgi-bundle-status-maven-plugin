package com.citytechinc.maven.plugins.osgicomponentstatus;

import java.util.Map;

public class ComponentsStateException extends Exception {

    private final Map<String, String> errors;

    public ComponentsStateException(Map<String, String> errors) {
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(java.lang.System.lineSeparator());
        for (String key : errors.keySet()) {
            sb.append(key)
                    .append(" is : ")
                    .append(errors.get(key))
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }
}
