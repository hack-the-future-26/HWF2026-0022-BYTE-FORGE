package com.titleverify.titleverify_ai.controller;

import com.titleverify.titleverify_ai.config.SupportedLanguage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HeadlineGeneratorController {

    @GetMapping("/headline-generator")
    public String showHeadlineGeneratorPage(Model model) {
        model.addAttribute("supportedLanguages", SupportedLanguage.getAllSupportedLanguages());
        return "headline_generator";
    }
}
