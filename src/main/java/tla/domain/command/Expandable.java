package tla.domain.command;

/**
 * Can be used to identify search command adapters that might want to invoke query expansion,
 * meaning that they don't need to return search hits, but an IDs aggregation instead.
 */
public interface Expandable {

    /**
     * Sets this search command's expansion flag.
     * @param expand expansion flag value
     */
    void setExpand(boolean expand);

    /**
     * Returns this search command's expansion flag.
     * @return expansion flag
    */
    boolean isExpand();

    /**
     * Set the IDs of the ancestors within an object tree which this
     * search command shall specify.
     * @param ids ancestor IDs
     */
    void setRootIds(String[] ids);

    /**
     * Returns the ancestor IDs specified by this search command.
     * @return ancestor IDs specified
    */
    String[] getRootIds();

}
