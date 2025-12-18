package com.RotaDurak.RotaDurak.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gpsdevice")
public class GPSDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deviceid")
    private Long id;

    @Column(name = "imei", nullable = false, unique = true)
    private String imei;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status = DeviceStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routeid")
    private Route route;

    @Column(name = "lastseenat")
    private LocalDateTime lastSeenAt;

    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
