package org.example.botfather.controllers;

import lombok.AllArgsConstructor;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.data.entities.BusinessOwner;
import org.example.botfather.data.services.BusinessOwnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/business_owner")
public class BusinessOwnerController {

    private final BusinessOwnerService businessOwnerService;

    @PostMapping("{userTelegramId}")
    public ResponseEntity<Bot> createBot(@RequestParam String userTelegramId, @RequestBody Bot bot) {
        return ResponseEntity.ok(businessOwnerService.saveBot(userTelegramId, bot));
    }

    @GetMapping("{id}")
    public ResponseEntity<List<Bot>> findAllBots(@RequestParam Long id){
        return ResponseEntity.ok(businessOwnerService.findAllBots(id));
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByUserTelegramId(@RequestParam String userTelegramId) {
        boolean exists = businessOwnerService.existsByUserTelegramId(userTelegramId);
        return ResponseEntity.ok(exists);
    }

    @PostMapping
    public ResponseEntity<BusinessOwner> craeteBusinessOwner(@RequestBody BusinessOwner businessOwner) {
        return ResponseEntity.ok(businessOwnerService.saveBusinessOwner(businessOwner));
    }
}
