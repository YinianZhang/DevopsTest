package org.example.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SegmentInfo {
    private String name;
    private int docCount;
    private int deletedCount;
}
