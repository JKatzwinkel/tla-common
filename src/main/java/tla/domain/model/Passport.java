package tla.domain.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Passport {

    private static ObjectMapper mapper = new ObjectMapper();

    @JsonIgnore
    private String leafNodeValue = null;

    @JsonIgnore
    private Map<String, List<Passport>> properties = null;

    @JsonIgnore
    private Map<String, String> thsValueCache = null;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private ObjectReference thesaurusValue = null;


    public Passport() {
        super();
        properties = new HashMap<String, List<Passport>>();
    }


    public Passport(String value) {
        this.leafNodeValue = value;
    }


    /** Convert any object to a Passport instance via Jackson JSON round-trip.
     * Fails silently and returns empty null instead of throwing an exception.
     * Don't pass a JSON string.
     *
     * @param object Preferably a map or sth the jackson object mapper can serialize.
     * @return new passport instance or null
     */
    public static Passport of(Object object) {
        try {
            return mapper.readValue(
                mapper.writeValueAsString(object),
                Passport.class
            );
        } catch (Exception e) {
            log.warn(
                String.format(
                    "could not convert `%s` object to passport",
                    object.getClass()
                ),
                e
            );
            return null;
        }
    }


    /**
     * Determine whether this node has no contents at all.
     */
    public boolean isEmpty() {
        if (this.properties != null && this.properties.size() > 0) {
            return false;
        }
        return this.get() == null;
    }

    /**
     * Add a passport node at the specified key.
     */
    public void add(String key, Passport child) {
        if (this.properties == null) {
            this.properties = new HashMap<String, List<Passport>>();
        }
        if (this.leafNodeValue != null) {
            this.leafNodeValue = null;
        }
        if (this.properties.containsKey(key)) {
            this.properties.get(key).add(child);
        } else {
            this.properties.put(
                key,
                new LinkedList<>(List.of(child))
            );
        }
    }

    /**
     * Extracts a list of values from this passport object which can be reached by following
     * the specified selector path.
     *
     * Path segments are expected to be passed as individual strings. However, if only 1 single segment
     * gets provided, the method will try and split this string argument along the "<code>.</code>" delimiter
     * and use the result as the path segments list.
     */
    public List<Passport> extractProperty(String... path) {
        if (path.length == 1) {
            path = path[0].split("\\.");
        }
        List<Passport> recursionResults = new LinkedList<Passport>();
        if (path.length > 0) {
            String segment = path[0];
            if (this.properties != null && this.properties.containsKey(segment)) {
                for (Passport child : this.properties.get(segment)) {
                    if (child != null) {
                        recursionResults.addAll(
                            child.extractProperty(
                                Arrays.copyOfRange(path, 1, path.length)
                            )
                        );
                    }
                }
                return recursionResults;
            } else {
                log.debug("path segment not in passport: {}", segment);
            }
        } else {
            recursionResults.add(this);
        }
        return recursionResults;
    }

    /**
     * Extract list of selectors available for this passport object.
     *
     * Selector path segments delimiter is "<code>.</code>".
     */
    public List<String> extractPaths() {
        List<String> paths = new LinkedList<String>();
        if (this.properties != null) {
            if (this.properties.size() > 0) {
                for (Entry<String, List<Passport>> e : this.properties.entrySet()) {
                    String key = e.getKey();
                    Set<String> remainders = new HashSet<>();
                    boolean hasNonEmptyChildren = false;
                    if (e.getValue().size() > 0) {
                        for (Passport child : e.getValue()) {
                            hasNonEmptyChildren |= !child.isEmpty();
                            List<String> singleRecursionResults = child.extractPaths();
                            if (singleRecursionResults != null) {
                                remainders.addAll(singleRecursionResults);
                            }
                        }
                    }
                    if (remainders.size() > 0) {
                        for (String remainder : remainders) {
                            paths.add(key + "." + remainder);
                        }
                    } else if (hasNonEmptyChildren) {
                        paths.add(key);
                    }
                }
            } else {
                return null;
            }
        }
        return paths;
    }


    /**
     * Walks down tree and collects leaf node values.
     */
    public List<Passport> extractValues() {
        List<Passport> values = new LinkedList<Passport>();
        if (this.get() != null) {
            values.add(this);
        } else {
            for (Entry<String, List<Passport>> e : this.properties.entrySet()) {
                if (e.getValue() != null) {
                    for (Passport child : e.getValue()) {
                        if (child != null) {
                            values.addAll(child.extractValues());
                        }
                    }
                }
            }
        }
        return values;
    }


    /**
     * Extract object reference values, i.e. usually references to thesaurus entries.
     *
     * @return list of all object references found in this passport
     */
    public List<ObjectReference> extractObjectReferences() {
        List<ObjectReference> refs = this.extractValues().stream()
            .filter(leaf -> leaf.get() instanceof ObjectReference)
            .map(leaf -> (ObjectReference)leaf.get())
            .collect(Collectors.toList());
        return refs;
    }


    /**
     * Required for Jackson to be able to deserialize arbitrary passport fields.
     * @param key field name
     * @param children list of subnodes
     */
    @JsonAnySetter
    public void setProperty(String key, List<Passport> children) {
        this.properties.put(key, children);
    }


    /**
     * Helper method for handling values of ominous keys during deserialization.
     *
     * <p>Thing is, the passport data structure as optimized for ES indexing and publication
     * needs to be able to store references to thesaurus entries with the attributes
     * <code>id, eclass, type</code> and <code>name</code>. In case a passport node is in
     * fact a thesaurus reference, those are expected to have string values.
     * However, because passport nodes can be stored under any key, any of these attribute
     * names can also come with a list of passport child nodes assigned.</p>
     *
     * This helper method handles both cases.
     */
    @SuppressWarnings("unchecked")
    private void setPossibleThesaurusReferenceValue(String key, Object value) throws Exception {
        if (value instanceof String) {
            if (this.thsValueCache == null) {
                this.thsValueCache = new HashMap<>();
            }
            this.thsValueCache.put(key, (String) value);
        } else if (value instanceof List) {
            List<Object> objects = (List<Object>) value;
            List<Passport> children = objects.stream().map(
                Passport::of
            ).collect(
                Collectors.toList()
            );
            this.properties.put(key, children);
        }
        if (this.qualifiesAsThesaurusReference()) {
            this.thesaurusValue = this.toThsReference();
            this.thsValueCache = null;
        }
    }


    /**
     * Setter for virtual field <code>id</code> required for deserialization of thesaurus entry reference
     * values like eg:
     *
     * <pre>
     * {
     *   'key': [
     *     {
     *       'id': 'THSENTRYID',
     *       'eclass': 'BTSThsEntry',
     *       'type': 'date',
     *       'name': 'Ramses II'
     *     }
     *   ]
     * }
     * </pre>
     *
     * @param value either a string value or a list of passport child nodes
     */
    public void setId(Object value) throws Exception {
        setPossibleThesaurusReferenceValue("id", value);
    }

    /**
     * Setter for virtual field <code>eclass</code> required for deserialization of thesaurus entry reference
     */
    public void setEclass(Object value) throws Exception  {
        setPossibleThesaurusReferenceValue("eclass", value);
    }

    /**
     * Setter for virtual field <code>type</code> required for deserialization of thesaurus entry reference
     */
    public void setType(Object value) throws Exception  {
        setPossibleThesaurusReferenceValue("type", value);
    }

    /**
     * Setter for virtual field <code>name</code> required for deserialization of thesaurus entry reference
     */
    public void setName(Object value) throws Exception  {
        setPossibleThesaurusReferenceValue("name", value);
    }


    @Override
    public String toString() {
        if (this.leafNodeValue != null) {
            return this.leafNodeValue;
        } else {
            return this.getContents().toString();
        }
    }


    /** could you create a thesaurus entry object reference from this?
     *
     * TODO: this is obviously pretty dumb, fix this later pls
    */
    private boolean qualifiesAsThesaurusReference() {
        return this.thsValueCache != null && List.of(
            "id", "type", "eclass", "name"
        ).stream().allMatch(
            this.thsValueCache::containsKey
        );
    }


    /**
     * Returns an {@link ObjectReference} instance created from the internal thesaurus
     * reference value cache.
     */
    private ObjectReference toThsReference() {
        return new ObjectReference(
            this.thsValueCache.get("id"),
            this.thsValueCache.get("eclass"),
            this.thsValueCache.get("type"),
            this.thsValueCache.get("name"),
            null
        );
    }


    /**
     * Returns this passport node's payload.
     *
     * Passport node payload can come in 3 different shapes:
     *
     * <ul>
     *   <li>single string value</li>
     *   <li>generic passport node with arbitrary key/value pairs</li>
     *   <li>{@link ObjectReference} instance referring to a thesarus entry</li>
     * </ul>
     *
     * This methods checks what kind this node is and returns an object of the appropriate type.
     */
    public Object get() {
        if (this.leafNodeValue != null) {
            return this.leafNodeValue;
        } else if (this.thesaurusValue != null) {
            return this.thesaurusValue;
        }
        return null;
    }


    /**
     * Used in serialization.
     */
    @JsonValue
    public Object getContents() {
        Object value = this.get();
        return (value != null) ? value : this.properties;
    }

    /**
     * Returns number of subkeys.
     */
    public int size() {
        Object content = this.getContents();
        if (content instanceof Map) {
            return this.properties.size();
        } else {
            return content != null ? 1 : 0;
        }
    }

    /**
     * See if this node contains a certain key.
     * @param key
     * @return true if key is there
     */
    public boolean containsKey(String key) {
        if (this.getContents() instanceof Map) {
            return this.properties.containsKey(key);
        } else {
            return false;
        }
    }

    /**
     * Returns names of fields contained by this passport node.
     */
    public List<String> getFields() {
        if (this.getContents() instanceof Map) {
            return new LinkedList<>(this.properties.keySet());
        } else {
            return Collections.emptyList();
        }
    }

}
