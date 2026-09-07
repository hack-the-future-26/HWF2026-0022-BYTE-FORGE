package com.titleverify.titleverify_ai.repository;

import com.titleverify.titleverify_ai.entity.RegisteredPublicationTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegisteredPublicationTitleRepository extends JpaRepository<RegisteredPublicationTitle, Long> {

    Optional<RegisteredPublicationTitle> findByNormalizedTitle(String normalizedTitle);

    List<RegisteredPublicationTitle> findByNormalizedTitleContaining(String normalizedTitleFragment);

    @Query("SELECT r FROM RegisteredPublicationTitle r WHERE LOWER(r.normalizedTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RegisteredPublicationTitle> searchCandidatesByKeyword(@Param("keyword") String keyword);
}
