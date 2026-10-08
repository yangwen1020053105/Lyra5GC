package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * PatchItem — TS 29.510 Table 6.1.6.2.10-1 (RFC 6902 JSON Patch).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchItem {

    private PatchOperation op;
    private String path;
    private JsonNode value;
    private String from;

    public PatchOperation getOp() {
        return op;
    }

    public void setOp(PatchOperation op) {
        this.op = op;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public JsonNode getValue() {
        return value;
    }

    public void setValue(JsonNode value) {
        this.value = value;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
