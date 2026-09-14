package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExactMatchService {

    private static final Logger logger = LoggerFactory.getLogger(ExactMatchService.class);

    private final RegisteredPublicationTitleRepository registeredTitleRepository;

    public ExactMatchService(RegisteredPublicationTitleRepository registeredTitleRepository) {
        this.registeredTitleRepository = registeredTitleRepository;
    }

    public Optional<RegisteredPublicationTitle> findExactMatch(String normalizedTitle) {
        if (normalizedTitle == null || normalizedTitle.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return registeredTitleRepository.findByNormalizedTitle(normalizedTitle.trim());
        } catch (Exception e) {
            logger.error("Database query failure while checking exact match for title '{}': {}", normalizedTitle, e.getMessage());
            return Optional.empty();
        }
    }
}
