package org.example.data.layer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "workingHours", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeRange> timeRanges = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "bot_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Bot bot;

    public void addTimeRange(TimeRange timeRange) {
        if (timeRanges == null) {
            timeRanges = new ArrayList<>();
        }
        timeRanges.add(timeRange);
        timeRange.setWorkingHours(this);
    }

    public void removeTimeRange(TimeRange timeRange) {
        if (timeRanges == null) return;
        
        timeRanges.remove(timeRange);
        timeRange.setWorkingHours(null);
    }

    public void addTimeRanges(List<TimeRange> timeRanges) {
        for (TimeRange timeRange : timeRanges) {
            addTimeRange(timeRange);
        }
    }

    public void replaceTimeRanges(List<TimeRange> timeRanges) {
        timeRanges.clear();
        for (TimeRange timeRange : timeRanges) {
            addTimeRange(timeRange);
        }
    }

    public String info() {
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
