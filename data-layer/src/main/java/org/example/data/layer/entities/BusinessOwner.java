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
public class BusinessOwner {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userTelegramId;

    @Column(nullable = false)
    private String firstName;

    @Column
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column
    private String address;

    @OneToMany(mappedBy = "owner", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Bot> bots = new ArrayList<>();

    public void addBot(Bot bot) {
        bots.add(bot);
    }

    public void removeBot(Bot bot) {
        bots.remove(bot);
    }
}
