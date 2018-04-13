package org.foraci.mxf.mxfReader.exceptions;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception to encompass one or more messages when validating an MXF file's structure fails
 */
public class ValidationException extends RuntimeException
{
    private List<String> messages;

    public ValidationException(String message) {
        this(message, null);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.messages = new ArrayList<String>();
        addMessage(message);
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }
}
