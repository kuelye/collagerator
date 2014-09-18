package com.kuelye.components.async;

public class MessageAndErrorCodeProgress {

    public static final String  EMPTY_MESSAGE = "";
    public static final int     NO_ERROR_CODE = -1;

    private final String    mMessage;
    private final int       mErrorCode;

    public MessageAndErrorCodeProgress(String message, int errorCode) {
        mMessage = message;
        mErrorCode = errorCode;
    }

    public MessageAndErrorCodeProgress(String message) {
        this(message, NO_ERROR_CODE);
    }

    public MessageAndErrorCodeProgress(int errorCode) {
        this(EMPTY_MESSAGE, errorCode);
    }

    public String getMessage() {
        return mMessage;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public boolean isEmpty() {
        return mMessage == EMPTY_MESSAGE;
    }

    public boolean isError() {
        return mErrorCode != NO_ERROR_CODE;
    }

}
