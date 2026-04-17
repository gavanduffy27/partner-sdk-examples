package com.genkey.fingerprint.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket handler for streaming live scanner preview images to connected clients.
 */
@Slf4j
@Component
public class ScannerPreviewWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final Object broadcastLock = new Object();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket client connected: {} (Total: {})", session.getId(), sessions.size());
        
        // Send welcome message
        session.sendMessage(new TextMessage("{\"type\":\"connected\",\"message\":\"Scanner preview stream connected\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket client disconnected: {} (Total: {})", session.getId(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages from client (e.g., commands)
        String payload = message.getPayload();
        log.debug("Received message from {}: {}", session.getId(), payload);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session);
    }

    /**
     * Broadcast a preview frame to all connected clients.
     * @param imageData Raw grayscale image data
     * @param width Image width
     * @param height Image height
     */
    public void broadcastPreviewFrame(byte[] imageData, int width, int height) {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            // Convert raw grayscale to a simple PNG or send as base64
            // For simplicity, we'll convert to BMP and send as base64
            byte[] bmpData = convertRawToBmp(imageData, width, height);
            String base64Image = Base64.getEncoder().encodeToString(bmpData);
            
            String jsonMessage = String.format(
                "{\"type\":\"preview\",\"width\":%d,\"height\":%d,\"image\":\"%s\"}",
                width, height, base64Image
            );
            
            TextMessage message = new TextMessage(jsonMessage);
            
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.error("Failed to send preview to session {}: {}", session.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error broadcasting preview frame: {}", e.getMessage());
        }
    }

    /**
     * Broadcast scanning started event
     */
    public void broadcastScanningStarted(int finger, String fingerName) {
        broadcastEvent("scanning_started", finger, fingerName, null);
    }

    /**
     * Broadcast scanning status update
     */
    public void broadcastStatus(String status) {
        String jsonMessage = String.format("{\"type\":\"status\",\"message\":\"%s\"}", status);
        broadcastText(jsonMessage);
    }

    /**
     * Broadcast capture success
     */
    public void broadcastCaptureSuccess(int finger, String fingerName, byte[] imageData, int width, int height) {
        try {
            byte[] bmpData;
            // Check if data is already in BMP format (starts with "BM")
            if (imageData != null && imageData.length > 2 && imageData[0] == 66 && imageData[1] == 77) {
                // Already BMP format, use as-is
                bmpData = imageData;
                log.debug("Image data is already in BMP format, skipping conversion");
            } else {
                // Raw pixel data, convert to BMP
                bmpData = convertRawToBmp(imageData, width, height);
                log.debug("Converted raw pixel data to BMP format");
            }
            String base64Image = Base64.getEncoder().encodeToString(bmpData);
            
            String jsonMessage = String.format(
                "{\"type\":\"capture_success\",\"finger\":%d,\"fingerName\":\"%s\",\"width\":%d,\"height\":%d,\"image\":\"%s\"}",
                finger, fingerName, width, height, base64Image
            );
            broadcastText(jsonMessage);
        } catch (Exception e) {
            log.error("Error broadcasting capture success: {}", e.getMessage());
        }
    }

    /**
     * Broadcast capture failed event
     */
    public void broadcastCaptureFailed(int finger, String errorMessage) {
        String jsonMessage = String.format(
            "{\"type\":\"capture_failed\",\"finger\":%d,\"error\":\"%s\"}",
            finger, errorMessage != null ? errorMessage.replace("\"", "'") : "Unknown error"
        );
        broadcastText(jsonMessage);
    }

    /**
     * Broadcast scanning ended event
     */
    public void broadcastScanningEnded() {
        broadcastText("{\"type\":\"scanning_ended\"}");
    }

    private void broadcastEvent(String type, int finger, String fingerName, String extra) {
        String jsonMessage = String.format(
            "{\"type\":\"%s\",\"finger\":%d,\"fingerName\":\"%s\"%s}",
            type, finger, fingerName, extra != null ? "," + extra : ""
        );
        broadcastText(jsonMessage);
    }

    private void broadcastText(String message) {
        if (sessions.isEmpty()) {
            return;
        }
        
        synchronized (broadcastLock) {
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("Failed to send message to session {}: {}", session.getId(), e.getMessage());
                    } catch (IllegalStateException e) {
                        log.warn("WebSocket session in invalid state: {}", e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Convert raw grayscale image to BMP format
     */
    private byte[] convertRawToBmp(byte[] rawData, int width, int height) {
        // BMP header for 8-bit grayscale
        int rowSize = ((width + 3) / 4) * 4; // Row size must be multiple of 4
        int imageSize = rowSize * height;
        int fileSize = 54 + 1024 + imageSize; // Header + palette + data
        int dataOffset = 1078; // Header (54) + palette (1024)
        
        byte[] bmp = new byte[fileSize];
        int offset = 0;
        
        // BMP File Header (14 bytes)
        bmp[offset++] = 'B'; bmp[offset++] = 'M';
        writeInt(bmp, offset, fileSize); offset += 4;
        writeInt(bmp, offset, 0); offset += 4; // Reserved
        writeInt(bmp, offset, dataOffset); offset += 4;
        
        // DIB Header (40 bytes)
        writeInt(bmp, offset, 40); offset += 4;
        writeInt(bmp, offset, width); offset += 4;
        writeInt(bmp, offset, height); offset += 4;
        writeShort(bmp, offset, 1); offset += 2; // Planes
        writeShort(bmp, offset, 8); offset += 2; // Bits per pixel
        writeInt(bmp, offset, 0); offset += 4; // Compression
        writeInt(bmp, offset, imageSize); offset += 4;
        writeInt(bmp, offset, 2835); offset += 4; // X pixels per meter
        writeInt(bmp, offset, 2835); offset += 4; // Y pixels per meter
        writeInt(bmp, offset, 256); offset += 4; // Colors used
        writeInt(bmp, offset, 256); offset += 4; // Important colors
        
        // Color palette (grayscale)
        for (int i = 0; i < 256; i++) {
            bmp[offset++] = (byte) i; // Blue
            bmp[offset++] = (byte) i; // Green
            bmp[offset++] = (byte) i; // Red
            bmp[offset++] = 0;        // Reserved
        }
        
        // Pixel data (BMP stores bottom-to-top)
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int srcIndex = y * width + x;
                if (srcIndex < rawData.length) {
                    bmp[offset + x] = rawData[srcIndex];
                }
            }
            offset += rowSize;
        }
        
        return bmp;
    }

    private void writeInt(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private void writeShort(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    /**
     * Get the number of connected clients
     */
    public int getConnectedClientCount() {
        return sessions.size();
    }
}
