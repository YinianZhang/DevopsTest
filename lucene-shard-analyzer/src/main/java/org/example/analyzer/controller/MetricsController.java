package org.example.analyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {
    @GetMapping(value = "/metrics", produces = "text/plain")
    public String metrics() {
        return "analyze_requests_total 1";
    }
}
