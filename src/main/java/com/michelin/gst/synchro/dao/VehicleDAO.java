
package com.michelin.gst.synchro.dao;

import com.michelin.gst.synchro.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleDAO extends JpaRepository<Vehicle, Long>{}
