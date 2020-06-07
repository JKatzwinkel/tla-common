package tla.domain.dto;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import tla.domain.dto.meta.NamedDocumentDto;
import tla.domain.model.Language;
import tla.domain.model.Paths;
import tla.domain.model.meta.BTSeClass;

/**
 * DTO Model for serial transfer of TLA thesaurus entry objects.
 */
@Data
@SuperBuilder
@BTSeClass("BTSThsEntry")
@EqualsAndHashCode(callSuper = true)
public class ThsEntryDto extends NamedDocumentDto {

    @JsonAlias("sortkey")
    private String sortKey;

    /**
     * object tree paths leading to this entry
     */
    private Paths paths;

    @Singular
    private SortedMap<Language, List<String>> translations;

    public ThsEntryDto() {
        this.translations = Collections.emptySortedMap();
    }

}