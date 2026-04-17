package com.genkey.fingerprint.scanner;

import com.genkey.fingerprint.config.ScannerConfig;
import com.genkey.fingerprint.model.CaptureResult;
import com.genkey.fingerprint.model.MultipleFingerCaptureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * Mock fingerprint scanner for testing without hardware.
 * Generates synthetic fingerprint images for development and testing.
 */
@Slf4j
@Component
public class MockFingerprintScanner implements FingerprintScanner {
    
    private final ScannerConfig config;
    private boolean initialized = false;
    private final Random random = new Random();
    
    public MockFingerprintScanner(ScannerConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean initialize() {
        log.info("Initializing Mock Fingerprint Scanner...");
        initialized = true;
        log.info("Mock Scanner initialized successfully");
        return true;
    }
    
    @Override
    public boolean isReady() {
        return initialized;
    }
    
    @Override
    public CaptureResult capture(int finger, int timeout) {
        log.info("Mock capture for finger {} with timeout {}ms", finger, timeout);
        
        if (!initialized) {
            return CaptureResult.builder()
                    .success(false)
                    .statusCode(-1)
                    .statusMessage("Scanner not initialized")
                    .finger(finger)
                    .build();
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Simulate capture delay
            Thread.sleep(500 + random.nextInt(500));
            
            // Generate a mock fingerprint image
            byte[] imageData = generateMockFingerprint(finger);
            int quality = 60 + random.nextInt(40); // Random quality 60-100
            
            long captureTime = System.currentTimeMillis() - startTime;
            
            return CaptureResult.builder()
                    .success(true)
                    .statusCode(0)
                    .statusMessage("Capture successful")
                    .finger(finger)
                    .imageData(imageData)
                    .imageFormat(config.getImageFormat())
                    .quality(quality)
                    .width(300)
                    .height(400)
                    .resolution(config.getResolution())
                    .captureTimeMs(captureTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Mock capture failed", e);
            return CaptureResult.builder()
                    .success(false)
                    .statusCode(-2)
                    .statusMessage("Capture failed: " + e.getMessage())
                    .finger(finger)
                    .build();
        }
    }
    
    @Override
    public MultipleFingerCaptureResult captureMultiple(int[] fingers, int timeout) {
        log.info("Mock multiple capture for fingers {} with timeout {}ms", java.util.Arrays.toString(fingers), timeout);
        
        if (!initialized) {
            return MultipleFingerCaptureResult.multiBuilder()
                    .success(false)
                    .statusCode(-1)
                    .statusMessage("Scanner not initialized")
                    .fingers(fingers)
                    .build();
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Simulate capture delay
            Thread.sleep(800 + random.nextInt(700));
            
            // Generate a mock fingerprint image (larger for multiple fingers)
            byte[] imageData = generateMockMultipleFingerprint(fingers);
            int quality = 60 + random.nextInt(30); // Random quality 60-90
            
            long captureTime = System.currentTimeMillis() - startTime;
            
            return MultipleFingerCaptureResult.multiBuilder()
                    .success(true)
                    .statusCode(0)
                    .statusMessage("Multiple capture successful")
                    .fingers(fingers)
                    .imageData(imageData)
                    .imageFormat(config.getImageFormat())
                    .quality(quality)
                    .width(600)
                    .height(400)
                    .resolution(config.getResolution())
                    .captureTimeMs(captureTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Mock multiple capture failed", e);
            return MultipleFingerCaptureResult.multiBuilder()
                    .success(false)
                    .statusCode(-2)
                    .statusMessage("Multiple capture failed: " + e.getMessage())
                    .fingers(fingers)
                    .build();
        }
    }
    
    @Override
    public String getDeviceInfo() {
        return "Mock Fingerprint Scanner v1.0 (Testing Only)";
    }
    
    @Override
    public void release() {
        log.info("Releasing Mock Scanner resources");
        initialized = false;
    }
    
    @Override
    public String getScannerType() {
        return "MOCK";
    }
    
    /**
     * Generate a mock fingerprint image with synthetic patterns
     */
    private byte[] generateMockFingerprint(int finger) throws Exception {
        int width = 300;
        int height = 400;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = image.createGraphics();
        
        // Fill with light gray background
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillRect(0, 0, width, height);
        
        // Draw elliptical fingerprint shape
        g2d.setColor(new Color(150, 150, 150));
        g2d.fillOval(30, 50, width - 60, height - 100);
        
        // Draw ridge patterns (simplified)
        g2d.setColor(new Color(80, 80, 80));
        g2d.setStroke(new BasicStroke(2));
        
        // Create wave-like ridge patterns unique to each finger
        int seed = finger * 12345;
        Random patternRandom = new Random(seed);
        
        for (int y = 60; y < height - 60; y += 8) {
            int[] xPoints = new int[20];
            int[] yPoints = new int[20];
            
            for (int i = 0; i < 20; i++) {
                int x = 40 + i * 12;
                int wave = (int)(Math.sin((x + y + patternRandom.nextInt(20)) * 0.1) * 10);
                xPoints[i] = x + wave;
                yPoints[i] = y + patternRandom.nextInt(3);
            }
            
            g2d.drawPolyline(xPoints, yPoints, 20);
        }
        
        // Add some minutiae points (simplified)
        g2d.setColor(new Color(50, 50, 50));
        for (int i = 0; i < 15; i++) {
            int x = 50 + patternRandom.nextInt(width - 100);
            int y = 80 + patternRandom.nextInt(height - 160);
            g2d.fillOval(x, y, 4, 4);
        }
        
        g2d.dispose();
        
        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = config.getImageFormat().equalsIgnoreCase("BMP") ? "bmp" : 
                       config.getImageFormat().toLowerCase();
        ImageIO.write(image, format, baos);
        
        return baos.toByteArray();
    }
    
    /**
     * Generate a mock multiple fingerprint image
     */
    private byte[] generateMockMultipleFingerprint(int[] fingers) throws Exception {
        int width = 600;
        int height = 400;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = image.createGraphics();
        
        // Fill with light gray background
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillRect(0, 0, width, height);
        
        // Draw multiple fingerprint shapes side by side
        int fingerWidth = (width - 100) / fingers.length;
        
        for (int i = 0; i < fingers.length; i++) {
            int finger = fingers[i];
            int x = 50 + i * fingerWidth;
            
            // Draw elliptical fingerprint shape
            g2d.setColor(new Color(150, 150, 150));
            g2d.fillOval(x, 50, fingerWidth - 20, height - 100);
            
            // Draw ridge patterns
            g2d.setColor(new Color(80, 80, 80));
            g2d.setStroke(new BasicStroke(2));
            
            int seed = finger * 12345 + i * 1000;
            Random patternRandom = new Random(seed);
            
            for (int y = 60; y < height - 60; y += 8) {
                int[] xPoints = new int[15];
                int[] yPoints = new int[15];
                
                for (int j = 0; j < 15; j++) {
                    int xPos = x + 10 + j * (fingerWidth - 40) / 15;
                    int wave = (int)(Math.sin((xPos + y + patternRandom.nextInt(20)) * 0.1) * 8);
                    xPoints[j] = xPos + wave;
                    yPoints[j] = y + patternRandom.nextInt(3);
                }
                
                g2d.drawPolyline(xPoints, yPoints, 15);
            }
            
            // Add finger label
            g2d.setColor(new Color(50, 50, 50));
            g2d.drawString("F" + finger, x + fingerWidth/2 - 10, 30);
        }
        
        g2d.dispose();
        
        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = config.getImageFormat().equalsIgnoreCase("BMP") ? "bmp" : 
                       config.getImageFormat().toLowerCase();
        ImageIO.write(image, format, baos);
        
        return baos.toByteArray();
    }
}
