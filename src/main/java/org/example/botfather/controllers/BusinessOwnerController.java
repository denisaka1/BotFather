package org.example.botfather.controllers;

import lombok.AllArgsConstructor;
import org.example.botfather.data.entities.Bot;
import org.example.botfather.data.services.BusinessOwnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("api/business_owner")
public class BusinessOwnerController {

    private final BusinessOwnerService businessOwnerService;

    @PostMapping("{id}")
    public ResponseEntity<Bot> createBot(@RequestParam Long id, @RequestBody Bot bot) {
        return ResponseEntity.ok(businessOwnerService.saveBot(id, bot));
    }
}
