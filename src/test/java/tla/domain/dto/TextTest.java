package tla.domain.dto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tla.domain.Util;
import tla.domain.model.ExternalReference;
import tla.domain.model.ObjectPath;
import tla.domain.model.ObjectReference;
import tla.domain.model.Passport;
import tla.domain.model.meta.Resolvable;
import tla.domain.util.IO;

public class TextTest {

    @Test
    void deserializeFromFile() throws Exception {
        TextDto t = (TextDto) Util.loadFromFile("text", "A2HWO5CX6RCQ5IBSV2MHAAIYA4.json");
        assertAll("deserialization from file should preserve all contents",
            () -> assertEquals("tlademotic", t.getCorpus()),
            () -> assertTrue(t.getWordCount().min() > 0),
            () -> assertTrue(t.getPaths() != null, "corpus object paths should be deserialized"),
            () -> assertNotNull(t.getSUID(), "short unique ID"),
            () -> assertEquals(1, t.getPaths().size(), "expect exactly 1 path"),
            () -> assertEquals(5, t.getPaths().get(0).size(), "count path segments"),
            () -> assertEquals("6WFOSXHVQRGGNAG5FCM6QEXWR4", t.getPaths().get(0).get(0).getId(), "segment not empty")
        );
        Passport pp = t.getPassport();
        List<Passport> leafNodes = pp.extractProperty("object.description_of_object.type");
        assertAll("passport contents should be preserved",
            () -> assertNotNull(pp, "expect passport"),
            () -> assertEquals(1, leafNodes.size(), "expect exactly 1 value for property path `object.description_of_object.type`"),
            () -> assertTrue(leafNodes.get(0).get() instanceof ObjectReference, "leaf node should be thesaurus entry reference"),
            () -> assertEquals("Mumienetikett", ((ObjectReference)leafNodes.get(0).get()).getName(), "check ths ref leaf node name")
        );
    }


    @Test
    void textBuilder() {
        CorpusObjectDto parent = CorpusObjectDto.builder().id("2").name("papyrus").type("type").build();
        Resolvable r = parent.toObjectReference();
        List<ObjectPath> paths = List.of(ObjectPath.of(r));
        TextDto t1 = TextDto.builder()
            .id("1")
            .SUID("xy")
            .corpus("corpus")
            .paths(paths)
            .externalReference("trismegistos", new TreeSet<>(Set.of(new ExternalReference("xx", "text"))))
            .build();
        TextDto t2 = TextDto.builder()
            .id("1")
            .SUID("xy")
            .corpus("corpus")
            .paths(paths)
            .externalReferences(
                Map.of(
                    "trismegistos", new TreeSet<>(Set.of(ExternalReference.builder().id("xx").type("text").build()))
                )
            ).build();
        assertEquals(t1, t2, "two builder-built instances should be equal");
        assertEquals(t1.toString(), t2.toString(), "both instances should serialize into same toString() result");
        assertEquals(t1.getPaths(), t2.getPaths(), "paths should be the same");
        assertTrue(IO.json(t1).contains("\"suid\":\"xy\""), "short ID serialized as 'suid'");
        assertEquals(
            "[[{\"eclass\":\"BTSTCObject\",\"id\":\"2\",\"name\":\"papyrus\",\"type\":\"type\"}]]",
            tla.domain.util.IO.json(t1.getPaths()),
            "paths serialization"
        );
    }

    @Test
    @DisplayName("serialize wordcount")
    void serializeWordCount() {
        var dto = new TextDto();
        dto.setWordCount(new TextDto.WordCount(23));
        var json = tla.domain.util.IO.json(dto);
        assertTrue(
            json.contains(
                """
                    "wordCount":{"min":23,"max":23}
                """.trim()
            ), json
        );
    }

}
