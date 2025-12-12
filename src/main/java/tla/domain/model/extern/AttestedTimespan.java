package tla.domain.model.extern;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tla.domain.model.ObjectReference;
import tla.domain.model.meta.Resolvable;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;

/**
 * Nested data structure for temporally grouped lemma attestation statistics.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AttestedTimespan {

    private Period period;

    @Builder.Default
    private AttestationStats attestations = new AttestationStats();

    @Builder.Default
    private List<AttestedTimespan> contains = List.of();

    public AttestedTimespan() {
        this.attestations = new AttestationStats();
        this.contains = List.of();
    }

    /**
     * Checks whether another instance's duration lies within the period covered by this
     * instance.
     *
     * @param timespan
     * @return
     */
    public boolean contains(AttestedTimespan timespan) {
        return this.period.contains(timespan.period);
    }

    /**
     * Container for document counts.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttestationStats {

        private long count;
        private long texts;
        private long sentences;
        private long objects;

        /**
         * Create an instance with all counts set to zero.
         */
        public AttestationStats() {
            this.count = 0;
            this.texts = 0;
            this.sentences = 0;
            this.objects = 0;
        }

        /**
         * Add another instance's stats to own.
         */
        public AttestationStats add(AttestationStats summand) {
            this.count += summand.count;
            this.texts += summand.texts;
            this.sentences += summand.sentences;
            this.objects += summand.objects;
            return this;
        }

    }

    /**
     * Time period identified by first and last year, and
     * a link to the corresponding thesaurus entry.
     *
     * Note: equality is determined based on first and last year alone, without
     * taking thesaurus entry object reference into account at all.
     *
     * Comparator consistent with equals.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Period implements Comparable<Period> {

        /** first year */
        @Setter
        private int begin;
        /** last year */
        @Setter
        private int end;
        /** Link to thesaurus entry */
        @JsonDeserialize(as = ObjectReference.class)
        private Resolvable ref;

        public Period(int begin, int end) {
            this.begin = begin;
            this.end = end;
            this.ref = null;
        }

        @Override
        public int compareTo(Period arg0) {
            if (this.end < arg0.end) {
                return -1;
            } else if (this.end > arg0.end) {
                return 1;
            }
            if (this.begin < arg0.begin) {
                return 1;
            } else if (this.begin > arg0.begin) {
                return -1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            return (o != null && ((Period) o).begin == this.begin)
                && (((Period) o).end == this.end);
        }

        /**
         * Returns true if another period lies within this one.
         */
        public boolean contains(Period period) {
            return (period.begin >= this.begin) && (period.end <= this.end);
        }
    }

}
