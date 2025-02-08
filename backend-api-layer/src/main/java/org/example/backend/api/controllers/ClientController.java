package org.example.backend.api.controllers;
import lombok.AllArgsConstructor;
import org.example.backend.api.data.services.ClientService;
import org.example.data.layer.entities.Client;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("api/client")
public class ClientController {
    private final ClientService clientService;

    @GetMapping("{telegramId}")
    public ResponseEntity<Client> findByTelegramId(@PathVariable String telegramId) {
        return clientService.findByTelegramId(telegramId);
    }

    @PostMapping
    public ResponseEntity<Client> craeteClient(@RequestBody Client client) {
        return ResponseEntity.ok(clientService.saveClient(client));
    }
}
