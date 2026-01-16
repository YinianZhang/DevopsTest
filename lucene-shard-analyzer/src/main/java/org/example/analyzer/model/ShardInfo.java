package org.example.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ShardInfo {
    private String indexPath;
    private int totalSegments;
    private int totalDocs;
    private int totalDeletedDocs;
}
