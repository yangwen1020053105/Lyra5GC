package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * NFServiceVersion — TS 29.510 Table 6.1.6.2.4-1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NFServiceVersion {

    private String apiVersionInUri;
    private String apiFullVersion;

    public NFServiceVersion() {
    }

    public NFServiceVersion(String apiVersionInUri, String apiFullVersion) {
        this.apiVersionInUri = apiVersionInUri;
        this.apiFullVersion = apiFullVersion;
    }

    public String getApiVersionInUri() {
        return apiVersionInUri;
    }

    public void setApiVersionInUri(String apiVersionInUri) {
        this.apiVersionInUri = apiVersionInUri;
    }

    public String getApiFullVersion() {
        return apiFullVersion;
    }

    public void setApiFullVersion(String apiFullVersion) {
        this.apiFullVersion = apiFullVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NFServiceVersion other)) {
            return false;
        }
        return Objects.equals(apiVersionInUri, other.apiVersionInUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiVersionInUri);
    }
}
