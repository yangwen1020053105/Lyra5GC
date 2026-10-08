package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * SearchResult — TS 29.510 Table 6.2.6.2.3-1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResult {

    private Integer validityPeriod;
    private List<NFProfile> nfInstances;
    @JsonProperty("numOfNfInstComplete")
    private Integer numOfNfInstComplete;

    public Integer getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(Integer validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public List<NFProfile> getNfInstances() {
        return nfInstances;
    }

    public void setNfInstances(List<NFProfile> nfInstances) {
        this.nfInstances = nfInstances;
    }

    public Integer getNumOfNfInstComplete() {
        return numOfNfInstComplete;
    }

    public void setNumOfNfInstComplete(Integer numOfNfInstComplete) {
        this.numOfNfInstComplete = numOfNfInstComplete;
    }
}
