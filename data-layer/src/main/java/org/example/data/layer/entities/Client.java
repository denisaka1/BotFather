package org.example.data.layer.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String telegramId;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String phoneNumber;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments;

    @PostPersist
    private void onCreate() {
        appointments = new ArrayList<>();
    }

    public void addAppointment(Appointment appointment, Bot bot) {
        appointments.add(appointment);
        appointment.setClient(this);
        appointment.setBot(bot);
    }

    public Appointment removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
        appointment.setClient(null);
        appointment.setBot(null);
        appointment.setStatus(Appointment.AppointmentStatus.CANCELED);
        return appointment;
    }

    public Appointment getLastAppointment() {
        if (appointments == null || appointments.isEmpty()) return null;
        return appointments.get(appointments.size() - 1);
    }
}
