package com.titleverify.titleverify_ai.service;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import com.titleverify.titleverify_ai.repository.RegisteredPublicationTitleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExactMatchService {

    private final RegisteredPublicationTitleRepository registeredTitleRepository;

    public ExactMatchService(RegisteredPublicationTitleRepository registeredTitleRepository) {
        this.registeredTitleRepository = registeredTitleRepository;
    }

    public Optional<RegisteredPublicationTitle> findExactMatch(String normalizedTitle) {
        if (normalizedTitle == null || normalizedTitle.trim().isEmpty()) {
            return Optional.empty();
        }
        return registeredTitleRepository.findByNormalizedTitle(normalizedTitle.trim());
    }
}
