package tla.domain.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import tla.domain.Util;
import tla.domain.dto.meta.NamedDocumentDto;
import tla.domain.model.ExternalReference;
import tla.domain.model.Language;
import tla.domain.model.ObjectReference;
import tla.domain.model.extern.AttestedTimespan;

import static org.junit.jupiter.api.Assertions.*;

public class ThsEntryTest {

    private ObjectMapper mapper = tla.domain.util.IO.getMapper();

    public static AttestedTimespan.Period loadThsEntryFromFileAndConvertToTimePeriod(String id) throws Exception {
        NamedDocumentDto term = (NamedDocumentDto) Util.loadFromFile("ths", String.format("%s.json", id));
        List<Integer> years = new ArrayList<>();
        List.of(
            "thesaurus_date.main_group.beginning",
            "thesaurus_date.main_group.end"
        ).stream().forEach(
            path -> {
                term.getPassport().extractProperty(path).stream().forEach(
                    node -> {
                        if (node.get() instanceof String) {
                            years.add(Integer.valueOf((String) node.get()));
                        }
                    }
                );
            }
        );
        Collections.sort(years);
        return AttestedTimespan.Period.builder()
            .begin(years.get(0))
            .end(years.get(1))
            .ref(ObjectReference.from(term))
            .build();
    }

    @Test
    void deserializeFromFile() throws Exception {
        ThsEntryDto t = (ThsEntryDto) Util.loadFromFile("ths", "2AVEQ3VFT5EEPF7NBH7RHCVBXA.json");
        assertAll("deserialized thesaurus entry and its fields should not be null",
            () -> assertNotNull(t, "thesaurus entry itself should not be null"),
            () -> assertTrue(t instanceof ThsEntryDto, "should be ths term instance"),
            () -> assertNotNull(t.getEclass(), "eclass should not be null"),
            () -> assertNotNull(t.getSUID(), "short unique id"),
            () -> assertEquals("mkgyci", t.getSUID(), "short id correct"),
            () -> assertEquals("BTSThsEntry", t.getEclass(), "eclass should be 'BTSThsEntry'"),
            () -> assertNotNull(t.getEditors(), "editor info should not be null"),
            () -> assertNotNull(t.getName(), "name should not be null"),
            () -> assertTrue(t.getExternalReferences() != null, "eternal references should not be null"),
            () -> assertTrue(t.getType() != null, "type should not be null"),
            () -> assertTrue(t.getRelations() != null, "relations should not be null"),
            () -> assertTrue(t.getId() != null, "id should not be null"),
            () -> assertNotNull(t.getReviewState(), "review state should not be null"),
            () -> assertNull(((ThsEntryDto)t).getSortKey(), "sort key should be null"),
            () -> assertNull(t.getSubtype(), "subtype should be null"),
            () -> assertEquals("BTSThsEntry", t.getEclass(), "eclass must be `BTSThsEntry`"),
            () -> assertNotNull(t.getPaths(), "object tree path(s)"),
            () -> assertEquals(1, t.getPaths().size(), "one path"),
            () -> assertEquals(3, t.getPaths().get(0).size(), "level 3")
        );
        assertAll("external references should be deserialized correctly",
            () -> assertEquals(2, t.getExternalReferences().size(), "2 providers should be present"),
            () -> assertEquals("aaew", t.getExternalReferences().firstKey(), "first provider should be 'aaew_1'"),
            () -> assertEquals(1, t.getExternalReferences().get("thot").size(), "provider 'thot' should specify exactly 1 value")
        );
        assertAll("editor info should be correct",
            () -> assertEquals(1, t.getEditors().getContributors().size(), "exactly 1 contributor should be specified"),
            () -> assertTrue(t.getEditors().getContributors().contains("L. Popko"), "contributor name should be correct"),
            () -> assertEquals("user", t.getEditors().getType(), "author account type should be 'user'")
        );
        assertAll("relations should be correctly deserialized",
            () -> assertEquals(2, t.getRelations().size(), "2 types of relation should be specified"),
            () -> assertEquals(8, t.getRelations().get("contains").size(), "exactly 8 relations of type 'contains' should be specified")
        );
    }


    @Test
    void equality() throws Exception {
        ThsEntryDto t1 = mapper.readValue(
            "{\"eclass\":\"BTSThsEntry\",\"id\":\"1\",\"subtype\":\"st\",\"externalReferences\":{\"thot\":[{\"id\":\"ID\",\"type\":\"TYPE\"}]},"
            + "\"suid\":\"xxx\",\"translations\":{\"de\":[\"ja\"]}}",
            ThsEntryDto.class
        );
        ThsEntryDto t2 = ThsEntryDto.builder().id("1").subtype("st")
            .externalReference("thot", new TreeSet<>(
                List.of(ExternalReference.builder().id("ID").type("TYPE").build()))
            ).SUID("xxx")
            .translations(
                Map.of(Language.DE, List.of("ja"))
            ).build();
        assertAll("deserialized thesaurus entry should be equal to procedurally built",
            () -> assertTrue(List.of(t1).contains(t2), "asserting equality"),
            () -> assertEquals(t1, t2, "asserting equality"),
            () -> assertEquals(t1.toString(), t2.toString(), "asserting toString() equality"),
            () -> assertEquals(t1.getId(), t2.getId(), "equal ID"),
            () -> assertEquals(t1.getSUID(), t2.getSUID(), "equal short ID"),
            () -> assertEquals(t1.getTranslations(), t2.getTranslations(), "equal translations"),
            () -> assertEquals(t1.getExternalReferences(), t2.getExternalReferences(), "equal externalreferences")
        );
    }

}
