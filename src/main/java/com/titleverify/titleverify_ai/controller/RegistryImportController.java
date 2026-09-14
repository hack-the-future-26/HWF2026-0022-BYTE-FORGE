package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.dto.RegistryImportResultDto;
import com.titleverify.titleverify_ai.service.RegistryImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Controller
public class RegistryImportController {

    private static final Logger logger = LoggerFactory.getLogger(RegistryImportController.class);

    private final RegistryImportService registryImportService;

    @Autowired
    public RegistryImportController(RegistryImportService registryImportService) {
        this.registryImportService = registryImportService;
    }

    @GetMapping("/registry/import")
    public String showImportPage() {
        return "registry_import";
    }

    @PostMapping("/registry/import")
    @ResponseBody
    public ResponseEntity<RegistryImportResultDto> importRegistryCsv(
            @RequestParam(value = "file", required = false) MultipartFile file) {

        if (file == null || file.isEmpty()) {
            logger.warn("Registry import request rejected: missing or empty file");
            RegistryImportResultDto errorResult = new RegistryImportResultDto(0, 0, 0, 0, 1,
                    List.of("Uploaded file is missing or empty"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        if (!isCsvFile(file)) {
            logger.warn("Registry import request rejected: unsupported or invalid file type for '{}'",
                    file.getOriginalFilename());
            RegistryImportResultDto errorResult = new RegistryImportResultDto(0, 0, 0, 0, 1,
                    List.of("Unsupported file type or invalid upload. Please upload a valid CSV file."));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        try {
            RegistryImportResultDto result = registryImportService.importCsv(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Registry import execution failed: {}", e.getMessage(), e);
            RegistryImportResultDto errorResult = new RegistryImportResultDto(0, 0, 0, 0, 1,
                    List.of("Unexpected server error during import: " + e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    private boolean isCsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        // 1. Explicit rejection of known binary/non-text media types
        if (contentType != null) {
            String ct = contentType.toLowerCase(Locale.ROOT);
            if (ct.startsWith("image/") || ct.startsWith("audio/") || ct.startsWith("video/")
                    || ct.equals("application/pdf") || ct.equals("application/zip")
                    || ct.equals("application/x-zip-compressed") || ct.equals("application/json")
                    || ct.equals("application/xml") || ct.equals("application/x-msdownload")) {
                return false;
            }
        }

        if (filename != null) {
            String lowerName = filename.toLowerCase(Locale.ROOT);
            if (lowerName.endsWith(".pdf") || lowerName.endsWith(".png") || lowerName.endsWith(".jpg")
                    || lowerName.endsWith(".jpeg") || lowerName.endsWith(".zip") || lowerName.endsWith(".exe")
                    || lowerName.endsWith(".json") || lowerName.endsWith(".xml")) {
                return false;
            }
        }

        boolean hasCsvExtension = filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".csv");
        boolean isCsvOrTextMime = contentType != null && (
                contentType.equalsIgnoreCase("text/csv")
                || contentType.equalsIgnoreCase("text/plain")
                || contentType.equalsIgnoreCase("application/vnd.ms-excel")
                || contentType.equalsIgnoreCase("application/csv")
                || contentType.equalsIgnoreCase("text/comma-separated-values")
        );

        // 2. Content inspection: check first line for valid text characters
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                return false;
            }
            if (firstLine.startsWith("\uFEFF")) {
                firstLine = firstLine.substring(1);
            }
            // Reject binary / null bytes
            for (int i = 0; i < Math.min(firstLine.length(), 200); i++) {
                char ch = firstLine.charAt(i);
                if (ch == 0 || (ch < 32 && ch != '\t' && ch != '\r' && ch != '\n')) {
                    return false;
                }
            }
            return hasCsvExtension || isCsvOrTextMime || firstLine.contains(",");
        } catch (Exception e) {
            return false;
        }
    }
}
