package org.example.data.layer.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientScheduleState> scheduleStates;

    @PostPersist
    private void onCreate() {
        appointments = new ArrayList<>();
    }

    public void addAppointment(Appointment appointment, Bot bot) {
        appointments.add(appointment);
        appointment.setClient(this);
        appointment.setBot(bot);
    }

    public void addScheduleState(Long key, AppointmentScheduleState state) {
        ClientScheduleState scheduleState = new ClientScheduleState();
        scheduleState.setBotId(key);
        scheduleState.setState(state);
        scheduleState.setClient(this);
        scheduleStates.add(scheduleState);
    }

    public void updateScheduleState(ClientScheduleState clientScheduleState) {
        Long key = clientScheduleState.getBotId();
        AppointmentScheduleState state = clientScheduleState.getState();
        Optional<ClientScheduleState> existingState = scheduleStates.stream()
                .filter(s -> s.getBotId().equals(key))
                .findFirst();
        if (existingState.isPresent()) {
            existingState.get().setState(state);
        } else {
            addScheduleState(key, state);
        }
    }

    public Appointment removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
        appointment.setClient(null);
        appointment.setBot(null);
        return appointment;
    }

    public Appointment getLastAppointment() {
        if (appointments == null || appointments.isEmpty()) return null;
        return appointments.get(appointments.size() - 1);
    }

    public enum AppointmentScheduleState {AuthState, ScheduleOrCancelQuestionState, ScheduleState}
}
