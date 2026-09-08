package com.titleverify.titleverify_ai.repository;

import com.titleverify.titleverify_ai.entity.ProposedTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProposedTitleRepository extends JpaRepository<ProposedTitle, Long> {
}
