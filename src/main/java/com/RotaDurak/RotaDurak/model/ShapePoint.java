package com.RotaDurak.RotaDurak.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shapepoint")
public class ShapePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shapepointid")
    private Long id;

    @Column(name = "shapeid", length = 100, nullable = false)
    private String shapeid;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lon", nullable = false)
    private Double lon;

    @Column(name = "disttraveled")
    private Double disttraveled;
}




