package com.michelin.gst.synchro.dao;

import com.michelin.gst.synchro.entity.Journey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JourneyDAO extends JpaRepository<Journey, Long>{}
