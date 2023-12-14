package org.agard.InventoryManagement.Exceptions;

public class ItemDeleteException extends RuntimeException{
    public ItemDeleteException() {
    }

    public ItemDeleteException(String message) {
        super(message);
    }

    public ItemDeleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemDeleteException(Throwable cause) {
        super(cause);
    }

    public ItemDeleteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
