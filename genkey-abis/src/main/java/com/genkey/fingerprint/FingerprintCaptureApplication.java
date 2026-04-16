package com.genkey.fingerprint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FingerprintCaptureApplication {

    public static void main(String[] args) {
        // Set native library path for ABIS SDK
        String libraryPath = System.getProperty("java.library.path");
        if (libraryPath == null || !libraryPath.contains("abisclient-dll")) {
            System.setProperty("java.library.path", 
                "c:\\Users\\Rodb\\Desktop\\AFIS\\6.2.2-SNAPSHOT\\abisclient-dll");
        }
        
        SpringApplication.run(FingerprintCaptureApplication.class, args);
    }
}
