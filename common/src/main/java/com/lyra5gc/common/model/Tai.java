package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Tai — TS 29.571 Table 5.4.4.9-1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tai {

    private PlmnId plmnId;
    private String tac;

    public Tai() {
    }

    public Tai(PlmnId plmnId, String tac) {
        this.plmnId = plmnId;
        this.tac = tac;
    }

    public PlmnId getPlmnId() {
        return plmnId;
    }

    public void setPlmnId(PlmnId plmnId) {
        this.plmnId = plmnId;
    }

    public String getTac() {
        return tac;
    }

    public void setTac(String tac) {
        this.tac = tac;
    }
}
