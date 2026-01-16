package org.example.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AnalysisReport {
    private ShardInfo shardInfo;
    private List<SegmentInfo> segments;
}
