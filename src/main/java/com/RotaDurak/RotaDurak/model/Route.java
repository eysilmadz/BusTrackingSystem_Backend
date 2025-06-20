package com.RotaDurak.RotaDurak.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Route")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routeid")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "line", nullable = false, length = 100)
    private String line;

    @ManyToOne
    @JoinColumn(name = "cityid", nullable = false)
    private City city;

    @Column(name = "shapeid", length = 100)
    private String shape;

    @ManyToMany
    @JoinTable(
            name = "routestation",
            joinColumns = @JoinColumn(name = "routeid"),
            inverseJoinColumns = @JoinColumn(name = "stationid")
    )
    @JsonIgnore
    private Set<Station> stations = new HashSet<>();
}
