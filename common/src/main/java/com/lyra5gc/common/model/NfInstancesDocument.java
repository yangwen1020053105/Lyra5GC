package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 3GPP HAL document used by Nnrf_NFManagement NFListRetrieval —
 * TS 29.510 Table 6.1.6.2.1-1 pattern ({ _links: { self, item[] } }).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NfInstancesDocument {

    @JsonProperty("_links")
    private Links links;

    public NfInstancesDocument() {
    }

    public NfInstancesDocument(String self, List<String> items) {
        this.links = new Links();
        this.links.self = new Link(self);
        if (items != null) {
            this.links.item = items.stream().map(Link::new).toList();
        }
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Links {
        private Link self;
        private List<Link> item;

        public Link getSelf() {
            return self;
        }

        public void setSelf(Link self) {
            this.self = self;
        }

        public List<Link> getItem() {
            return item;
        }

        public void setItem(List<Link> item) {
            this.item = item;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Link {
        private String href;

        public Link() {
        }

        public Link(String href) {
            this.href = href;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }
}
