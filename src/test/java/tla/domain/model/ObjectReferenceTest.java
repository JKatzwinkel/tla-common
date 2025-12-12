package tla.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.InvalidDefinitionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import tla.domain.dto.meta.AbstractDto.ObjectReferences;
import tla.domain.model.meta.Resolvable;
import tla.domain.util.IO;

public class ObjectReferenceTest {

    private ObjectMapper mapper = IO.getMapper();

    @Test
    void deserialize_partial_id_eclass() throws Exception {
        assertThrows(
            InvalidDefinitionException.class,
            () -> mapper.readValue(
                "{\"id\": \"ID\", \"eclass\": \"eclass\"}",
                Resolvable.class
            ),
            "deserializing into instance should fail"
        );
    }

    @ParameterizedTest
    @ValueSource(classes = {List.class, ArrayList.class, ObjectReferences.class})
    void deserializeInArray(Class<?> model) throws Exception {
        Object refs = mapper.readValue(
            "[{\"id\":\"1\",\"eclass\":\"eclass\"},{\"id\":\"2\",\"eclass\":\"eclass\"}]",
            model
        );
        Collection<?> l = model.isArray() ? Arrays.asList(refs) : (Collection<?>) refs;
        assertAll("should deserialize multiple references",
            () -> assertNotNull(refs, "array"),
            () -> assertEquals(2, l.size(), "item count"),
            () -> assertTrue(
                l.stream().allMatch(it -> it != null),
                "no items are null"
            )
        );
    }

    @ParameterizedTest
    @SuppressWarnings("rawtypes")
    @ValueSource(classes = {Map.class, LinkedHashMap.class})
    void deserializeInMap(Class<?> model) throws Exception {
        Object relations = mapper.readValue(
            "{\"predicate\":[{\"id\":\"1\",\"eclass\":\"eclass\"},{\"id\":\"2\",\"eclass\":\"eclass\"}]}",
            model
        );
        assertTrue(relations instanceof Map, "deserialized as map");
        Object refs = ((Map) relations).get("predicate");
        assertTrue(refs instanceof List, "contains list");
        List<?> l = (List<?>) refs;
        assertAll("should deserialize multiple references",
            () -> assertNotNull(refs, "array"),
            () -> assertEquals(2, l.size(), "item count"),
            () -> assertNotNull(l.get(0), "single item")
        );
    }

    @Test
    void equality() throws Exception {
        ObjectReference ref1 = ObjectReference.builder()
            .id("ID")
            .eclass("eclass")
            .type("type")
            .name("name")
            .build();
        ObjectReference ref2 = mapper.readValue(
            mapper.writeValueAsString(ref1),
            ObjectReference.class
        );
        assertAll("test objectreference equality",
            () -> assertEquals(ref1.getClass(), ref2.getClass(), "same type"),
            () -> assertEquals(ref1, ref2, "deserialized serialization should be equal"),
            () -> assertEquals(IO.json(ref1), IO.json(ref2), "JSON serialization"),
            () -> assertEquals(ref1.hashCode(), ref2.hashCode(), "hashcodes should match"),
            () -> assertEquals(ref1.toString(), ref2.toString(), "toString() should match")
        );
    }

    @Test
    void comparison() throws Exception {
        ObjectReference ref = ObjectReference.builder().id("ID").eclass("BTSThsEntry").build();
        assertThrows(
            NullPointerException.class,
            () -> {ref.compareTo(null);}
        );
        ObjectReference ref2 = ObjectReference.builder().id("ID").eclass("BTSText").build();
        assertAll("test assumptions about string comparisons",
            () -> assertEquals(3, "h".compareTo("e"), "distance between 'e' and 'h' should be 3"),
            () -> assertEquals(1, "e".compareTo(""), "distance between 'e' and empty string should be 1"),
            () -> assertEquals(-1, "".compareTo("e"), "inverse should be -1"),
            () -> assertEquals("e".compareTo(""), "h".compareTo(""), "should not matter what letter is being compared to empty string")
        );
        assertAll("ref should be > than ref2",
            () -> assertEquals(3, ref.compareTo(ref2), "compareTo should return positive value"),
            () -> assertEquals(3, ref.getEclass().compareTo(ref2.getEclass()), "comparing eclass values should return positive"),
            () -> assertEquals(-3, ref2.compareTo(ref), "inversed comparison should return positive")
        );
    }
    
}