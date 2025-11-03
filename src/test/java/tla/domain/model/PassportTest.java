package tla.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tla.domain.util.IO.json;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tla.domain.Util;
import tools.jackson.databind.ObjectMapper;


class PassportTest {

    static final String RESOURCE_PATH = "src/test/resources/sample/passport/";

    private ObjectMapper mapper;

    @BeforeEach
    public void init() {
        mapper = new ObjectMapper();
    }

    public Passport loadFromFile(String filename) throws Exception {
        return mapper.readValue(
            new File(
                String.format(RESOURCE_PATH + "%s", filename)
            ),
            Passport.class
        );
    }

    @Test
    void addProperties() {
        Passport p = new Passport();
        p.add("key", new Passport("val1"));
        p.add("key", new Passport("val2"));
        assertEquals(1, p.getProperties().size(), "number of keys in passport should be 1");
        List<Passport> values = p.getProperties().get("key");
        assertEquals(2, values.size(), "number of values provided for passport key 'key' should be 2");
        assertAll("passport node under key 'key' should contain both 'val1' and 'val2'",
            () -> assertEquals("val1", values.get(0).toString()),
            () -> assertEquals("val2", values.get(1).toString())
        );
    }

    /**
     * tests whether the {@link Passport#add} method works on Passport nodes that have
     * been explicitly instantiated as a value-holding leaf, i.e. using the {@link Passport#Passport(String)}
     * constructor.
     */
    @Test
    void addProperties_refitNodeType() {
        Passport p = new Passport("val");
        p.add("key", new Passport("val1"));
        p.add("key", new Passport("val2"));
        assertEquals(1, p.getProperties().size(), "number of keys in passport should be 1");
        List<Passport> values = p.getProperties().get("key");
        assertEquals(2, values.size(), "number of values provided for passport key 'key' should be 2");
        assertAll("passport node under key 'key' should contain both 'val1' and 'val2'",
            () -> assertEquals("val1", values.get(0).toString()),
            () -> assertEquals("val2", values.get(1).toString())
        );
    }

    @Test
    void deserializeFromString_emptyLeafList() throws Exception {
        Passport pp = mapper.readValue(
            "{\"key\": [{\"subkey\": []}]}",
            Passport.class
        );
        assertEquals(pp.getProperties().size(), 1, "passport size should be 1");
        assertTrue(pp.getProperties().containsKey("key"), "passport should contain key 'key'");

        List<Passport> children = pp.getProperties().get("key");
        assertEquals(1, children.size(), "number of child nodes should be 1");
        Passport spp = children.get(0);
        assertEquals(1, spp.getProperties().size(), "subkey passport size should be 1");
        assertTrue(spp instanceof Passport);
    }

    @Test
    void deserializeFromString_emptyLeaf() throws Exception {
        Passport pp = mapper.readValue(
            "{\"key\": [{\"subkey\": [{}]}]}",
            Passport.class
        );
        Passport subkeyPassport = pp.getProperties().get("key").get(0);
        Passport leafValuePassport = subkeyPassport.getProperties().get("subkey").get(0);
        assertTrue(leafValuePassport instanceof Passport);
        assertEquals(0, leafValuePassport.getProperties().size(), "leaf node should be empty");
    }

    @Test
    void deserializeFromString_stringLeaf() throws Exception {
        Passport pp = mapper.readValue(
            "{\"key\": [\"val\"]}",
            Passport.class
        );
        assertAll("deserialized passport instance should contain non-null properties map and nothing else",
            () -> assertTrue(pp.getProperties() != null, "properties map is not to be null"),
            () -> assertEquals(null, pp.getLeafNodeValue(), "no leaf node value should be set")
            //() -> assertEquals(null, pp.getId(), "no thesaurus reference should be set")
        );
        assertEquals("val", pp.getProperties().get("key").get(0).toString(), "first value should be 'val'");
    }

    @Test
    void deserializeFromString_reference_level0() throws Exception {
        Passport pp = mapper.readValue(
            "{\"id\": \"ID\", \"eclass\": \"BTSThsEntry\", \"type\": \"date\"}",
            Passport.class
        );
        assertTrue(pp != null, "deserialized thesaurus reference passport leaf should not be null");
    }

    @Test
    void deserializeFromString_equals() throws Exception {
        Passport p1 = mapper.readValue(
            "{\"a\": [{\"b\": [\"c\"], \"d\": [{\"id\":\"1\",\"eclass\":\"c\",\"type\":\"t\",\"name\":\"n\"}]}]}",
            Passport.class
        );
        Passport p2 = new Passport();
        Passport p2b = new Passport();
        p2b.add("b", new Passport("c"));
        p2b.add("d", Passport.of(
            ObjectReference.builder().id("1").eclass("c").type("t").name("n").build()
        ));
        p2.add("a", p2b);
        assertAll("two passports should be equal",
            () -> assertEquals(p1, p2, "passport objects should be equivalents"),
            () -> assertEquals(p1.hashCode(), p2.hashCode(), "hashcodes should match"),
            () -> assertEquals(p1.toString(), p2.toString(), "toString should match")
        );
    }

    @Test
    void extractPaths_level0() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": []}",
            Passport.class
        );
        List<String> paths = p.extractPaths();
        assertAll("selector extraction should yield empty result",
            () -> assertEquals(0, paths.size(), "no selectors should be extracted from passport (no values)"),
            () -> assertEquals("", String.join(", ", paths), "there should be no selectors")
        );
    }

    @Test
    void extractPaths_level1() {
        Passport pp = new Passport();
        Passport leaf = new Passport("val");
        pp.add("key", leaf);
        List<String> paths = pp.extractPaths();
        assertEquals(1, paths.size(), "number of property paths extracted should be 1");
        assertEquals("key", paths.get(0), "extracted selector path should be 'key'");
    }

    @Test
    void extractPaths_level1_multipleValues() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [\"b\", \"c\", \"d\"]}",
            Passport.class
        );
        List<String> paths = p.extractPaths();
        assertEquals(1, paths.size(), "number of property selectors extracted should be 1");
        assertEquals("a", paths.get(0), "only extractable selector should be 'a'");
    }

    @Test
    void extractPaths_level2_basic() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"b\": [\"c\"]}]}",
            Passport.class
        );
        List<String> paths = p.extractPaths();
        assertEquals(1, paths.size(), "number of property selectors extracted should be 1");
        assertEquals("a.b", paths.get(0), "only extractable selector should be 'a.b'");
    }

    @Test
    void extractPaths_level2_emptyValues() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"b\": [{}], \"c\": [], \"d\": [\"e\"]}]}",
            Passport.class
        );
        List<String> paths = p.extractPaths();
        assertAll("should only extract selector 'a.d'",
            () -> assertEquals(1, paths.size(), "exactly 1 selector should be extractable"),
            () -> assertEquals("a.d", String.join(", ", paths), "only extractable selector should be 'a.d'")
        );
    }

    @Test
    void extractPaths_level2() throws Exception {
        Passport pp = mapper.readValue(
            "{\"key\": [{\"subkey\": [\"val\"]}]}",
            Passport.class
        );
        List<String> paths = pp.extractPaths();
        assertEquals(1, paths.size(), "number of property paths extracted should be 1");
        assertEquals("key.subkey", paths.get(0), "extracted selector path should be 'key.subkey'");
    }

    @Test
    void extractPaths_level3() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"l\": [{\"m\": [{\"n\": [\"o\"], \"p\": [{}], \"q\": []}], \"h\": [{\"i\": [\"j\", \"k\", \"l\", \"m\", \"n\"]}]}, {\"b\": [{\"c\": [\"d\"], \"e\": [{\"f\": [\"g\"]}]}]}]}]}",
            Passport.class
        );
        List<String> paths = p.extractPaths();
        Collections.sort(paths);
        assertAll("extracted selectors should be correct",
            () -> assertEquals(4, paths.size(), "number of selector paths should be 4"),
            () -> assertTrue(!paths.contains("a.l.m.p"), "selector 'a.l.m.p' should not be extracted (empty value)"),
            () -> assertTrue(!paths.contains("a.l.m.q"), "selector 'a.l.m.q' should not be extracted (no values)"),
            () -> assertEquals("a.l.b.c", paths.get(0), "first path should be albc"),
            () -> assertEquals("a.l.b.e.f", paths.get(1), "second path should be albef"),
            () -> assertEquals("a.l.h.i", paths.get(2), "third path should be alhi"),
            () -> assertEquals("a.l.m.n", paths.get(3), "fourth path should be almn")
        );
    }

    @Test
    void extractProperty_singleValue_level1() throws Exception {
        Passport pp = new Passport();
        Passport leaf = new Passport("val");
        pp.add("key", leaf);
        List<Passport> values = pp.extractProperty("key");
        assertTrue(values.contains(leaf), "extracted leaf values for key 'key' should contain " + leaf);
    }

    @Test
    void extractProperty_twoValues_level2() throws Exception {
        Passport pp = mapper.readValue(
            "{\"key1\": [{\"key2\": [\"val1\", \"val2\"]}]}",
            Passport.class
        );
        List<Passport> values = pp.extractProperty("key1", "key2");
        assertAll("should be able to extract 2 values",
            () -> assertEquals(2, values.size(), "extracted value list should contain 2 elements"),
            () -> assertEquals("val1, val2", values.stream().map(Passport::toString).collect(Collectors.joining(", ")), "both values should have been extracted"),
            () -> assertEquals(values, pp.extractValues(), "extractValues() should return those those exact 2 values.")
        );
    }

    @Test
    void extractProperty_internalNode() throws Exception {
        Passport pp = mapper.readValue(
            "{\"a\": [{\"b\": [\"1\"], \"c\": [\"2\"]}]}",
            Passport.class
        );
        List<Passport> nodes = pp.extractProperty("a");
        assertAll("should extract internal node",
            () -> assertEquals(1, nodes.size(), "expect 1 inner node"),
            () -> assertEquals(2, nodes.get(0).size(), "expect 2 keys in inner node"),
            () -> assertTrue(nodes.get(0).containsKey("b"), "inner node contains key 'b'"),
            () -> assertTrue(nodes.get(0).containsKey("c"), "inner node contains key 'c'")
        );
    }

    @Test
    void extractProperty_reference_level3() throws Exception {
        Passport pp = mapper.readValue(
            "{\"date\": [{\"date\": [{\"date\": [{\"eclass\": \"BTSThsEntry\", \"type\": \"date\", \"id\": \"FCJURX24JZGXZEKP3TW36U3ZFA\", \"name\": \"Ramses II. Usermaatre-Setepenre\"}]}]}]}",
            Passport.class
        );
        List<Passport> leafs = pp.extractProperty("date", "date", "date");
        assertEquals(1, leafs.size(), "number of leaf nodes under selector should be 1");
        Passport leaf = leafs.get(0);
        assertEquals(
            "FCJURX24JZGXZEKP3TW36U3ZFA",
            ((ObjectReference)leaf.get()).getId(),
            "ID value of only ref leaf under selector should be 'FCJURX24JZGXZEKP3TW36U3ZFA'"
        );
        assertAll("number of thesaurus references found amongst leafes should be 1",
            () -> assertEquals(1, pp.extractObjectReferences().size(), "exactly 1 thesaurus references should be extractable from entire passport"),
            () -> assertEquals(leaf.get(), pp.extractObjectReferences().get(0), "only thesaurus reference found should be the one under selector 'date.date.date'")
        );
    }

    @Test
    void extractProperty_reference_ominous_key() throws Exception {
        Passport pp = mapper.readValue(
            "{\"type\":[{\"id\":\"1\",\"eclass\":\"c\",\"type\":\"t\",\"name\":\"n\"}]}",
            Passport.class
        );
        assertAll("ominous key value should be deserialized",
            () -> assertEquals(1, pp.size(), "node size should be 1"),
            () -> assertTrue(pp.containsKey("type"), "key `type` expected")
        );
        List<Passport> leafs = pp.extractProperty("type");
        assertAll("thesaurus reference leaf node expected",
            () -> assertEquals(1, leafs.size(), "exactly 1 leaf node expected"),
            () -> assertTrue(leafs.get(0).get() instanceof ObjectReference, "leaf is thesaurus ref"),
            () -> assertEquals("1", ((ObjectReference) leafs.get(0).get()).getId(), "check ths ref ID")
        );
    }

    @Test
    void extractLeafValues_nullNode() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"b\":[\"c\"], \"d\": null}]}",
            Passport.class
        );
        assertDoesNotThrow(
            () -> { p.extractValues(); },
            "entire tree traversed despite of internal null node lurking inside"
        );
    }

    @Test
    void extractProperty_nullNode() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"b\":[\"c\"], \"d\": null}]}",
            Passport.class
        );
        assertDoesNotThrow(
            () -> { p.extractProperty("a.b.c"); },
            "lurking internal null node doesn't compromise path lookup"
        );
    }

    @Test
    void extractProperty_invalidSelector() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"b\": [{\"c\": [\"d\"]}]}]}",
            Passport.class
        );
        List<Passport> nodes = p.extractProperty("a.b.f");
        assertEquals(0, nodes.size(), "expect no results");
    }

    @Test
    void extractProperty_validSelector() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"e\": [{\"c\": [\"f\"]}], \"b\": [{\"c\": [\"d\"]}]}]}",
            Passport.class
        );
        assertDoesNotThrow(
            () -> { p.extractProperty("a.e.c"); },
            "dead end in legal selector lookup should not throw exception"
        );
        List<Passport> values = p.extractProperty("a.e.c");
        assertAll("single value 'f' should be extracted at given path",
            () -> assertEquals(1, values.size(), "number of extracted values should be 1"),
            () -> assertEquals("f", values.get(0).toString(), "extracted value should be 'f'")
        );
        List<Passport> leafs = p.extractValues();
        assertAll("2 leaf values should be extractable from passport in total",
            () -> assertEquals(2, leafs.size(), "extractable leaf count should be 2"),
            () -> assertTrue(List.of("d", "f").contains(leafs.get(0).get()), "value 'f' should be among extracted values"),
            () -> assertTrue(List.of("d", "f").contains(leafs.get(1).get()), "value 'd' should be among extracted values")
        );
    }

    @Test
    void extractProperty_shortSelector() throws Exception {
        Passport p = mapper.readValue(
            "{\"a\": [{\"e\": [{\"c\": [\"f\"]}], \"b\": [{\"c\": [\"d\"]}]}]}",
            Passport.class
        );
        List<Passport> leafs = p.extractProperty("a.e");
        assertEquals(1, leafs.size(), "number of selected nodes should be 1");
        assertEquals(
            Map.of("c", List.of("f")).toString(),
            leafs.get(0).toString(),
            "extracted value list should contain map object"
        );
    }


    @Test
    void serialize_singleValue() throws Exception {
        Passport p = new Passport();
        p.add("key", new Passport("val"));
        String out = json(p);
        assertEquals("{\"key\":[\"val\"]}", out, "serialized passport should only contain key and value");
    }

    @Test
    void serialize_singleThsRef() throws Exception {
        Passport p = new Passport();
        Passport q = new Passport();
        q.setId("THSID");
        q.setEclass("BTSThsEntry");
        q.setType("place");
        q.setName("memphis");
        p.add("key", q);
        String out = json(p);
        assertEquals(
            "{\"key\":[{\"eclass\":\"BTSThsEntry\",\"id\":\"THSID\",\"name\":\"memphis\",\"type\":\"place\"}]}",
            out,
            "serialized passport should terminate in single thesaurus reference"
        );
    }

    @Test
    void serialize_complexFromFile() throws Exception {
        Passport p = loadFromFile("2BJVOCJBVVFEZHGYWNX4J5VYGI.json");
        String in = Util.loadFromFileAsString("passport", "2BJVOCJBVVFEZHGYWNX4J5VYGI.json");
        String out = tla.domain.util.IO.json(p, "  ");
        assertAll("input and output should be the same",
            () -> assertEquals(in.length(), out.length(), "input and output string length should be same"),
            () -> assertEquals(in.charAt(in.length() - 1), out.charAt(out.length() - 1), "last character should match"),
            () -> assertEquals(in, out, "serialized passport should be same as input source")
        );
    }

    @Test
    void emptyPassport() {
        Passport p = new Passport();
        assertAll("test assumptions about empty passport",
            () -> assertTrue(p.isEmpty(), "isEmpty should return true"),
            () -> assertEquals(0, p.size(), "size should be 0"),
            () -> assertNull(p.get(), "leaf value should be null"),
            () -> assertTrue(p.getFields().isEmpty(), "expect no fields")
        );
    }

    @Test
    void passportOfString() {
        Passport p = Passport.of("{\"key\":[\"value\"]}");
        assertAll("see if passport could be created from JSON string (assume no)",
            () -> assertNotNull(p, "factory method should return instance"),
            () -> assertTrue(!p.isEmpty(), "instance should not be empty"),
            () -> assertEquals(1, p.size(), "size should be 1"),
            () -> assertTrue(!p.containsKey("key"), "no key key should be present as this is a leaf node"),
            () -> assertEquals(0, p.getFields().size(), "expect no fields"),
            () -> assertEquals("{\"key\":[\"value\"]}", p.get(), "should be just leaf node")
        );
    }
}
