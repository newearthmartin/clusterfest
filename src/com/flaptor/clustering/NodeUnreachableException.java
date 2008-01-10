package com.flaptor.clustering;

public class NodeUnreachableException extends Exception {

    private static final long serialVersionUID = -4021562241785282968L;

    public NodeUnreachableException() {
        super();
    }

    public NodeUnreachableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeUnreachableException(String message) {
        super(message);
    }

    public NodeUnreachableException(Throwable cause) {
        super(cause);
    }
    
}
