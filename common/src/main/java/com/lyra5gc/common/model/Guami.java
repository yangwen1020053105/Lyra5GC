package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Guami — TS 29.571 Table 5.4.4.49-1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Guami {

    private PlmnId plmnId;
    private String amfId;

    public Guami() {
    }

    public Guami(PlmnId plmnId, String amfId) {
        this.plmnId = plmnId;
        this.amfId = amfId;
    }

    public PlmnId getPlmnId() {
        return plmnId;
    }

    public void setPlmnId(PlmnId plmnId) {
        this.plmnId = plmnId;
    }

    public String getAmfId() {
        return amfId;
    }

    public void setAmfId(String amfId) {
        this.amfId = amfId;
    }
}
