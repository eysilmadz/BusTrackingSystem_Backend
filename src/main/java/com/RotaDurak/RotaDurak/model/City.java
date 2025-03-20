package com.RotaDurak.RotaDurak.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "City")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @OneToOne
    @JoinColumn(name = "id", nullable = true)
    private Station station;

    @OneToOne
    @JoinColumn(name = "id", nullable = true)
    private Route route;

    @OneToOne
    @JoinColumn(name = "id", nullable = true)
    private PopularPlace popularPlace;

    @OneToOne
    @JoinColumn(name = "id", nullable = true)
    private CardReloadPoint cardReloadPoint;
}
