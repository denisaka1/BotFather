package org.example.data.layer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class ClientScheduleState {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long botId;

    @Enumerated(EnumType.STRING)
    private Client.AppointmentScheduleState state;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnore
    private Client client;
}
