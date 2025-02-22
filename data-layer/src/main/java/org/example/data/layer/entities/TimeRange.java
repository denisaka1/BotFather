package org.example.data.layer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TimeRange {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String startTime; // e.g., "09:00"

    @Column(nullable = false)
    private String endTime; // e.g., "17:00"

    @ManyToOne
    @JoinColumn(name = "working_hours_id")
    @JsonIgnore
//    @ToString.Exclude
    private WorkingHours workingHours;
}
