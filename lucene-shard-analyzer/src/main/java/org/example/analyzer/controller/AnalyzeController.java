package org.example.analyzer.controller;

import lombok.RequiredArgsConstructor;
import org.example.analyzer.model.AnalysisReport;
import org.example.analyzer.service.LuceneAnalysisService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AnalyzeController {

    private final LuceneAnalysisService luceneAnalysisService;

    @PostMapping("/analyze")
    public AnalysisReport analyze(@RequestParam("file") MultipartFile file) throws Exception {
        return luceneAnalysisService.analyzeShard(file);
    }
}
