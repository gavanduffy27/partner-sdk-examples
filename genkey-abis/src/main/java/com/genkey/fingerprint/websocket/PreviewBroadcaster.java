package com.genkey.fingerprint.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Singleton holder for the ScannerPreviewWebSocketHandler.
 * This allows the scanner to access the WebSocket handler without Spring DI.
 */
@Slf4j
@Component
public class PreviewBroadcaster {

    private static ScannerPreviewWebSocketHandler handler;

    public PreviewBroadcaster(ScannerPreviewWebSocketHandler handler) {
        PreviewBroadcaster.handler = handler;
        log.info("PreviewBroadcaster initialized with WebSocket handler");
    }

    /**
     * Get the WebSocket handler (may be null if not initialized)
     */
    public static ScannerPreviewWebSocketHandler getHandler() {
        return handler;
    }

    /**
     * Broadcast a preview frame to all connected WebSocket clients
     */
    public static void broadcastPreview(byte[] imageData, int width, int height) {
        if (handler != null) {
            handler.broadcastPreviewFrame(imageData, width, height);
        }
    }

    /**
     * Broadcast scanning started event
     */
    public static void broadcastScanningStarted(int finger, String fingerName) {
        if (handler != null) {
            handler.broadcastScanningStarted(finger, fingerName);
        }
    }

    /**
     * Broadcast status update
     */
    public static void broadcastStatus(String status) {
        if (handler != null) {
            handler.broadcastStatus(status);
        }
    }

    /**
     * Broadcast capture success
     */
    public static void broadcastCaptureSuccess(int finger, String fingerName, byte[] imageData, int width, int height) {
        if (handler != null) {
            handler.broadcastCaptureSuccess(finger, fingerName, imageData, width, height);
        }
    }

    /**
     * Broadcast capture failed
     */
    public static void broadcastCaptureFailed(int finger, String errorMessage) {
        if (handler != null) {
            handler.broadcastCaptureFailed(finger, errorMessage);
        }
    }

    /**
     * Broadcast scanning ended
     */
    public static void broadcastScanningEnded() {
        if (handler != null) {
            handler.broadcastScanningEnded();
        }
    }
}
