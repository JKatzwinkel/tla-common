package tla.domain.dto;

import org.junit.jupiter.api.Test;

import tla.domain.dto.meta.DocumentDto;
import tla.domain.model.Language;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import tools.jackson.databind.ObjectMapper;

public class DocumentTest {

    private ObjectMapper mapper = new ObjectMapper();    

    @Test
    void equality() throws Exception {
        DocumentDto d1 = LemmaDto.builder().id("1").sortKey("2").translation(Language.DE, List.of("3")).build();
        DocumentDto d2 = ThsEntryDto.builder().id("1").sortKey("2").translation(Language.DE, List.of("3")).build();
        assertAll("instances of different document classes with same values should not equal",
            () -> assertNotEquals(d1, d2, "asserting non-equality"),
            () -> assertEquals(d1.getId(), d2.getId(), "equal ID however")
        );
        DocumentDto d = mapper.readValue(
            "{\"id\":\"1\",\"eclass\":\"BTSLemmaEntry\",\"sortKey\":\"2\",\"translations\":{\"de\":[\"3\"]}}",
            LemmaDto.class
        );
        assertAll("deserialized document should be equal to procedural build",
            () -> assertEquals(d1, d, "asserting equality")
        );
    }

    @Test
    void instantiateWrongEclass() {
        assertAll("attempt to instantiate ths entry DTO from object with eclass BTSLemmaEntry should throw illegalargumentexception",
            () -> assertThrows(
                tools.jackson.databind.exc.InvalidTypeIdException.class,
                () -> {mapper.readValue(
                    "{\"id\":\"1\",\"eclass\":\"BTSLemmaEntry\"}",
                    ThsEntryDto.class
                );}
            )
        );
    }
}