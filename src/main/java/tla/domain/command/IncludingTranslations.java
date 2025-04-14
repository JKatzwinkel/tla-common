package tla.domain.command;

/**
 * Interface for search commands that allow specification
 * of {@code translation} criteria.
*/
public interface IncludingTranslations {

    /**
     * Overwrite this command's {@code translation} criteria.
     * @param translation the new specs.
    */
    void setTranslation(TranslationSpec translation);

    /**
     * Returns this command's {@code translation} criteria.
     * @return this command's translation criteria.
    */
    TranslationSpec getTranslation();

}
