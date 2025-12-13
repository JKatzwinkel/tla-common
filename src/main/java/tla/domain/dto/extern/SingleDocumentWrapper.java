package tla.domain.dto.extern;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import tla.domain.dto.meta.AbstractDto;
import tla.domain.dto.meta.DocumentDto;

@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SingleDocumentWrapper<T extends AbstractDto> implements DocumentWrapper {

    private static ObjectMapper mapper = tla.domain.util.IO.getMapper();

    /**
     * The object being the main payload which this container is expected to deliver
     * at the very least.
     */
    private T doc;

    /**
     * Map containing objects related to the object constituting this container's
     * main payload, organized in a manner that allows for quick convenient access.
     */
    @Default
    private Map<String, Map<String, DocumentDto>> related = new HashMap<>();

    public SingleDocumentWrapper() {
        this.related = new HashMap<>();
    }

    /**
     * Initializes a wrapper around the given object.
     */
    public SingleDocumentWrapper(T doc) {
        this();
        this.doc = doc;
    }

    /**
     * Creates a new object details container by deserializing a JSON string.
     */
    @SuppressWarnings({"unchecked"})
    public static SingleDocumentWrapper<? extends AbstractDto> from(String src) throws Exception {
        return mapper.readValue(
            src,
            SingleDocumentWrapper.class
        );
    }

}
