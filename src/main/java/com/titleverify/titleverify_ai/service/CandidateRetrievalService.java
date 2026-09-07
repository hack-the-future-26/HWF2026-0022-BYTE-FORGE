package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CandidateRetrievalService {

    private final RegisteredPublicationTitleRepository registeredTitleRepository;

    public CandidateRetrievalService(RegisteredPublicationTitleRepository registeredTitleRepository) {
        this.registeredTitleRepository = registeredTitleRepository;
    }

    public List<RegisteredPublicationTitle> retrieveCandidates(String normalizedTitle) {
        if (normalizedTitle == null || normalizedTitle.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchKeyword = normalizedTitle.trim();
        List<RegisteredPublicationTitle> candidates = registeredTitleRepository.findByNormalizedTitleContaining(searchKeyword);

        if (candidates.isEmpty() && searchKeyword.contains(" ")) {
            String[] tokens = searchKeyword.split(" ");
            for (String token : tokens) {
                if (token.length() >= 3) {
                    List<RegisteredPublicationTitle> tokenMatches = registeredTitleRepository.searchCandidatesByKeyword(token);
                    for (RegisteredPublicationTitle match : tokenMatches) {
                        if (!candidates.contains(match)) {
                            candidates.add(match);
                        }
                    }
                }
            }
        }

        return candidates;
    }
}
