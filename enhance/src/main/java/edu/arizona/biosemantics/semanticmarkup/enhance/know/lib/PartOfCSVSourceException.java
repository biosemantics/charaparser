package edu.arizona.biosemantics.semanticmarkup.enhance.know.lib;

public class PartOfCSVSourceException extends Exception {

	protected PartOfCSVSourceException() {
    }

    /**
     * @param message the message
     */
    public PartOfCSVSourceException(String message) {
        super(message);
    }

    /**
     * @param message the message
     * @param cause the cause
     */
    public PartOfCSVSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause
     */
    public PartOfCSVSourceException(Throwable cause) {
        super(cause);
    }
}
