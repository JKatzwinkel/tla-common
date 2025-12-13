package tla.domain.dto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import tla.domain.Util;
import tla.domain.dto.meta.AbstractDto;
import tla.domain.dto.meta.DocumentDto;
import tla.domain.model.EditorInfo;
import tla.domain.model.ObjectReference;
import tla.domain.model.meta.Resolvable;

public class AnnotationTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializeFromFile() throws Exception {
        AnnotationDto a = (AnnotationDto) Util.loadFromFile(
            Util.SAMPLE_DIR_PATHS.get(AnnotationDto.class),
            "5YUS7W6LUNC7DLP6SEIQDDKJHE.json"
        );
        assertAll("should be able to deserialize annotation",
            () -> assertNotNull(a, "should not be null"),
            () -> assertNotNull(a.getName(), "expect name"),
            () -> assertEquals("annotation.lemma", a.getPassport().extractPaths().get(0), "lemma annotation field should exist in passport"),
            () -> assertEquals("BTSAnnotation", a.getEclass(), "eclass must be `BTSAnnotation`"),
            () -> assertTrue(!a.getBody().isEmpty(), "text content"),
            () -> assertEquals(List.of("Sicher als Spießente (Anas acuta) identifiziert."), a.getBody(), "text content match")
        );
    }

    @Test
    void serialize() throws Exception {
        AnnotationDto a = new AnnotationDto();
        a.setEclass("BTSAnnotation");
        DocumentDto b = mapper.readValue(
            mapper.writeValueAsString(a),
            DocumentDto.class
        );
        assertAll("test serialize and deserialize again",
            () -> assertEquals(a, b),
            () -> assertTrue(b instanceof AnnotationDto),
            () -> assertEquals(a.hashCode(), b.hashCode()),
            () -> assertEquals(a.toString(), b.toString())
        );
    }

    @Test
    void equality() throws Exception {
        AnnotationDto a1 = tla.domain.util.IO.loadFromFile(
            "src/test/resources/sample/annotation/5YUS7W6LUNC7DLP6SEIQDDKJHE.json",
            AnnotationDto.class
        );
        AnnotationDto a2 = new AnnotationDto();
        a2.setName(a1.getName());
        a2.setEclass(a1.getEclass());
        a2.setId(a1.getId());
        a2.setType(a1.getType());
        a2.setBody(a1.getBody());
        a2.setReviewState(a1.getReviewState());
        LinkedHashMap<String, SortedSet<Resolvable>> relations = new LinkedHashMap<>();
        a1.getRelations().entrySet().stream().forEach(
            e -> {
                relations.put(
                    e.getKey(),
                    new AbstractDto.ObjectReferences(
                        e.getValue().stream().map(
                            ref -> new ObjectReference(
                                ref.getId(),
                                ref.getEclass(),
                                ref.getType(),
                                ref.getName(),
                                ref.getRanges()
                            )
                        ).collect(
                            Collectors.toList()
                        )
                    )
                );
            }
        );
        a2.setRelations(relations);
        a2.setPassport(a1.getPassport());
        a2.setExternalReferences(a1.getExternalReferences());
        a2.setEditors(
            EditorInfo.builder()
                .author(a1.getEditors().getAuthor())
                .contributors(a1.getEditors().getContributors())
                .updated(a1.getEditors().getUpdated())
                .type(a1.getEditors().getType())
                .build()
        );
        assertAll("procedurally built instance should equal deserialized instance with same contens",
            () -> assertEquals(a2, a1, "whole instance"),
            () -> assertEquals(a2.toString(), a1.toString(), "string repr"),
            () -> assertEquals(a2.hashCode(), a1.hashCode(), "hashcode")
        );

    }
}