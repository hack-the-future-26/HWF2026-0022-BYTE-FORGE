package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.dto.ApplicationAnalysisDetailsDto;
import com.titleverify.titleverify_ai.dto.TitleComparisonDto;
import com.titleverify.titleverify_ai.service.PublicationApplicationService;
import com.titleverify.titleverify_ai.service.VerificationReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AnalyzerController {

    private final PublicationApplicationService applicationService;
    private final VerificationReportService reportService;

    @Autowired
    public AnalyzerController(PublicationApplicationService applicationService, VerificationReportService reportService) {
        this.applicationService = applicationService;
        this.reportService = reportService;
    }

    public AnalyzerController(PublicationApplicationService applicationService) {
        this(applicationService, new VerificationReportService());
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

    @GetMapping("/history")
    public String showHistoryPage(Model model) {
        model.addAttribute("historyItems", applicationService.getHistorySummaries());
        return "history";
    }

    @GetMapping("/comparison/{applicationId}")
    public String showComparisonPage(@PathVariable("applicationId") Long applicationId, Model model) {
        try {
            TitleComparisonDto comparison = applicationService.getComparisonDetails(applicationId);
            model.addAttribute("comparisonDetails", comparison);
            return "title_comparison";
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/report/{applicationId}/download")
    public ResponseEntity<byte[]> downloadPdfReport(@PathVariable("applicationId") Long applicationId) {
        try {
            TitleComparisonDto comparison = applicationService.getComparisonDetails(applicationId);
            byte[] pdfBytes = reportService.generatePdfReport(comparison);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"TitleVerify_Report_APP-" + applicationId + ".pdf\"");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
