package com.ld.myfastjson;

public class JSONException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JSONException(){
        super();
    }
    
    public JSONException(Throwable cause) {
    	super(cause);
    }

    public JSONException(String message){
        super(message);
    }

    public JSONException(String message, Throwable cause){
        super(message, cause);
    }
}
