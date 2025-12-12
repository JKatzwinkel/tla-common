package tla.domain.dto.extern;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import tla.domain.Util;
import tla.domain.command.LemmaSearch;
import tla.domain.dto.meta.AbstractDto;
import tla.domain.dto.LemmaDto;
import tla.domain.dto.ThsEntryDto;

public class WrapperTest {

    private ObjectMapper mapper = tla.domain.util.IO.getMapper();

    @Test
    void addRelated() throws Exception {
        LemmaDto l = (LemmaDto) Util.loadFromFile("lemma", "10070.json");
        ThsEntryDto t = (ThsEntryDto) Util.loadFromFile("ths", "2AVEQ3VFT5EEPF7NBH7RHCVBXA.json");
        SingleDocumentWrapper<LemmaDto> w = SingleDocumentWrapper.<LemmaDto>builder()
            .doc(l)
            .build();
        w.addRelated(t);
        assertAll("check single documentwrapper",
            () -> assertNotNull(w.getDoc(), "should contain payload"),
            () -> assertTrue(w.getDoc() instanceof LemmaDto, "payload should be lemma"),
            () -> assertNotNull(w.getRelated(), "should contain related object map"),
            () -> assertEquals(1, w.getRelated().size(), "expect 1 eclass in related map"),
            () -> assertTrue(w.getRelated().containsKey(t.getEclass()), "only ths entry eclass should be in related map"),
            () -> assertTrue(w.getRelated().get(t.getEclass()).containsKey(t.getId()), "ths entry should be stored under its ID"),
            () -> assertEquals(t, w.getRelated().get(t.getEclass()).get(t.getId()), "ths entry should be in data structure")
        );
        w.addRelated(t);
        assertEquals(1, w.getRelated().get(t.getEclass()).size(), "number of ths entries should have stayed the same");
        ThsEntryDto t2 = (ThsEntryDto) Util.loadFromFile("ths", "ACJUYKAESFH4JAWU2KOOHKW3HM.json");
        w.addRelated(t2);
        assertAll("adding another related object of same type",
            () -> assertEquals(2, w.getRelated().get(t.getEclass()).size(), "expect 2 related ths entries now")
        );
        assertAll("related objects map should never be returned null",
            () -> assertNotNull(new SingleDocumentWrapper<>(l).getRelated(), "container instantiated by passing lemma to constructor"),
            () -> assertDoesNotThrow(
                () -> new SingleDocumentWrapper<>(l).addRelated(t), "container instantiated via one-param container can add related object"
            ),
            () -> assertNotNull(new SingleDocumentWrapper<>().getRelated(), "container instantiated using no args constructor"),
            () -> assertDoesNotThrow(
                () -> new SingleDocumentWrapper<>().addRelated(t), "container instantiated via no args constructor can add related object"
            )
        );
    }

    @Test
    void deserializeDetails() throws Exception {
        LemmaDto l = (LemmaDto) Util.loadFromFile("lemma", "10070.json");
        ThsEntryDto t = (ThsEntryDto) Util.loadFromFile("ths", "2AVEQ3VFT5EEPF7NBH7RHCVBXA.json");
        SingleDocumentWrapper<LemmaDto> w = new SingleDocumentWrapper<>(l);
        w.addRelated(t);
        SingleDocumentWrapper<? extends AbstractDto> w2 = SingleDocumentWrapper.from(mapper.writeValueAsString(w));
        assertAll("test lemma details container deserialization",
            () -> assertEquals(
                tla.domain.util.IO.json(w),
                tla.domain.util.IO.json(w2),
                "deserialized instance should serialize back to origin"
            ),
            () -> assertEquals(w.getDoc(), w2.getDoc(), "payloads should be equal"),
            () -> assertNotEquals(w.toString(), w2.toString(), "expect different string repr"),
            () -> assertEquals(w.getDoc().toString(), w2.getDoc().toString(), "toString repr of both instance's payload should equal"),
            () -> assertEquals(w.getDoc().hashCode(), w2.getDoc().hashCode(), "hashcodes of payloads should be same"),
            () -> assertTrue(w.getDoc() instanceof LemmaDto, "payload of constructed container should be lemmadto instance"),
            () -> assertTrue(w2.getDoc() instanceof LemmaDto, "payload of deserialized container should be lemmadto instance"),
            () -> assertEquals(w.getRelated(), w2.getRelated(), "related objects of both containers should be equal")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void deserializeDetailsEmptyRelated() throws Exception {
        SingleDocumentWrapper<AbstractDto> w = mapper.readValue(
            "{\"doc\":{\"id\":\"ID\",\"eclass\":\"BTSText\"}}",
            SingleDocumentWrapper.class
        );
        assertAll("test minimal details container deserialization",
            () -> assertNotNull(w.getRelated(), "related objects should never be null even if missing")
        );
    }

    @Test
    void searchResultsWrapperTest() throws Exception {
        PageInfo p = PageInfo.builder()
            .numberOfElements(1)
            .totalElements(1)
            .totalPages(1)
            .size(20)
            .build();
        LemmaDto l = (LemmaDto) Util.loadFromFile("lemma", "10070.json");
        SearchResultsWrapper<LemmaDto> searchResults = new SearchResultsWrapper<>(
            List.of(l),
            new LemmaSearch(),
            p
        );
        assertAll("test paged search results wrapper",
            () -> assertEquals(searchResults.getResults().size(), searchResults.getPage().getNumberOfElements()),
            () -> assertTrue(searchResults.getPage().isLast()),
            () -> assertTrue(searchResults.getPage().isFirst()),
            () -> assertNotNull(searchResults.getQuery())
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new SearchResultsWrapper<>(List.of(l, l), null, p)
        );
        p.setTotalPages(2);
        assertThrows(
            IllegalArgumentException.class,
            () -> new SearchResultsWrapper<>(List.of(l), null, p)
        );
        p.setTotalElements(0);
        assertThrows(
            IllegalArgumentException.class,
            () -> new SearchResultsWrapper<>(List.of(l), null, p)
        );
        p.setNumberOfElements(0);
        p.setSize(0);
        assertThrows(
            IllegalArgumentException.class,
            () -> new SearchResultsWrapper<>(Collections.emptyList(), null, p)
        );
    }

    @Test
    void searchResultsWrapperTest_pagination() throws Exception {
        PageInfo p = PageInfo.builder().numberOfElements(20).totalElements(80).totalPages(4).size(20).build();
        LinkedList<LemmaDto> hits = new LinkedList<>();
        for (int i=0; i<20; i++) {
            hits.add(LemmaDto.builder().id(String.format("%d", i)).eclass("BTSLemmaEntry").build());
        }
        SearchResultsWrapper<LemmaDto> dto = new SearchResultsWrapper<>(hits, new LemmaSearch(), p);
        assertNotNull(dto, "should be able to create search results wrapper if number of results is multiple of page size");
    }

    @Test
    void searchResultsDeserialize() throws Exception {
        SearchResultsWrapper<?> w = mapper.readValue(
            Util.loadFromFileAsString("lemma", "search.json"),
            SearchResultsWrapper.class
        );
        assertAll("check deserialization",
            () -> assertNotNull(w)
        );
    }

}