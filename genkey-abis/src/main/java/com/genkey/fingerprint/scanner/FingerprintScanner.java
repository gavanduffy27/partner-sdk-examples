package com.genkey.fingerprint.scanner;

import com.genkey.fingerprint.model.CaptureResult;
import com.genkey.fingerprint.model.MultipleFingerCaptureResult;

/**
 * Interface for fingerprint scanner implementations.
 * Implement this interface for specific scanner hardware (SecuGen, Futronic, etc.)
 */
public interface FingerprintScanner {
    
    /**
     * Initialize the scanner hardware
     * @return true if initialization successful
     */
    boolean initialize();
    
    /**
     * Check if scanner is connected and ready
     * @return true if scanner is ready
     */
    boolean isReady();
    
    /**
     * Capture a single fingerprint image
     * @param finger finger position (1-10)
     * @param timeout timeout in milliseconds
     * @return CaptureResult containing image data and status
     */
    CaptureResult capture(int finger, int timeout);
    
    /**
     * Capture multiple fingerprints in a single image
     * @param fingers array of finger positions (1-10)
     * @param timeout timeout in milliseconds
     * @return MultipleFingerCaptureResult containing image data and status
     */
    MultipleFingerCaptureResult captureMultiple(int[] fingers, int timeout);
    
    /**
     * Get scanner device information
     * @return device info string
     */
    String getDeviceInfo();
    
    /**
     * Release scanner resources
     */
    void release();
    
    /**
     * Get the scanner type
     * @return scanner type name
     */
    String getScannerType();
}
