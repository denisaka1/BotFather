package org.example.data.layer.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bot {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String token;

    @Column
    private String welcomeMessage;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "owner_id")
    private BusinessOwner owner;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Job> jobs = new ArrayList<>();

    @OneToMany(mappedBy = "bot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkingHours> workingHours = new ArrayList<>();

    public void addJob(Job job) {
        jobs.add(job);
        job.setOwner(this);
    }

    public void removeJob(Job job) {
        jobs.remove(job);
    }

    public void addWorkingHour(WorkingHours workingHour) {
        workingHours.add(workingHour);
        workingHour.setBot(this);
        for (TimeRange timeRange : workingHour.getTimeRanges()) {
            timeRange.setWorkingHours(workingHour);
        }
    }

    public void removeWorkingHour(WorkingHours workingHour) {
        workingHours.remove(workingHour);
        workingHour.setBot(null);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Name: ").append(name).append("\n");
        stringBuilder.append("Username: ").append(username).append("\n");
        stringBuilder.append("Welcome message:").append("\n").append(welcomeMessage).append("\n\n");

        stringBuilder.append("Working hours:").append("\n");
        for (WorkingHours workingHour : workingHours) {
            stringBuilder.append(workingHour);
        }
        stringBuilder.append("\n");

        stringBuilder.append("Jobs:").append("\n");
        List<Job> sortedJobs = jobs.stream()
                .sorted(Comparator.comparing(Job::getType))
                .toList();

        Job previousJob = sortedJobs.get(0);
        stringBuilder.append(previousJob);
        for (Job job : sortedJobs.subList(1, sortedJobs.size())) {
            if (Objects.equals(previousJob.getType(), job.getType())) {
                stringBuilder.append(", ").append(job.getDuration());
            } else {
                stringBuilder.append("\n").append(job);
            }
            previousJob = job;
        }
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
