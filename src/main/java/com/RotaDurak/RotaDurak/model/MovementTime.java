package com.RotaDurak.RotaDurak.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movementtime")

public class MovementTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movementid")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "routeid", nullable = false)
    private Route route;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Column(nullable = false)
    private LocalTime time;
}
