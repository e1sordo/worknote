package es.e1sordo.worknote.controllers;

import es.e1sordo.worknote.dto.AppSettingDto;
import es.e1sordo.worknote.enums.AppSettingKeys;
import es.e1sordo.worknote.mapping.Mappings;
import es.e1sordo.worknote.models.AppSettingEntity;
import es.e1sordo.worknote.services.AppSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/settings")
@RequiredArgsConstructor
public class AppSettingsController {

    private final AppSettingsService service;

    @GetMapping("/{key}")
    public ResponseEntity<AppSettingDto> get(@PathVariable String key) {
        final var existedKey = AppSettingKeys.valueOf(key);
        final AppSettingEntity setting = service.get(existedKey);
        if (setting == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Mappings.mapToDto(setting));
    }

    @PutMapping("/{key}")
    public void set(@PathVariable String key, @RequestBody AppSettingDto request) {
        final var existedKey = AppSettingKeys.valueOf(key);
        service.set(existedKey, request.value());
    }
}
