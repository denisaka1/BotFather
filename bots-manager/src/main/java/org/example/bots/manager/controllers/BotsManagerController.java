package org.example.bots.manager.controllers;

import lombok.AllArgsConstructor;
import org.example.bots.manager.services.BotsManager;
import org.example.client.api.processor.MessageBatchProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@AllArgsConstructor
@RestController
@RequestMapping("api/bots_manager")
public class BotsManagerController {
    private final BotsManager botsManager;
    private final MessageBatchProcessor messageBatchProcessor;

    @PostMapping("/send_message")
    public ResponseEntity<Boolean> sendMessage(@RequestBody SendMessage message) {
        messageBatchProcessor.addMessage(message);
        botsManager.sendMessage();
        return ResponseEntity.ok(true); // Assume success
    }
}
