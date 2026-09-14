package com.titleverify.titleverify_ai.repository;

import com.titleverify.titleverify_ai.entity.PublicationApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicationApplicationRepository extends JpaRepository<PublicationApplication, Long> {

    List<PublicationApplication> findAllByOrderByCreatedAtDesc();
}
