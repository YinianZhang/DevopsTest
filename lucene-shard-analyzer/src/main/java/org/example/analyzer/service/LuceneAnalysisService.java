package org.example.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.example.analyzer.model.AnalysisReport;
import org.example.analyzer.model.SegmentInfo;
import org.example.analyzer.model.ShardInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class LuceneAnalysisService {

    public AnalysisReport analyzeShard(MultipartFile file) throws Exception {
        Path tempDir = Files.createTempDirectory("shard");
        Path uploadFile = tempDir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), uploadFile);

        extract(uploadFile, tempDir);

        Path indexPath = findLuceneIndex(tempDir);

        List<SegmentInfo> segments = readSegments(indexPath);

        int totalDocs = segments.stream().mapToInt(SegmentInfo::getDocCount).sum();
        int totalDeleted = segments.stream().mapToInt(SegmentInfo::getDeletedCount).sum();

        ShardInfo shardInfo = new ShardInfo(
                indexPath.toString(),
                segments.size(),
                totalDocs,
                totalDeleted
        );

        return new AnalysisReport(shardInfo, segments);
    }

    private void extract(Path file, Path dest) throws Exception {
        String name = file.toString();

        if (name.endsWith(".zip")) {
            unzip(file, dest);
        } else if (name.endsWith(".tar") || name.endsWith(".tar.gz")) {
            untar(file, dest);
        } else {
            throw new IllegalArgumentException("Only zip/tar allowed");
        }
    }

    private void unzip(Path file, Path dest) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(file))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path out = dest.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.createDirectories(out.getParent());
                    Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void untar(Path file, Path dest) throws Exception {
        try (InputStream fi = Files.newInputStream(file);
             InputStream bi = new BufferedInputStream(fi);
             InputStream gzi = file.toString().endsWith(".gz") ? new GzipCompressorInputStream(bi) : bi;
             TarArchiveInputStream tis = new TarArchiveInputStream(gzi)) {

            org.apache.commons.compress.archivers.tar.TarArchiveEntry entry;
            while ((entry = tis.getNextTarEntry()) != null) {
                Path out = dest.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.createDirectories(out.getParent());
                    IOUtils.copy(tis, Files.newOutputStream(out));
                }
            }
        }
    }

    private Path findLuceneIndex(Path root) throws IOException {
        return Files.walk(root)
                .filter(p -> p.getFileName().toString().startsWith("segments_"))
                .findFirst()
                .map(Path::getParent)
                .orElseThrow(() -> new RuntimeException("No Lucene segments found"));
    }

    private List<SegmentInfo> readSegments(Path indexPath) throws IOException {
        try (Directory dir = FSDirectory.open(indexPath)) {
            SegmentInfos infos = SegmentInfos.readLatestCommit(dir);

            List<SegmentInfo> list = new ArrayList<>();

            for (int i = 0; i < infos.size(); i++) {
                SegmentCommitInfo sci = infos.info(i);

                // 获取文档数
                int maxDoc = sci.info.maxDoc();      // 总文档数（包括删除的）
                int delCount = sci.getDelCount();    // 删除的文档数
                int liveDocs = maxDoc - delCount;    // 有效文档数

                list.add(new SegmentInfo(sci.info.name, liveDocs, delCount));
                // 或者如果你想要总文档数：
                // list.add(new SegmentInfo(sci.info.name, maxDoc, delCount));
            }

            return list;
        }
    }
}
