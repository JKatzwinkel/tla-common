package tla.domain.command;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import tla.domain.model.Language;
import tla.domain.model.Script;

import static org.junit.jupiter.api.Assertions.*;
import static tla.domain.util.IO.json;

public class LemmaSearchTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserialize() throws Exception {
        LemmaSearch form = mapper.readValue(
            "{\"@class\":\"tla.domain.command.LemmaSearch\",\"script\":[\"hieratic\"],\"translation\":{\"text\":\"trans\",\"lang\":[\"en\"]},\"transcription\":\"nfr\",\"bibliography\":\"wb\",\"wordClass\":{\"type\":\"noun\"},\"sort\":\"sortKey.desc\"}",
            LemmaSearch.class
        );
        assertAll("does deserialized search form have all expected properties?",
            () -> assertNotNull(form.getTranslation(), "expect translation"),
            () -> assertEquals(1, form.getTranslation().getLang().length, "1 translation lang"),
            () -> assertEquals(Language.EN, form.getTranslation().getLang()[0], "english among translation languages?"),
            () -> assertEquals(1, form.getScript().length, "1 script"),
            () -> assertEquals(Script.HIERATIC, form.getScript()[0], "script is hieratic"),
            () -> assertEquals("wb", form.getBibliography(), "wb mentioned in bibliography passport field"),
            () -> assertNotNull(form.getWordClass(), "part of speech/wordclass is defined"),
            () -> assertEquals("noun", form.getWordClass().getType(), "correct POS"),
            () -> assertNotNull(form.getSort(), "sort order specs")
        );
    }

    @Test
    void serialize() throws Exception {
        LemmaSearch form = new LemmaSearch();
        form.setWordClass(new TypeSpec());
        form.setAnno(new TypeSpec());
        form.setTranslation(new TranslationSpec());
        String s = json(form);
        form.getTranslation().setLang(new Language[]{});
        String s2 = json(form);
        assertAll("lemma search command serialization",
            () -> assertTrue(!s.contains("pos"), "no pos in ser"),
            () -> assertTrue(!s.contains("annotationType"), "no anno type in ser"),
            () -> assertTrue(!s.contains("translation"), "no translation in ser"),
            () -> assertTrue(!s.contains("lang"), "no translation lang in ser"),
            () -> assertTrue(!s2.contains("translation"), "no translation")
       );
    }

}
