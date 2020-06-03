package tla.domain.dto.extern;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tla.domain.command.SearchCommand;
import tla.domain.dto.meta.AbstractDto;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResultsWrapper<T extends AbstractDto> {

    @JsonAlias("content")
    private List<T> results;

    private SearchCommand<? extends AbstractDto> query;

    private PageInfo page;

    public SearchResultsWrapper(List<T> hits, SearchCommand<? extends AbstractDto> query) {
        this.results = hits;
        this.query = query;
    }

    public SearchResultsWrapper(
        List<T> hits, SearchCommand<? extends AbstractDto> query, PageInfo page
    ) throws Exception {
        this(hits, query);
        this.page = page;
        if (page.getNumberOfElements() != this.results.size()) {
            throw new IllegalArgumentException(
                String.format(
                    "page info element count %s does not match actual element count %s",
                    page.getNumberOfElements(),
                    this.results.size()
                )
            );
        }
        if (page.getTotalElements() < page.getNumberOfElements()) {
            throw new IllegalArgumentException(
                String.format(
                    "total element count %s can't be less than number of elements on page: %s",
                    page.getTotalElements(),
                    page.getNumberOfElements()
                )
            );
        }
        if (page.getSize() < 1) {
            throw new IllegalArgumentException("page size can't be 0");
        }
        if (page.getTotalElements() > 0 && page.getTotalPages() != Math.floorDiv(page.getTotalElements(), page.getSize()) + 1) {
            throw new IllegalArgumentException(
                String.format(
                    "total page count can't be %s when page size is %s and total element count is %s",
                    page.getTotalPages(),
                    page.getSize(),
                    page.getTotalElements()
                )
            );
        }
    }


}