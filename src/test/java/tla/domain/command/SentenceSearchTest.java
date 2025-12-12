package tla.domain.command;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import tla.domain.model.Language;
import tla.domain.model.SentenceToken.Lemmatization;
import tla.domain.model.Transcription;

public class SentenceSearchTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void donotSerializeEmptyElements() throws Exception {
        SentenceSearch cmd = new SentenceSearch();
        SentenceSearch.TokenSpec token = new SentenceSearch.TokenSpec();
        token.setLemma(new Lemmatization());
        token.getLemma().setPartOfSpeech(new TypeSpec());
        token.setTranscription(new Transcription());
        token.setTranslation(new TranslationSpec());
        cmd.setTokens(List.of(token));
        cmd.setTranslation(new TranslationSpec());
        cmd.getTranslation().setLang(new Language[]{});
        cmd.setType(new TypeSpec("", " "));
        cmd.setPassport(new PassportSpec());
        String s = mapper.writeValueAsString(cmd);
        assertAll("should not serialize any content",
            () -> assertEquals(
                String.format("{\"@class\":\"%s\",\"@dto\":\"tla.domain.dto.SentenceDto\"}", cmd.getClass().getName()),
                s,
                "should be only @class and @dto"
            )
        );
    }

    @Test
    void doSerializeNonEmptyElements() throws Exception {
        SentenceSearch cmd = new SentenceSearch();
        SentenceSearch.TokenSpec token = new SentenceSearch.TokenSpec();
        token.setLemma(new Lemmatization("10070", new TypeSpec()));
        cmd.setTokens(List.of(token));
        cmd.setTranslation(new TranslationSpec());
        cmd.getTranslation().setLang(new Language[]{Language.DE});
        cmd.setType(new TypeSpec("", " "));
        cmd.setPassport(new PassportSpec());
        String s = mapper.writeValueAsString(cmd);
        assertAll("should serialize including specifications",
            () -> assertNotEquals(
                String.format("{\"@class\":\"%s\",\"@dto\":\"tla.domain.dto.SentenceDto\"}", cmd.getClass().getName()),
                s,
                "should not be only @class and @dto"
            ),
            () -> assertTrue(s.contains("tokens"), "tokens specs"),
            () -> assertTrue(s.contains("translation"), "translation specs")
        );
    }

}