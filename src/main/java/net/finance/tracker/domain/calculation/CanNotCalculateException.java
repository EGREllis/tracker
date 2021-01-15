package net.finance.tracker.domain.calculation;

public class CanNotCalculateException extends Exception {
    public CanNotCalculateException() {
        super();
    }

    public CanNotCalculateException(String reason) {
        super(reason);
    }

    public CanNotCalculateException(Throwable throwable) {
        super(throwable);
    }

    public CanNotCalculateException(String reason, Throwable throwable) {
        super(reason, throwable);
    }

    public CanNotCalculateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
