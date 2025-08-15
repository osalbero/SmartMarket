package com.smartmarket.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WelcomeController {

    // Este endpoint de tipo POST se usa para enviar datos (ej. desde un formulario)
    @PostMapping(value = "/post")
    public String WelcomePost()
    {
        return "Welcome from secure endpoint (POST)";
    }

    // Este nuevo endpoint de tipo GET se usa para obtener datos
    // y se puede acceder directamente desde el navegador.
    @GetMapping(value = "/get")
    public String WelcomeGet()
    {
        return "Hello from a secure endpoint (GET)";
    }

}