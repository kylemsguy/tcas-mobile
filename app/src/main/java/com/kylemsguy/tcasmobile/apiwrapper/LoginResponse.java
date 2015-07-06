package com.kylemsguy.tcasmobile.apiwrapper;

/**
 * Converts a raw login response into structured form.
 */
public final class LoginResponse {
    
    private final LoginStatus status;
    private final int userId;
    private final String usernameFormatted;
    private final String usernameCanonicalized;
    
    public static enum LoginStatus {
        /** Everything is fine. */
        OK,
        
        /** User provided incorrect credentials. */
        BAD_LOGIN,
        
        /** Server complained, but in a way client is unfamiliar with. */
        UNKNOWN_ERROR,
        
        /** Server returned gobbldygook. */
        UNRECOGNIZED_RESPONSE
    }
    
    public LoginResponse(String rawResponse) {
        Object[] response;
        try {
            Object gridResponse = Decoder.decode(rawResponse);
            response = getValuesFromV0Response(gridResponse);
        } catch (IllegalStateException e) {
            response = null;
        }
        
        LoginStatus status = LoginStatus.UNRECOGNIZED_RESPONSE;
        int userId = 0;
        String usernameFormatted = null;
        String usernameCanonicalized = null;
        
        if (response != null) {
            if (response.length >= 4 && "OK".equals(response[0])) {
                if (response[1] instanceof Integer &&
                    response[2] instanceof String &&
                    response[3] instanceof String) {
                    status = LoginStatus.OK;
                    userId = (Integer) response[1];
                    usernameFormatted = (String) response[2];
                    usernameCanonicalized = (String) response[3];
                } else {
                    status = LoginStatus.UNRECOGNIZED_RESPONSE;
                }
            } else if (response.length >= 2 && "ERR".equals(response[0])) {
                if ("BAD_LOGIN".equals(response[1])) {
                    status = LoginStatus.BAD_LOGIN;
                } else {
                    status = LoginStatus.UNKNOWN_ERROR;
                }
            }
        }
        
        this.status = status;
        this.userId = userId;
        this.usernameFormatted = usernameFormatted;
        this.usernameCanonicalized = usernameCanonicalized;
    }
    
    /** Login was successful. */
    public boolean isSuccess() {
        return status == LoginStatus.OK;
    }
    
    /** Status of the response. */
    public LoginStatus getStatus() {
        return status;
    }
    
    /** TwoCans internal User ID# */
    public int getUserId() {
        return userId;
    }
    
    /** Formatted string of the username. */
    public String getUsernameFormatted() {
        return usernameFormatted;
    }
    
    /** Canonicalized form of the name. Alphanumerics only. */
    public String getUsernameCanonicalized() {
        return usernameCanonicalized;
    }
    
    private Object[] getValuesFromV0Response(Object response) {
        if (response instanceof Object[][]) {
            Object[][] table = (Object[][]) response;
            if (table.length == 1) {
                return table[0];
            }
        }
        return null;
    }
}
