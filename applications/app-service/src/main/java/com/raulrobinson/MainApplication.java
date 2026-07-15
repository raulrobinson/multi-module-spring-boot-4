package com.raulrobinson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap de la aplicación.
 * <p>
 * Módulo: infrastructure/entry-points/reactivé-web
 * Único módulo con 'apply plugin: org.springframework.boot'.
 *
 * @SpringBootApplication escanea desde 'com.raulrobinson' hacia abajo,
 * lo que cubre todos los paquetes de todos los módulos del proyecto.
 */
@SpringBootApplication(scanBasePackages = "com.raulrobinson")
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
