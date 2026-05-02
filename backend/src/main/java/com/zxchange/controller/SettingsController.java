package com.zxchange.controller;

import com.zxchange.model.entity.SettingsEntity;
import com.zxchange.repository.SettingsRepository;
import com.zxchange.service.EncryptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsRepository settingsRepository;
    private final EncryptionService encryptionService;

    public SettingsController(SettingsRepository settingsRepository, EncryptionService encryptionService) {
        this.settingsRepository = settingsRepository;
        this.encryptionService = encryptionService;
    }

    @GetMapping
    public List<SettingsEntity> getSettings() {
        return settingsRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> updateSetting(@RequestBody Map<String, String> request) {
        String key = request.get("key");
        String value = request.get("value");
        boolean encrypt = Boolean.parseBoolean(request.getOrDefault("encrypt", "false"));

        if (key == null || value == null) {
            return ResponseEntity.badRequest().body("Key and Value are required");
        }

        SettingsEntity setting = settingsRepository.findByKey(key)
                .orElse(new SettingsEntity());
        
        setting.setKey(key);
        setting.setEncrypted(encrypt);
        setting.setValue(encrypt ? encryptionService.encrypt(value) : value);
        
        settingsRepository.save(setting);
        return ResponseEntity.ok(setting);
    }
}
