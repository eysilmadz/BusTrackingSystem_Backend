package com.RotaDurak.RotaDurak.repository;
import com.RotaDurak.RotaDurak.model.GPSDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GpsDeviceRepository extends JpaRepository<GPSDevice, Long> {
    Optional<GPSDevice> findByImei(String imei);
}