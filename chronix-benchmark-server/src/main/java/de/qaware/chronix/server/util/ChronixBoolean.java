package de.qaware.chronix.server.util;

/**
 * Created by mcqueen666 on 25.06.16.
 *
 * related to:
 * http://stackoverflow.com/questions/27617003/mapping-yes-no-to-boolean-in-rest-api-query-parameter
 */
public class ChronixBoolean {
    private static final ChronixBoolean FALSE = new ChronixBoolean(false);
    private static final ChronixBoolean TRUE = new ChronixBoolean(true);
    private boolean value;

    public ChronixBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    public static ChronixBoolean valueOf(String s) {
        switch (s.toLowerCase()) {
            case "true":
            case "yes":
            case "y": {
                return ChronixBoolean.TRUE;
            }
            default: {
                return ChronixBoolean.FALSE;
            }
        }
    }
}
