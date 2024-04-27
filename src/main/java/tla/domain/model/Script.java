package tla.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
* Enum specifying different egyptian language stages.
*/
public enum Script {

    HIERATIC("hieratic"),
    DEMOTIC("demotic"),
    COPTIC("coptic");

    private String id;

    Script(String id) {
        this.id = id;
    }

    @JsonCreator
    public static Script fromString(String src) {
        return Script.valueOf(src.toUpperCase());
    }

    @Override
    @JsonValue
    public String toString() {
        return this.id;
    }

    public static Script ofLemmaId(String id) {
        char c = id.toLowerCase().charAt(0);
        if (c == 'c') {
            return COPTIC;
        } else if (c == 'd') {
            return DEMOTIC;
        } else {
            return HIERATIC;
        }
    }

}
