package tla.error;

import org.junit.jupiter.api.Test;

import static tla.domain.util.IO.json;

import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionTests {

    @Test
    void serialize404() throws Exception {
        TlaStatusCodeException e = new ObjectNotFoundException("33410");
        String s = json(e);
        assertAll("serialize exception",
            () -> assertTrue(s.contains("\"code\":404"), "status code in"),
            () -> assertTrue(s.contains("\"objectId\":\"33410\""), "ID in"),
            () -> assertTrue(!s.contains("\"eclass\":"), "no eclass"),
            () -> assertTrue(s.contains("\"message\":\"The object with ID '33410'"), "message in"),
            () -> assertTrue(!s.contains("\"stackTrace\":"), "no stacktrace"),
            () -> assertTrue(s.contains("\"@class\":\"tla.error.ObjectNotFoundException\""), "class in")
        );
    }

    @Test
    void deserialize404() throws Exception {
        String s = json(new ObjectNotFoundException("d222"));
        TlaStatusCodeException e = new ObjectMapper().readValue(
            s, TlaStatusCodeException.class
        );
        assertAll("deserialize exception",
            () -> assertTrue(e instanceof ObjectNotFoundException, "is 404 error"),
            () -> assertEquals(404, e.getCode(), "code 404"),
            () -> assertTrue(e.getMessage().startsWith("The object with ID 'd222'"), "msg")
        );
    }
}