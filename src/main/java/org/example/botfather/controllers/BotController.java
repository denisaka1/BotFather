package org.example.botfather.controllers;
import lombok.AllArgsConstructor;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.data.services.BotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/bots")
public class BotController {

    private final BotService botService;

    @GetMapping
    public ResponseEntity<List<Bot>> getAllBots() {
        return ResponseEntity.ok(botService.getBots());
    }

    @PostMapping
    public ResponseEntity<Bot> createBot(@RequestBody Bot bot) {
        return ResponseEntity.ok(botService.saveBot(bot));
    }
}
