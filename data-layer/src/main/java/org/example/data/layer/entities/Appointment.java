package org.example.data.layer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime appointmentDate;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "bot_id")
    private Bot bot;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    public enum AppointmentStatus {
        PENDING,
        APPROVED,
        CANCELED
    }

    public enum AppointmentCreationStep {
        DATE_SELECTED,
        UPDATE_DATES,
        JOB_SELECTED,
        BACK_TO_DATES,
        HOUR_SELECTED,
        UPDATE_HOURS,
        BACK_TO_JOBS,
        BACK_TO_MENU,
        CANCEL_APPOINTMENT
    }
}
