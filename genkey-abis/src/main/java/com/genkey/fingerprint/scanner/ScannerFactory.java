package com.genkey.fingerprint.scanner;

import com.genkey.fingerprint.config.ScannerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Factory configuration for fingerprint scanners.
 * Returns the appropriate scanner implementation based on configuration.
 */
@Slf4j
@Configuration
public class ScannerFactory {
    
    @Bean
    @Primary
    public FingerprintScanner fingerprintScanner(ScannerConfig config) {
        log.info("Creating fingerprint scanner of type: {}", config.getType());
        
        FingerprintScanner scanner = switch (config.getType()) {
            case MOCK -> new MockFingerprintScanner(config);
            case GENERIC_USB -> new GenericUsbScanner(config);
            case INTEGRATED_BIOMETRICS -> new IntegratedBiometricsScanner(config);
            case WINDOWS_BIOMETRIC -> new WindowsBiometricScanner(config);
            case SECUGEN -> new SecuGenScanner(config);
            case FUTRONIC -> new FutronicScanner(config);
            case DIGITAL_PERSONA -> new DigitalPersonaScanner(config);
            case CROSSMATCH -> new CrossmatchScanner(config);
        };
        
        // Initialize the scanner
        if (scanner.initialize()) {
            log.info("Scanner initialized successfully: {}", scanner.getDeviceInfo());
        } else {
            log.warn("Scanner initialization failed, falling back to Mock scanner");
            scanner = new MockFingerprintScanner(config);
            scanner.initialize();
        }
        
        return scanner;
    }
}

/**
 * Futronic Scanner Implementation (Template)
 */
@Slf4j
class FutronicScanner implements FingerprintScanner {
    private final ScannerConfig config;
    private boolean initialized = false;
    
    public FutronicScanner(ScannerConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean initialize() {
        log.warn("Futronic SDK not integrated. Add Futronic SDK to implement.");
        return false;
    }
    
    @Override
    public boolean isReady() { return initialized; }
    
    @Override
    public com.genkey.fingerprint.model.CaptureResult capture(int finger, int timeout) {
        return com.genkey.fingerprint.model.CaptureResult.builder()
                .success(false)
                .statusCode(-99)
                .statusMessage("Futronic SDK not integrated")
                .finger(finger)
                .build();
    }
    
    @Override
    public String getDeviceInfo() { return "Futronic Scanner (Not integrated)"; }
    
    @Override
    public void release() { initialized = false; }
    
    @Override
    public String getScannerType() { return "FUTRONIC"; }
}

/**
 * Digital Persona Scanner Implementation (Template)
 */
@Slf4j
class DigitalPersonaScanner implements FingerprintScanner {
    private final ScannerConfig config;
    private boolean initialized = false;
    
    public DigitalPersonaScanner(ScannerConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean initialize() {
        log.warn("Digital Persona SDK not integrated. Add DP SDK to implement.");
        return false;
    }
    
    @Override
    public boolean isReady() { return initialized; }
    
    @Override
    public com.genkey.fingerprint.model.CaptureResult capture(int finger, int timeout) {
        return com.genkey.fingerprint.model.CaptureResult.builder()
                .success(false)
                .statusCode(-99)
                .statusMessage("Digital Persona SDK not integrated")
                .finger(finger)
                .build();
    }
    
    @Override
    public String getDeviceInfo() { return "Digital Persona Scanner (Not integrated)"; }
    
    @Override
    public void release() { initialized = false; }
    
    @Override
    public String getScannerType() { return "DIGITAL_PERSONA"; }
}

/**
 * Crossmatch Scanner Implementation (Template)
 */
@Slf4j
class CrossmatchScanner implements FingerprintScanner {
    private final ScannerConfig config;
    private boolean initialized = false;
    
    public CrossmatchScanner(ScannerConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean initialize() {
        log.warn("Crossmatch SDK not integrated. Add Crossmatch SDK to implement.");
        return false;
    }
    
    @Override
    public boolean isReady() { return initialized; }
    
    @Override
    public com.genkey.fingerprint.model.CaptureResult capture(int finger, int timeout) {
        return com.genkey.fingerprint.model.CaptureResult.builder()
                .success(false)
                .statusCode(-99)
                .statusMessage("Crossmatch SDK not integrated")
                .finger(finger)
                .build();
    }
    
    @Override
    public String getDeviceInfo() { return "Crossmatch Scanner (Not integrated)"; }
    
    @Override
    public void release() { initialized = false; }
    
    @Override
    public String getScannerType() { return "CROSSMATCH"; }
}
