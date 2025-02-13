package org.example.data.layer.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHours implements Serializable {
    @Serial
    private static final long serialVersionUID = 2405172041467251807L;

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false)
    private String day; // e.g., "Monday"

    @OneToMany(mappedBy = "workingHours", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TimeRange> timeRanges = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "bot_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Bot bot;

    @Override
    public String toString() {
        if (timeRanges.isEmpty()) return "";

        StringBuilder stringBuilder = new StringBuilder(day + " ");
        for (TimeRange timeRange : timeRanges) {
            stringBuilder.append(timeRange.getStartTime()).append(" - ").append(timeRange.getEndTime()).append(", ");
        }
        stringBuilder.setLength(stringBuilder.length() - 2); // remove last ", "

        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
}
