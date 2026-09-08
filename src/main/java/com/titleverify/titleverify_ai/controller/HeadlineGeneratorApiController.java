package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.config.SupportedLanguage;
import com.titleverify.titleverify_ai.dto.HeadlineGenerationRequestDto;
import com.titleverify.titleverify_ai.dto.HeadlineGenerationResponseDto;
import com.titleverify.titleverify_ai.service.HeadlineGeneratorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/headline-generator")
public class HeadlineGeneratorApiController {

    private final HeadlineGeneratorService headlineGeneratorService;

    public HeadlineGeneratorApiController(HeadlineGeneratorService headlineGeneratorService) {
        this.headlineGeneratorService = headlineGeneratorService;
    }

    @GetMapping("/languages")
    public ResponseEntity<List<SupportedLanguage>> getSupportedLanguages() {
        return ResponseEntity.ok(SupportedLanguage.getAllSupportedLanguages());
    }

    @PostMapping("/generate")
    public ResponseEntity<HeadlineGenerationResponseDto> generateHeadlines(@Valid @RequestBody HeadlineGenerationRequestDto requestDto) {
        try {
            HeadlineGenerationResponseDto response = headlineGeneratorService.generateAndVerifyHeadlines(requestDto);
            if ("ERROR".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new HeadlineGenerationResponseDto(null, null, null, "ERROR", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HeadlineGenerationResponseDto(null, null, null, "ERROR", "An unexpected error occurred during headline generation and verification."));
        }
    }
}
