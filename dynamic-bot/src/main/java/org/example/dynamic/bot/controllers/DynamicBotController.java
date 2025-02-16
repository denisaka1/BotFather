package org.example.dynamic.bot.controllers;

import lombok.AllArgsConstructor;
import org.example.data.layer.entities.Bot;
import org.example.dynamic.bot.services.DynamicBot;
import org.example.dynamic.bot.services.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@AllArgsConstructor
@RestController
@RequestMapping("api/dynamic_bot")
public class DynamicBotController {
    private final DynamicBot dynamicBot;
    private final RegistrationService registrationService;

    @PostMapping("/send_message")
    public ResponseEntity<Boolean> sendMessage(@RequestBody SendMessage message) {
        dynamicBot.handleAppointmentResponse(message); // No return value
        return ResponseEntity.ok(true); // Assume success
    }

    @PostMapping("/register_bot")
    public ResponseEntity<Bot> registerBot(@RequestBody Bot bot) {
        registrationService.registerBot(bot); // No return value
        return ResponseEntity.ok(bot); // Assume success
    }
}
