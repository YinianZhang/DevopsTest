package org.example.analyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@RestController
public class InfoController {

    @GetMapping("/info")
    public Map<String, Object> info() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("version", "0.1.0");
        map.put("git_sha", System.getenv().getOrDefault("GIT_SHA", "unknown"));
        map.put("arch", System.getProperty("os.arch"));
        map.put("hostname", InetAddress.getLocalHost().getHostName());

        return map;
    }
}
