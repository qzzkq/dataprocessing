package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record Page(String message, List<String> successors) {
    Page {
        if (message == null) {
            throw new IllegalArgumentException("field 'message' is missing");
        }
        successors = successors == null ? List.of() : List.copyOf(successors);
    }
}
