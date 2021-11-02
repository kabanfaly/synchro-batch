package com.michelin.synchro.batch.repository;

import com.michelin.synchro.batch.model.Journey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JourneyRepository extends JpaRepository<Journey, Long>{}
