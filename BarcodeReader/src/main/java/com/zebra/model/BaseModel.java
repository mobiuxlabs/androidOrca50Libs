package com.zebra.model;

import java.io.Serializable;

/**
 * Created by SUJEET KUMAR on 08-Mar-21.
 */
class BaseModel implements Serializable {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
