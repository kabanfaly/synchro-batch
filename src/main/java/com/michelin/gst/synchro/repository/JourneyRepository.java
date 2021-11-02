package com.michelin.gst.synchro.repository;

import com.michelin.gst.synchro.entity.Journey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JourneyRepository extends JpaRepository<Journey, Long>{}
