package org.example.botfather.data.entities;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
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
    @JoinColumn(name = "bot_id")
    private Bot owner;

    public String toString() {
        return "Job{id=" + id + ", duration='" + duration + "', type='" + type + "'}";
    }
}
