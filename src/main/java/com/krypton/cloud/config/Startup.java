package com.krypton.cloud.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class Startup implements CommandLineRunner {

    @Override
    public void run(String... args) {
        var rootFolder = new File(System.getProperty("user.home") + "/cloud");

        if (!rootFolder.exists()) {
            rootFolder.mkdir();
        }
    }
}
