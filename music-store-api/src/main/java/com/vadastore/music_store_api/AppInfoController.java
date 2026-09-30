package com.vadastore.music_store_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppInfoController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/app-name")
    public String getAppName() {
        return "Application name" + appName;
    }

}
