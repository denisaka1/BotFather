package org.example.telegram.bot.redis.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.example.data.layer.entities.BotCreationState;
import org.example.data.layer.entities.Job;
import org.example.data.layer.entities.WorkingHours;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@RedisHash
public class BotSession implements Serializable {
    private Long id;
    private String name;
    private String username;
    private String token;
    private String welcomeMessage;
    private List<Job> jobs;
    private List<WorkingHours> workingHours;
    private BotCreationState creationState;
}
