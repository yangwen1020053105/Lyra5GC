package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * InvalidParam — TS 29.571 Table 5.4.5.2.2-1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvalidParam {

    private String param;
    private String reason;

    public InvalidParam() {
    }

    public InvalidParam(String param, String reason) {
        this.param = param;
        this.reason = reason;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
