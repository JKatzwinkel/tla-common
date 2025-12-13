package tla.domain.model.extern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tla.domain.dto.ThsEntryTest;
import tla.domain.model.ObjectReference;
import tla.domain.model.extern.AttestedTimespan.AttestationStats;
import tla.domain.model.extern.AttestedTimespan.Period;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import tools.jackson.databind.ObjectMapper;

public class AttestationsTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void serialize() throws Exception {
        var t = AttestedTimespan.builder()
            .contains(
                List.of(
                    new AttestedTimespan(),
                    AttestedTimespan.builder()
                        .attestations(
                            AttestationStats.builder()
                                .objects(13)
                                .sentences(17)
                                .texts(23)
                                .build()
                        ).build(),
                    AttestedTimespan.builder()
                        .attestations(
                            AttestationStats.builder()
                                .objects(5)
                                .sentences(7)
                                .texts(11)
                                .build()
                        ).build()
                )
            ).build();
        String s = mapper.writeValueAsString(t);
        assertEquals(1, s.split("\"contains\":").length - 1, "contains-field not serialized for child nodes");
    }

    @Test
    void attestationSum() throws Exception {
        AttestationStats t1 = AttestationStats.builder().objects(5).sentences(7).texts(11).build();
        AttestationStats t2 = AttestationStats.builder().objects(13).sentences(17).texts(19).build();
        t1.add(t2);
        assertAll("attestation stats should add up",
            () -> assertEquals(18, t1.getObjects(), "object count should match"),
            () -> assertEquals(24, t1.getSentences(), "sentence count should match"),
            () -> assertEquals(30, t1.getTexts(), "text count should match")
        );
    }

    @Test
    void periodCompare_noOverlap() throws Exception {
        Period siam = ThsEntryTest.loadThsEntryFromFileAndConvertToTimePeriod(
            "677YHBKQIRHB3HVZG45V2N6DU4"
        );
        Period thut = ThsEntryTest.loadThsEntryFromFileAndConvertToTimePeriod(
            "WMSRSEY4LBALVAQEKU4GKNLTMU"
        );
        assertAll("compare attestation periods",
            () -> assertTrue(thut.compareTo(siam) < 0, "Thutmosis earlier than siamun"),
            () -> assertTrue(siam.compareTo(thut) > 0, "siamun later than thutmosis"),
            () -> assertTrue(thut.getEnd() < siam.getBegin(), "no overlap"),
            () -> assertFalse(thut.contains(siam), "no inclusion"),
            () -> assertNotEquals(thut, siam, "should not be equal")
        );
        Period test = new Period(thut.getBegin() + 1, thut.getEnd());
        assertTrue(thut.contains(test));
    }

    @Test
    @DisplayName("overlapping periods should compare correctly: same left boundary")
    void periodCompare_leftOverlap() {
        Period p1 = new Period(0, 5);
        Period p2 = new Period(0, 7);
        Period p3 = new Period(0, 10);
        assertAll("periods starting in the same year should compare based on when they end",
            () -> assertTrue(p2.contains(p1), "containment should be true"),
            () -> assertTrue(p1.compareTo(p3) < 0, "period ending earlier should compare as negative"),
            () -> assertTrue(p3.compareTo(p1) > 0, "period ending later should compare as positive"),
            () -> assertNotEquals(p1, p3, "should not be equal"),
            () -> assertEquals(p1.compareTo(p2), p2.compareTo(p3), "compare should be transitive")
        );
    }

    @Test
    @DisplayName("periods ending in the same year should be ordered by granularity (finer coming first)")
    void periodComapre_rightOverlap() {
        Period p1 = new Period(0, 10);
        Period p2 = new Period(5, 10);
        Period p3 = new Period(0, 5);
        assertAll("periods starting in the same year should be ordered by how short they are",
            () -> assertTrue(p2.compareTo(p1) < 0, "shorter period should come first"),
            () -> assertTrue(p1.compareTo(p2) > 0, "longer period should come last"),
            () -> assertEquals(p2.compareTo(p1), p3.compareTo(p2), "compare should be transitive"),
            () -> assertEquals(p2.compareTo(p1), p3.compareTo(p1), "compare should be consistent")
        );
    }

    @Test
    @DisplayName("overlapping periods should compare correctly: equality")
    void periodCompare_equality() {
        Period p1 = new Period(0, 5);
        Period p2 = new Period(0, 5);
        assertFalse(p1.equals(null), "should handle equality check against null");
        assertTrue(
            (p1.compareTo(p2) == 0) == p1.equals(p2), "natural ordering should be consistent with equals"
        );
    }

    @Test
    @DisplayName("periods should be sorted by the year in which they end && by duration")
    void periodSort() {
        List<Period> periods = new ArrayList<>();
        periods.addAll(List.of(
            new Period(0, 30),
            new Period(10, 20),
            new Period(0, 10),
            new Period(20, 30)
        ));
        Collections.sort(periods);
        assertEquals(
            List.of("0-10", "10-20", "20-30", "0-30"),
            periods.stream().map(p -> String.format("%d-%d", p.getBegin(), p.getEnd())).collect(
                Collectors.toList()
            )
        );
    }

    @Test
    void deserializePeriod() throws Exception {
        Period p1 = ThsEntryTest.loadThsEntryFromFileAndConvertToTimePeriod(
            "677YHBKQIRHB3HVZG45V2N6DU4"
        );
        String ser = mapper.writeValueAsString(p1);
        Period p2 = mapper.readValue(ser, Period.class);
        assertAll("test deserialization of time period DTO support type",
            () -> assertNotNull(p2, "should not be null"),
            () -> assertEquals(-986, p2.getBegin(), "first year should match"),
            () -> assertTrue(p2.getRef() instanceof ObjectReference, "expect object reference"),
            () -> assertEquals("BTSThsEntry", p2.getRef().getEclass(), "expect thesaurus reference")
        );
    }

}