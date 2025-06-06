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
public class Bot {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
//    @Column(nullable = false)
    private String name;

    @Column
//    @Column(nullable = false)
    private String username;

    @Column
//    @Column(nullable = false, unique = true)
    private String token;

    @Column
    private String welcomeMessage;

    @Column
    @Enumerated(EnumType.STRING)
    private BotCreationState creationState;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "owner_id")
    private BusinessOwner owner;

    @OneToMany(mappedBy = "bot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Job> jobs = new ArrayList<>();

    @OneToMany(mappedBy = "bot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WorkingHours> workingHours = new ArrayList<>();

    @PostPersist
    private void postCreate() {
        creationState = BotCreationState.ASK_BOT_FATHER_BOT_CREATION_MESSAGE;
    }

    public void addJob(Job job) {
        jobs.add(job);
        job.setOwner(this);
    }

    public void removeJob(Job job) {
        jobs.remove(job);
        job.setOwner(null);
    }

    public void addJobs(List<Job> jobs) {
        for (Job job : jobs) {
            addJob(job);
        }
    }

    public void addWorkingHour(WorkingHours workingHour) {
        workingHours.add(workingHour);
        workingHour.setBot(this);
        if (workingHour.getTimeRanges() == null || workingHour.getTimeRanges().isEmpty()) {
            return;
        }

        for (TimeRange timeRange : workingHour.getTimeRanges()) {
            timeRange.setWorkingHours(workingHour);
        }
    }

    public void removeWorkingHour(WorkingHours workingHour) {
        workingHours.remove(workingHour);
        workingHour.setBot(null);
        workingHour.getTimeRanges().clear();
    }

    public void addWorkingHours(List<WorkingHours> workingHours) {
        for (WorkingHours workingHour : workingHours) {
            addWorkingHour(workingHour);
        }
    }
}
