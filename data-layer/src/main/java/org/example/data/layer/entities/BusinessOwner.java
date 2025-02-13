package org.example.data.layer.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOwner {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
//    @Column(nullable = false, unique = true)
    private Long userTelegramId;

    @Column
//    @Column(nullable = false)
    private String firstName;

    @Column
    private String lastName;

    @Column
//    @Column(nullable = false, unique = true)
    private String email;

    @Column
//    @Column(nullable = false)
    private String phoneNumber;

    @Column
    private String address;

    @Column
    @Enumerated(EnumType.STRING)
    private OwnerRegistrationState registrationState;

    @OneToMany(mappedBy = "owner", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Bot> bots = new ArrayList<>();

    @PostPersist
    private void onCreate() {
        registrationState = OwnerRegistrationState.ASK_PHONE;
    }

    public Bot addBot(Bot bot) {
        bots.add(bot);
        bot.setOwner(this);
        return bots.get(bots.size() - 1);
    }

    public void removeBot(Bot bot) {
        bots.remove(bot);
    }
}
