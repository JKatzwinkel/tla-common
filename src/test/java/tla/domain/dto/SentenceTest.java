package tla.domain.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import tla.domain.Util;
import tla.domain.command.TypeSpec;
import tla.domain.model.SentenceToken;
import tla.domain.model.Transcription;

public class SentenceTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserialize() throws Exception {
        SentenceDto s = (SentenceDto) Util.loadFromFile("sentence", "IBUBd3xeVnQoNUbMi4MAnDRUenw.json");
        assertAll("check deserialized properties",
            () -> assertNotNull(s, "sentence should not be null"),
            () -> assertEquals("HS", s.getType(), "type"),
            () -> assertNotNull(s.getContext(), "context"),
            () -> assertNotNull(s.getContext().getParagraph(), "paragraph"),
            () -> assertEquals(117, s.getContext().getPosition(), "position in sentences array"),
            () -> assertNotNull(s.getTranslations(), "sentence translations"),
            () -> assertNotNull(s.getTokens(), "sentence tokens"),
            () -> assertFalse(s.getTokens().isEmpty(), "tokens exist"),
            () -> assertTrue(
                s.getTokens().get(0).getAnnoTypes().contains("subtext"),
                "token annotated with subtext"
            ),
            () -> assertNotNull(s.getRelations(), "sentence relations"),
            () -> assertFalse(s.getRelations().isEmpty(), "relations exist")
        );
        SentenceDto s2 = new SentenceDto();
        s2.setContext(s.getContext());
        s2.setId(s.getId());
        s2.setType(s.getType());
        s2.setTranscription(s.getTranscription());
        s2.setTranslations(s.getTranslations());
        s2.setRelations(s.getRelations());
        s2.setTokens(s.getTokens());
        assertAll("assert equality",
            () -> assertEquals(s, s2, "instance"),
            () -> assertEquals(s.hashCode(), s2.hashCode(), "hashcode")
        );
    }

    @Test
    void serialize() throws Exception {
        SentenceDto dto = new SentenceDto();
        dto.setTranscription(new Transcription());
        dto.getTranscription().setMdc("");
        SentenceToken t = new SentenceToken();
        t.setLemma(new SentenceToken.Lemmatization("", new TypeSpec()));
        t.setFlexion(new SentenceToken.Flexion());
        t.setType("lc");
        dto.setTokens(List.of(t));
        String s = mapper.writeValueAsString(dto);
        assertAll("serialize minimal sentence dto",
            () -> assertEquals("{\"eclass\":\"BTSSentence\",\"tokens\":[{\"type\":\"lc\"}]}", s)
        );
    }

    @Test
    void deserializeDefaults() throws Exception {
        SentenceDto dto = mapper.readValue(
            "{\"eclass\":\"BTSSentence\",\"tokens\":[{}]}",
            SentenceDto.class
        );
        assertAll("check for null contents",
            () -> assertNotNull(dto.getTokens().get(0), "token"),
            () -> assertNull(dto.getTokens().get(0).getTranscription(), "token transcription"),
            () -> assertNotNull(dto.getTokens().get(0).getFlexion(), "flexion"),
            () -> assertNotNull(dto.getTokens().get(0).getLemma(), "lemma info")
        );
    }
}