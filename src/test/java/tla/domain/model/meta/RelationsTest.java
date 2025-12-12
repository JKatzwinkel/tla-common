package tla.domain.model.meta;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import tla.domain.model.ObjectReference;
import tla.domain.util.IO;

public class RelationsTest {

    private ObjectMapper mapper = IO.getMapper();

    private Resolvable objRef(String start, String end) {
        return ObjectReference.builder()
            .id("ID")
            .eclass("BTSText")
            .ranges(
                List.of(
                    Resolvable.Range.of(start, end)
                )
            ).build();
    }

    @Test
    void serializeRangedRelation() {
        var ref = objRef("START", "END");
        var s = IO.json(ref);
        assertAll("check ranged object reference serialization",
            () -> assertTrue(s.contains("START"), "start token"),
            () -> assertTrue(s.contains("\"start\":"), "start token key"),
            () -> assertTrue(s.contains("END"), "end token"),
            () -> assertTrue(s.contains("\"end\":"), "end token key"),
            () -> assertTrue(
                s.indexOf("START") < s.indexOf("END"),
                "start token comes first"
            )
        );
    }

    @Test
    void serializeRangedRelation_partialRange() {
        var ref = objRef("START", null);
        var s = IO.json(ref);
        assertAll("check partially ranged object reference serialization",
            () -> assertTrue(s.contains("START"), "start token"),
            () -> assertTrue(s.contains("\"start\":"), "start token key"),
            () -> assertFalse(s.contains("\"end\":"), "no end token key"),
            () -> assertFalse(s.contains("\"to\":"), "no end token key")
        );
    }

    @Test
    void deserializeRangedRelation() throws Exception {
        Resolvable ref = mapper.readValue(
            "{\"id\":\"ID\",\"eclass\":\"ECLASS\",\"ranges\":[{\"start\":\"START\"}]}",
            ObjectReference.class
        );
        assertAll("check partially ranged object reference deserialization",
            () -> assertNotNull(ref.getRanges(), "ranges deserialized"),
            () -> assertEquals(1, ref.getRanges().size(), "exactly 1 range"),
            () -> assertNotNull(ref.getRanges().get(0).getStart(), "start token found"),
            () -> assertEquals("START", ref.getRanges().get(0).getStart(), "start token correct"),
            () -> assertNull(ref.getRanges().get(0).getEnd(), "no end token")
        );
    }

}
