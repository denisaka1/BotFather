package org.example.backend.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
        scanBasePackages = {
                "org.example.backend.api.controllers",
                "org.example.backend.api.data.repositories",
                "org.example.backend.api.data.services"
        }
)
@EntityScan(
        basePackages = {
                "org.example.data.layer.entities"
        }
)
public class BackendApiLayerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApiLayerApplication.class, args);
    }

}
