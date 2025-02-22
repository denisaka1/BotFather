package org.example.data.layer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    // Duration in hours: 1.5h, 0.2h
    private double duration;

    @Column(nullable = false)
    private String type;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "bot_id")
    private Bot owner;

    public String info() {
        return type + " duration: " + duration + "h";
    }
}
