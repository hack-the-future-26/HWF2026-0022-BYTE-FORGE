package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.dto.ApplicationRequestDto;
import com.titleverify.titleverify_ai.dto.ApplicationResponseDto;
import com.titleverify.titleverify_ai.service.PublicationApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationApiController {

    private final PublicationApplicationService applicationService;

    public ApplicationApiController(PublicationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<?> submitApplication(@RequestBody ApplicationRequestDto requestDto) {
        try {
            ApplicationResponseDto response = applicationService.saveApplication(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApplicationResponseDto(null, "ERROR", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApplicationResponseDto(null, "ERROR", "An unexpected error occurred while saving the application.", null));
        }
    }
}
