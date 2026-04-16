package com.genkey.fingerprint.scanner;

import com.genkey.fingerprint.config.ScannerConfig;
import com.genkey.fingerprint.model.CaptureResult;
import com.genkey.fingerprint.model.MultipleFingerCaptureResult;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Generic USB Scanner Implementation.
 * 
 * This implementation works as a bridge to actual scanner SDKs.
 * It looks for scanner software/SDKs and provides a unified interface.
 * 
 * To use a real scanner, you need to:
 * 1. Install the scanner's driver and SDK
 * 2. Configure the SDK path in application.yml
 * 3. Implement the native bridge methods below
 */
@Slf4j
public class GenericUsbScanner implements FingerprintScanner {
    
    private final ScannerConfig config;
    private boolean initialized = false;
    private String deviceModel = "Unknown Scanner";
    private Random random = new Random();
    
    public GenericUsbScanner(ScannerConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean initialize() {
        log.info("Initializing Generic USB Scanner...");
        
        try {
            // Try to detect scanner via common SDK paths
            String[] sdkPaths = {
                "C:\\Program Files\\SecuGen",
                "C:\\Program Files\\Digital Persona",
                "C:\\Program Files\\Futronic",
                "C:\\Program Files (x86)\\SecuGen",
                "C:\\Program Files (x86)\\Digital Persona",
                "C:\\Program Files (x86)\\Futronic"
            };
            
            for (String sdkPath : sdkPaths) {
                File sdkDir = new File(sdkPath);
                if (sdkDir.exists() && sdkDir.isDirectory()) {
                    log.info("Found potential scanner SDK at: {}", sdkPath);
                    deviceModel = sdkDir.getName() + " Scanner";
                    initialized = true;
                    return true;
                }
            }
            
            // No SDK found, but allow initialization anyway
            log.warn("No scanner SDK detected in common locations");
            log.info("Scanner will work in simulation mode");
            log.info("To use a real scanner:");
            log.info("  1. Install your scanner's SDK and drivers");
            log.info("  2. Set scanner.type to the specific scanner type (SECUGEN, FUTRONIC, etc.)");
            log.info("  3. Or implement the native capture method in this class");
            
            deviceModel = "Generic USB Scanner (Simulation Mode)";
            initialized = true;
            return true;
            
        } catch (Exception e) {
            log.error("Failed to initialize Generic USB scanner", e);
            return false;
        }
    }
    
    @Override
    public boolean isReady() {
        return initialized;
    }
    
    @Override
    public CaptureResult capture(int finger, int timeout) {
        if (!initialized) {
            return CaptureResult.builder()
                    .success(false)
                    .statusCode(-1)
                    .statusMessage("Scanner not initialized")
                    .finger(finger)
                    .build();
        }
        
        long startTime = System.currentTimeMillis();
        
        log.info("==========================================");
        log.info("  FINGERPRINT CAPTURE REQUEST");
        log.info("  Finger: {} ({})", finger, getFingerName(finger));
        log.info("  Timeout: {} ms", timeout);
        log.info("  *** PLEASE PLACE FINGER ON SCANNER ***");
        log.info("==========================================");
        
        try {
            // TODO: Replace this with actual scanner SDK call
            // For now, simulate a capture
            
            // Simulate user placing finger on scanner
            Thread.sleep(2000); // Wait 2 seconds to simulate capture delay
            
            // Generate a simulated fingerprint image
            byte[] imageData = generateSimulatedFingerprint(finger);
            
            // Simulate quality score
            int quality = 70 + random.nextInt(25); // 70-95
            
            long captureTime = System.currentTimeMillis() - startTime;
            
            log.info("==========================================");
            log.info("  CAPTURE COMPLETED");
            log.info("  Status: SUCCESS");
            log.info("  Quality: {}/100", quality);
            log.info("  Time: {} ms", captureTime);
            log.info("  Image Size: {} bytes", imageData.length);
            log.info("==========================================");
            
            return CaptureResult.builder()
                    .success(true)
                    .statusCode(0)
                    .statusMessage("Capture successful (Simulated)")
                    .finger(finger)
                    .imageData(imageData)
                    .imageFormat("BMP")
                    .quality(quality)
                    .width(500)
                    .height(500)
                    .resolution(config.getResolution())
                    .captureTimeMs(captureTime)
                    .build();
                    
        } catch (InterruptedException e) {
            log.warn("Capture interrupted");
            Thread.currentThread().interrupt();
            return CaptureResult.builder()
                    .success(false)
                    .statusCode(-3)
                    .statusMessage("Capture interrupted")
                    .finger(finger)
                    .build();
        } catch (Exception e) {
            log.error("Generic USB capture failed", e);
            return CaptureResult.builder()
                    .success(false)
                    .statusCode(-2)
                    .statusMessage("Capture failed: " + e.getMessage())
                    .finger(finger)
                    .build();
        }
    }
    
    /**
     * Generate a simulated fingerprint image.
     * In production, this would be replaced by actual scanner SDK capture.
     */
    private byte[] generateSimulatedFingerprint(int finger) throws Exception {
        int width = 500;
        int height = 500;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = image.createGraphics();
        
        // Create a light gray background
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, width, height);
        
        // Draw simulated fingerprint pattern
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(2));
        
        // Draw concentric ovals to simulate fingerprint ridges
        int centerX = width / 2;
        int centerY = height / 2;
        for (int i = 0; i < 15; i++) {
            int ovalWidth = 50 + (i * 25);
            int ovalHeight = 40 + (i * 20);
            g2d.drawOval(centerX - ovalWidth/2, centerY - ovalHeight/2, ovalWidth, ovalHeight);
        }
        
        // Add some random noise to make it look more realistic
        g2d.setColor(Color.GRAY);
        for (int i = 0; i < 1000; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g2d.fillRect(x, y, 1, 1);
        }
        
        // Add finger number indicator
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("Finger: " + finger, 10, 30);
        g2d.drawString("SIMULATED", 10, 60);
        
        g2d.dispose();
        
        // Convert to BMP byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "BMP", baos);
        return baos.toByteArray();
    }
    
    @Override
    public String getDeviceInfo() {
        if (!initialized) {
            return "Generic USB Scanner (Not initialized)";
        }
        return deviceModel;
    }
    
    @Override
    public MultipleFingerCaptureResult captureMultiple(int[] fingers, int timeout) {
        log.info("Generic USB multiple capture for fingers {} with timeout {}ms", java.util.Arrays.toString(fingers), timeout);
        
        if (!initialized) {
            return MultipleFingerCaptureResult.multiBuilder()
                    .success(false)
                    .statusCode(-1)
                    .statusMessage("Scanner not initialized")
                    .fingers(fingers)
                    .build();
        }
        
        // For now, implement multiple capture by calling single capture for each finger
        // This is a simplified implementation - real implementation would use SDK's multiple capture
        try {
            // Use first finger for capture (simplified approach)
            int primaryFinger = fingers.length > 0 ? fingers[0] : 1;
            
            // Capture using the existing single finger method
            CaptureResult singleResult = capture(primaryFinger, timeout);
            
            // Convert to MultipleFingerCaptureResult
            return MultipleFingerCaptureResult.multiBuilder()
                    .success(singleResult.isSuccess())
                    .statusCode(singleResult.getStatusCode())
                    .statusMessage(singleResult.getStatusMessage())
                    .fingers(fingers)
                    .imageData(singleResult.getImageData())
                    .imageFormat(singleResult.getImageFormat())
                    .quality(singleResult.getQuality())
                    .width(singleResult.getWidth())
                    .height(singleResult.getHeight())
                    .resolution(singleResult.getResolution())
                    .captureTimeMs(singleResult.getCaptureTimeMs())
                    .build();
                    
        } catch (Exception e) {
            log.error("Generic USB multiple capture failed", e);
            return MultipleFingerCaptureResult.multiBuilder()
                    .success(false)
                    .statusCode(-2)
                    .statusMessage("Multiple capture failed: " + e.getMessage())
                    .fingers(fingers)
                    .build();
        }
    }
    
    @Override
    public void release() {
        if (initialized) {
            log.info("Releasing Generic USB Scanner resources");
            initialized = false;
        }
    }
    
    @Override
    public String getScannerType() {
        return "GENERIC_USB";
    }
    
    private String getFingerName(int finger) {
        return switch (finger) {
            case 1 -> "Right Thumb";
            case 2 -> "Right Index";
            case 3 -> "Right Middle";
            case 4 -> "Right Ring";
            case 5 -> "Right Little";
            case 6 -> "Left Thumb";
            case 7 -> "Left Index";
            case 8 -> "Left Middle";
            case 9 -> "Left Ring";
            case 10 -> "Left Little";
            default -> "Unknown Finger " + finger;
        };
    }
}
