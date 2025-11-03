package tla.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import tools.jackson.databind.ObjectMapper;

public class ExternalReferenceTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void comparison() throws Exception {
        ExternalReference e1 = ExternalReference.builder().id("10070").build();
        ExternalReference e2 = mapper.readValue(
            "{\"id\":\"10070\"}",
            ExternalReference.class
        );
        assertAll("both instances should not be null",
            () -> assertTrue(e1 != null, "e1 should not be null"),
            () -> assertTrue(e2 != null, "e2 should not be null")
        );
        assertAll("comparison should indicate no difference",
            () -> assertDoesNotThrow(() -> {e1.compareTo(e2);}, "no exception should be thrown"),
            () -> assertEquals(0, e1.compareTo(e2), "compareTo should return 0"),
            () -> assertEquals(0, e2.compareTo(e1), "compareTo should return 0"),
            () -> assertEquals(e1, e2, "instances should be equal")
        );
        assertThrows(
            NullPointerException.class,
            () -> {e1.compareTo(null);}
        );
        e1.setType("a");
        e2.setType("b");
        ExternalReference e3 = ExternalReference.builder().id("10071").build();
        assertAll("instances should be ordered based on type",
            () -> assertEquals(e1.compareTo(e2), e1.getType().compareTo(e2.getType()), "instance comparison should return the same result as type value comparison"),
            () -> assertNotEquals(e1, e2, "expect no equality"),
            () -> assertEquals(1, e1.compareTo(e3), "instance with type value should be > than instance without"),
            () -> assertEquals(1, e2.compareTo(e3), "instance with type value should be > than instance without"),
            () -> assertEquals(-1, e3.compareTo(e1), "instance without type value should be < than instance with")
        );
    }

    
}