package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.dto.ApplicationAnalysisDetailsDto;
import com.titleverify.titleverify_ai.service.PublicationApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AnalyzerController {

    private final PublicationApplicationService applicationService;

    public AnalyzerController(PublicationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping({ "/", "/analyze" })
    public String showAnalyzerPage() {
        return "analyzer";
    }

    @GetMapping("/analysis/{applicationId}")
    public String showDetailedAnalysisPage(@PathVariable("applicationId") Long applicationId, Model model) {
        try {
            ApplicationAnalysisDetailsDto details = applicationService.getApplicationDetails(applicationId);
            model.addAttribute("analysisDetails", details);
            return "detailed_analysis";
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
