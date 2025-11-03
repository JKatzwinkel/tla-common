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

    private ObjectMapper mapper = tla.domain.util.IO.getMapper();

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
            () -> assertEquals(
                """
                {"eclass":"BTSSentence","context":{"position":0},"tokens":[{"type":"lc"}]}
                """.trim(), s
            )
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
            () -> assertNotNull(dto.getTokens().get(0).getLemma(), "lemma info"),
            () -> assertNotNull(dto.getContext(), "sentence context"),
            () -> assertEquals(
                1, dto.getContext().getVariants(),
                "unless specified, sentences have variants count of 1"
            )
        );
    }

    @Test
    void deserializeSentenceContextDefaultValues() {
        var dto = mapper.readValue(
            """
            {
                "eclass": "BTSSentence",
                "id": "s1",
                "context": {
                    "textId": "t1"
                }
            }
            """, SentenceDto.class
        );
        assertAll(tla.domain.util.IO.json(dto, "  "),
            () -> assertNotNull(dto.getId(), "sentence id"),
            () -> assertNotNull(dto.getContext(), "sentence context"),
            () -> assertNotNull(dto.getContext().getTextId(), "text id"),
            () -> assertEquals(
                0, dto.getContext().getPosition(), "sentence position defaults to 0"
            ),
            () -> assertEquals(
                1, dto.getContext().getVariants(), "sentence variants count defaults to 1"
            )
        );
    }

    @Test
    void serializeSentenceContextDefaultValues() {
        var dto = mapper.readValue(
            """
            {
                "eclass": "BTSSentence",
                "id": "s1",
                "context": {
                    "textId": "t1",
                    "position": 0,
                    "variants": 1
                }
            }
            """, SentenceDto.class
        );
        var json = tla.domain.util.IO.json(dto.getContext(), "  ");
        assertEquals(
            """
            {
              "textId": "t1",
              "position": 0
            }
            """.trim(), json
        );
    }

    @Test
    void deserializeSentenceContextNonDefaultValues() {
        var dto = mapper.readValue(
            """
            {
                "eclass": "BTSSentence",
                "id": "s1",
                "context": {
                    "textId": "t1",
                    "position": 1,
                    "variants": 2
                }
            }
            """, SentenceDto.class
        );
        assertAll(tla.domain.util.IO.json(dto),
            () -> assertEquals(1, dto.getContext().getPosition()),
            () -> assertEquals(2, dto.getContext().getVariants())
        );
    }

    @Test
    void serializeSentenceContextNonDefaultValues() {
        var dto = mapper.readValue(
            """
            {
                "eclass": "BTSSentence",
                "id": "s1",
                "context": {
                    "textId": "t1",
                    "pos": 1,
                    "variants": 2
                }
            }
            """, SentenceDto.class
        );
        assertEquals(
            """
            {
              "textId": "t1",
              "position": 1,
              "variants": 2
            }
            """.trim(),
            tla.domain.util.IO.json(dto.getContext(), "  ")
        );
    }

}
