package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Snssai — TS 29.571 Table 5.4.4.2-1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Snssai {

    private Integer sst;
    private String sd;

    public Snssai() {
    }

    public Snssai(Integer sst, String sd) {
        this.sst = sst;
        this.sd = sd;
    }

    public Integer getSst() {
        return sst;
    }

    public void setSst(Integer sst) {
        this.sst = sst;
    }

    public String getSd() {
        return sd;
    }

    public void setSd(String sd) {
        this.sd = sd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Snssai other)) {
            return false;
        }
        return Objects.equals(sst, other.sst) && Objects.equals(sd, other.sd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sst, sd);
    }

    @Override
    public String toString() {
        return "{sst:" + sst + ", sd:" + sd + "}";
    }
}
