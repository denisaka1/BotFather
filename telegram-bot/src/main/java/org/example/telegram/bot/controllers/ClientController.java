package org.example.telegram.bot.controllers;
import lombok.AllArgsConstructor;
import org.example.telegram.bot.data.entities.Client;
import org.example.telegram.bot.data.services.ClientService;
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
