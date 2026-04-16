package com.genkey.fingerprint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "scanner")
public class ScannerConfig {
    
    private ScannerType type = ScannerType.MOCK;
    private int deviceIndex = 0;
    private int resolution = 500;
    private String imageFormat = "BMP";
    
    public enum ScannerType {
        MOCK,           // For testing without hardware
        GENERIC_USB,    // Generic USB scanner with SDK auto-detection
        INTEGRATED_BIOMETRICS, // Integrated Biometrics scanners
        WINDOWS_BIOMETRIC, // Windows Biometric Framework (works with most scanners)
        SECUGEN,        // SecuGen scanners
        FUTRONIC,       // Futronic scanners
        DIGITAL_PERSONA,// Digital Persona scanners
        CROSSMATCH      // Crossmatch scanners
    }
}
