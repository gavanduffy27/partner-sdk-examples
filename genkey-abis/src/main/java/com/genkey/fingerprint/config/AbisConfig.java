package com.genkey.fingerprint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "abis")
public class AbisConfig {
    
    private Server server = new Server();
    private Library library = new Library();
    private Capture capture = new Capture();
    
    @Data
    public static class Server {
        private String host = "10.22.74.51";
        private int port = 8091;
        private String domain = "EnrollmentSDK";
        private String legacyHost = "10.22.74.51";
        private String drHost = "10.22.74.51";
    }
    
    @Data
    public static class Library {
        private String path = "c:/Users/Rodb/Desktop/AFIS/6.2.2-SNAPSHOT/abisclient-dll";
    }
    
    @Data
    public static class Capture {
        private int timeout = 30000;
        private int qualityThreshold = 40;
        private int maxRetries = 3;
    }
}
