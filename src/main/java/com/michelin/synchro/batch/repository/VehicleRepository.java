
package com.michelin.synchro.batch.repository;

import com.michelin.synchro.batch.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>{}
