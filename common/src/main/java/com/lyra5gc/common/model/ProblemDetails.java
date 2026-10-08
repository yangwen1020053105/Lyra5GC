package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * ProblemDetails — RFC 7807 / TS 29.571 Table 5.4.5.2.1-1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetails {

    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
    private String cause;
    private List<InvalidParam> invalidParams;

    public ProblemDetails() {
    }

    public ProblemDetails(int status, String detail) {
        this.status = status;
        this.detail = detail;
    }

    public ProblemDetails(int status, String detail, String cause) {
        this.status = status;
        this.detail = detail;
        this.cause = cause;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public List<InvalidParam> getInvalidParams() {
        return invalidParams;
    }

    public void setInvalidParams(List<InvalidParam> invalidParams) {
        this.invalidParams = invalidParams;
    }
}
