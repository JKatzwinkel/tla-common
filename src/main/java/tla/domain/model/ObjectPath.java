package tla.domain.model;

import java.util.ArrayList;
import java.util.Arrays;

import tools.jackson.databind.annotation.JsonDeserialize;

import tla.domain.model.meta.Resolvable;

@JsonDeserialize(contentAs = ObjectReference.class)
public class ObjectPath extends ArrayList<Resolvable> {
    private static final long serialVersionUID = -8627340081025370180L;

    public static ObjectPath of(Resolvable ...refs) {
        ObjectPath path = new ObjectPath();
        path.addAll(
            Arrays.asList(refs)
        );
        return path;
    }
}