package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * PlmnId — TS 29.571 Table 5.4.4.7-1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlmnId {

    private String mcc;
    private String mnc;

    public PlmnId() {
    }

    public PlmnId(String mcc, String mnc) {
        this.mcc = mcc;
        this.mnc = mnc;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlmnId other)) {
            return false;
        }
        return Objects.equals(mcc, other.mcc) && Objects.equals(mnc, other.mnc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mcc, mnc);
    }

    @Override
    public String toString() {
        return mcc + "-" + mnc;
    }
}
