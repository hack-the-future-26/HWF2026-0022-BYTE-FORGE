package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CandidateRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(CandidateRetrievalService.class);

    private final RegisteredPublicationTitleRepository registeredTitleRepository;

    public CandidateRetrievalService(RegisteredPublicationTitleRepository registeredTitleRepository) {
        this.registeredTitleRepository = registeredTitleRepository;
    }

    public List<RegisteredPublicationTitle> retrieveCandidates(String normalizedTitle) {
        if (normalizedTitle == null || normalizedTitle.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String searchKeyword = normalizedTitle.trim();
            List<RegisteredPublicationTitle> initialMatches = registeredTitleRepository.findByNormalizedTitleContaining(searchKeyword);
            List<RegisteredPublicationTitle> candidates = initialMatches != null ? new ArrayList<>(initialMatches) : new ArrayList<>();

            if (candidates.isEmpty() && searchKeyword.contains(" ")) {
                String[] tokens = searchKeyword.split(" ");
                for (String token : tokens) {
                    if (token != null && token.length() >= 3) {
                        List<RegisteredPublicationTitle> tokenMatches = registeredTitleRepository.searchCandidatesByKeyword(token);
                        if (tokenMatches != null) {
                            for (RegisteredPublicationTitle match : tokenMatches) {
                                if (!candidates.contains(match)) {
                                    candidates.add(match);
                                }
                            }
                        }
                    }
                }
            }

            return candidates;
        } catch (Exception e) {
            logger.error("Database query failure while retrieving candidates for title '{}': {}", normalizedTitle, e.getMessage());
            return new ArrayList<>();
        }
    }
}
