package tla.error;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * Supposed to be thrown whenever retrieval of an identified resource fails.
 */
@Getter
@Setter
@Status(404)
@JsonPropertyOrder({"className", "message", "objectId", "eclass"})
public class ObjectNotFoundException extends TlaStatusCodeException {

    private static final long serialVersionUID = 3545121302433575193L;

    static final String MSG_TEMPLATE = "The %sobject with ID '%s' could not be found!";

    final private String objectId;
    final private String eclass;

    /**
     * Creates an instance with only the missing object's ID.
     * @param id the ID of the object that could not be found.
     */
    public ObjectNotFoundException(String id) {
        this(id, null);
    }

    /**
     * Creates an instance with both ID and {@code eClass} of the
     * missing object.
     * @param id the ID of the object
     * @param eclass the {@code eClass} of the object
     */
    public ObjectNotFoundException(String id, String eclass) {
        this.objectId = id;
        this.eclass = eclass;
    }

    @Override
    public String getMessage() {
        var eclassValue = "";
        if (this.eclass != null) {
            eclassValue = this.eclass + " ";
        }
        return String.format(
            MSG_TEMPLATE, eclassValue, this.objectId
        );
    }

}
