package tla.domain.command;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import tla.domain.command.PassportSpec.LiteralPassportValue;
import tla.domain.command.PassportSpec.ThsRefPassportValue;

public class PassportSpecTest {

    @Test
    void deserialize() throws Exception {
        var s = "{\"data\":{\"values\":[\"XX\"],\"expand\":true},\"comment\":{\"values\":[\"bla\"]}}";
        var pp = tla.domain.util.IO.getMapper().readValue(s, PassportSpec.class);
        assertAll(
          () -> assertTrue(pp.get("data") instanceof ThsRefPassportValue),
          () -> assertTrue(pp.get("comment") instanceof LiteralPassportValue)
        );
    }

    @Test
    void deserializeInCommand() throws Exception {
        var s = "{\"@class\":\"tla.domain.command.TextSearch\",\"passport\":{\"date\":{\"values\":[\"XX\"],\"expand\":true}}}";
        var tsc = tla.domain.util.IO.getMapper().readValue(s, TextSearch.class);
        assertNotNull(tsc.getPassport());
    }

    @Test
    void mergePassportSpecs() {
        var pp = new PassportSpec();
        pp.put("date", ThsRefPassportValue.of(List.of("THSID1"), true));
        pp.put("date", ThsRefPassportValue.of(List.of("THSID2"), true));
        pp.put("comment", LiteralPassportValue.of(List.of("bla")));
        pp.put("comment", LiteralPassportValue.of(List.of("blubb")));
        assertAll("putting 2 values for same key results in merge",
            () -> assertEquals(2, pp.get("date").getValues().size()),
            () -> assertTrue(pp.get("date") instanceof ThsRefPassportValue),
            () -> assertTrue(((ThsRefPassportValue) pp.get("date")).isExpand()),
            () -> assertEquals(2, pp.get("comment").getValues().size())
        );
        var dd = ThsRefPassportValue.of(List.of("THSID1", "THSID2"), false);
        pp.put("date", dd);
        assertFalse(((ThsRefPassportValue) pp.get("date")).isExpand());
    }

}
