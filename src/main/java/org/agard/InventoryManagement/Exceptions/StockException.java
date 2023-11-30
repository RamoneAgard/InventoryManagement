package org.agard.InventoryManagement.Exceptions;

public class StockException extends RuntimeException{

    public StockException() {
    }

    public StockException(String message) {
        super(message);
    }

    public StockException(String message, Throwable cause) {
        super(message, cause);
    }

    public StockException(Throwable cause) {
        super(cause);
    }

    public StockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
