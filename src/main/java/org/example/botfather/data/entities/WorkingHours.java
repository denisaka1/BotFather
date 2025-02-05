package org.example.botfather.data.entities;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHours {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false)
    private String day; // e.g., "Monday"

    @Column(nullable = false)
    private String timeRange; // e.g., "09:00 - 17:00" or "None"

    @ManyToOne
    @JoinColumn(name = "bot_id", nullable = false)
    private Bot bot;

    @Override
    public String toString() {
        return "WorkingHours{id=" + id + ", day='" + day + "', timeRange='" + timeRange + "'}";
    }
}
