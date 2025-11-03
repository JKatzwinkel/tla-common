package tla.domain.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Utility class allowing for eg. deserializing a modeled object from file.
 */
@Slf4j
public class IO {

    private static ObjectMapper mapper = JsonMapper.builder().enable(
        MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
    ).disable(
        MapperFeature.SORT_CREATOR_PROPERTIES_FIRST
    ).build();

    /**
     * Deserialize a JSON file into an object of the specified type.
     * Returns <code>null</code> if anything goes wrong.
     */
    public static <T> T loadFromFile(String filename, Class<T> clazz) throws Exception {
        String contents = Files.readString(
            new File(
                filename
            ).toPath(),
            StandardCharsets.UTF_8
        );
        return mapper.readValue(
            contents,
            clazz
        );
    }

    /**
     * Return JSON serialization of object.
     *
     * @param object any object
     * @return JSON-formatted string or <code>null</code>
     */
    public static String json(Object object) {
        return json(object, null);
    }

    public static String json(Object object, String indent) {
        if (indent == null) {
            return mapper.writeValueAsString(object);
        }
        return mapper.writer().with(
            DtoPrettyPrinter.create(indent)
        ).with(
            SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS
        ).writeValueAsString(object);
    }

    /** get a jackson {@link ObjectMapper} instance*/
    public static ObjectMapper getMapper() {
        return mapper;
    }

}
