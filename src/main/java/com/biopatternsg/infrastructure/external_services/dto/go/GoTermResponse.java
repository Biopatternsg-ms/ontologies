package com.biopatternsg.infrastructure.external_services.dto.go;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoTermResponse(
    @JsonProperty("numberOfHits") int numberOfHits,
    @JsonProperty("results") List<GoTerm> results,
    @JsonProperty("pageInfo") Object pageInfo
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoTerm(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("synonyms") List<Synonym> synonyms
    ) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Synonym(
            @JsonProperty("name") String name,
            @JsonProperty("type") String type
        ) {}
    }
}
