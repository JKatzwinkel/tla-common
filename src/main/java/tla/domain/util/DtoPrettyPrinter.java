package tla.domain.util;

import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.core.util.Separators.Spacing;


public class DtoPrettyPrinter extends DefaultPrettyPrinter {

    private static final long serialVersionUID = -857602864138545423L;

    public static DefaultPrettyPrinter create(String indent) {
        DefaultPrettyPrinter pp = new DtoPrettyPrinter().withSeparators(
            new Separators().withObjectNameValueSpacing(
                Spacing.AFTER
            )
        );
        var indenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE.withIndent(indent);
        return pp.withArrayIndenter(indenter).withObjectIndenter(indenter);
    }

}
