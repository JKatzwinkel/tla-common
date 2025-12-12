package tla.domain.dto.meta;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tla.domain.dto.AnnotationDto;
import tla.domain.dto.CommentDto;
import tla.domain.dto.CorpusObjectDto;
import tla.domain.dto.LemmaDto;
import tla.domain.dto.SentenceDto;
import tla.domain.dto.TextDto;
import tla.domain.dto.ThsEntryDto;
import tla.domain.model.ObjectReference;
import tla.domain.model.meta.AbstractBTSBaseClass;
import tla.domain.model.meta.Relatable;
import tla.domain.model.meta.Resolvable;

/**
 * TLA DTO base class.
 */
@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eclass"
)
@JsonSubTypes(
    {
        @Type(value = LemmaDto.class, name = "BTSLemmaEntry"),
        @Type(value = AnnotationDto.class, name = "BTSAnnotation"),
        @Type(value = TextDto.class, name = "BTSText"),
        @Type(value = ThsEntryDto.class, name = "BTSThsEntry"),
        @Type(value = CorpusObjectDto.class, name = "BTSTCObject"),
        @Type(value = CommentDto.class, name = "BTSComment"),
        @Type(value = SentenceDto.class, name = "BTSSentence")
    }
)
public abstract class AbstractDto extends AbstractBTSBaseClass implements Relatable<SortedSet<Resolvable>> {

    @NonNull
    private String id;

    /**
     * labeled references to other objects.
     */
    @Singular
    @JsonDeserialize(contentAs = ObjectReferences.class)
    private Map<String, SortedSet<Resolvable>> relations;

    /**
     * This no arguments constructor is required so that instances deserialized by jackson
     * contain initialized relations maps.
     */
    public AbstractDto() {
        this.relations = new LinkedHashMap<String, SortedSet<Resolvable>>();
    }

    @NoArgsConstructor
    @JsonDeserialize(contentAs = ObjectReference.class)
    public static class ObjectReferences extends TreeSet<Resolvable> {
        private static final long serialVersionUID = -11078168966585263L;

        public ObjectReferences(Collection<Resolvable> refs) {
            super(refs);
        }
    }

    /**
     * serialize object to JSON
     */
    public String toJson() {
        return tla.domain.util.IO.json(this);
    }

}
