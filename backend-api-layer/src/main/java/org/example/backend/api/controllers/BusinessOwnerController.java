package org.example.backend.api.controllers;

import lombok.AllArgsConstructor;
import org.example.backend.api.data.services.BusinessOwnerService;
import org.example.data.layer.entities.Bot;
import org.example.data.layer.entities.BusinessOwner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/business_owner")
public class BusinessOwnerController {

    private final BusinessOwnerService businessOwnerService;

    @PostMapping("{userTelegramId}")
    public ResponseEntity<Bot> createBot(@PathVariable Long userTelegramId, @RequestBody Bot bot) {
        return ResponseEntity.ok(businessOwnerService.saveBot(userTelegramId, bot));
    }

    @DeleteMapping("{userTelegramId}/bots/{botId}")
    public ResponseEntity<Bot> deleteBot(@PathVariable Long userTelegramId, @PathVariable Long botId) {
        return ResponseEntity.ok(businessOwnerService.deleteBot(userTelegramId, botId));
    }

    @GetMapping("{userTelegramId}/bots")
    public ResponseEntity<List<Bot>> findAllBots(@PathVariable Long userTelegramId) {
        return ResponseEntity.ok(businessOwnerService.findAllBots(userTelegramId));
    }

    @GetMapping("{userTelegramId}/exists")
    public ResponseEntity<Boolean> existsByUserTelegramId(@PathVariable Long userTelegramId) {
        boolean exists = businessOwnerService.existsByUserTelegramId(userTelegramId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("{userTelegramId}/editable")
    public ResponseEntity<Bot> getEditableBot(@PathVariable Long userTelegramId) {
        return ResponseEntity.ok(businessOwnerService.getEditableBot(userTelegramId));
    }

    @PostMapping
    public ResponseEntity<BusinessOwner> createBusinessOwner(@RequestBody BusinessOwner businessOwner) {
        return ResponseEntity.ok(businessOwnerService.saveBusinessOwner(businessOwner));
    }

    @PutMapping("{id}")
    public ResponseEntity<BusinessOwner> updateBusinessOwner(@PathVariable Long id, @RequestBody BusinessOwner businessOwner) {
        return ResponseEntity.ok(businessOwnerService.updateBusinessOwner(id, businessOwner));
    }

    @GetMapping("{userTelegramId}")
    public ResponseEntity<BusinessOwner> getOwner(@PathVariable Long userTelegramId) {
        return ResponseEntity.ok(businessOwnerService.getOwner(userTelegramId));
    }
}
