package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CandidateRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(CandidateRetrievalService.class);

    private final RegisteredPublicationTitleRepository registeredTitleRepository;
    private final Bm25SimilarityService bm25SimilarityService;

    public CandidateRetrievalService(RegisteredPublicationTitleRepository registeredTitleRepository) {
        this(registeredTitleRepository, null);
    }

    @Autowired
    public CandidateRetrievalService(RegisteredPublicationTitleRepository registeredTitleRepository,
                                     @Autowired(required = false) Bm25SimilarityService bm25SimilarityService) {
        this.registeredTitleRepository = registeredTitleRepository;
        this.bm25SimilarityService = bm25SimilarityService;
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

            if (bm25SimilarityService != null && candidates.size() > 1) {
                candidates = bm25SimilarityService.rankCandidates(normalizedTitle, candidates);
            }

            return candidates;
        } catch (Exception e) {
            logger.error("Database query failure while retrieving candidates for title '{}': {}", normalizedTitle, e.getMessage());
            return new ArrayList<>();
        }
    }
}
