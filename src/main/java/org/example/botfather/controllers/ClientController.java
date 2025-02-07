package org.example.botfather.controllers;
import lombok.AllArgsConstructor;
import org.example.botfather.data.entities.Client;
import org.example.botfather.data.services.ClientService;
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
