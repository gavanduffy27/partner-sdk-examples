package com.genkey.fingerprint.scanner;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A preview window that displays live fingerprint images from the scanner.
 */
@Slf4j
public class FingerprintPreviewWindow extends JFrame {
    
    private final JLabel imageLabel;
    private final JLabel statusLabel;
    private final JLabel instructionLabel;
    private BufferedImage currentImage;
    
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 500;
    
    public FingerprintPreviewWindow() {
        super("Fingerprint Capture");
        
        // Set up the frame
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null); // Center on screen
        setAlwaysOnTop(true);
        setResizable(false);
        
        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.DARK_GRAY);
        
        // Instruction label at top
        instructionLabel = new JLabel("PLACE FINGER ON SCANNER", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionLabel.setForeground(Color.YELLOW);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(instructionLabel, BorderLayout.NORTH);
        
        // Image display panel in center
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.BLACK);
        imagePanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(350, 400));
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        
        mainPanel.add(imagePanel, BorderLayout.CENTER);
        
        // Status label at bottom
        statusLabel = new JLabel("Waiting for finger...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * Update the preview image from raw grayscale data
     */
    public void updatePreview(byte[] imageData, int width, int height) {
        if (imageData == null || width <= 0 || height <= 0) {
            return;
        }
        
        try {
            // Create BufferedImage from raw grayscale data
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            
            // Copy the raw data to the image
            byte[] imageBuffer = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(imageData, 0, imageBuffer, 0, Math.min(imageData.length, imageBuffer.length));
            
            // Scale image to fit the label
            int displayWidth = 350;
            int displayHeight = (int) ((double) height / width * displayWidth);
            if (displayHeight > 400) {
                displayHeight = 400;
                displayWidth = (int) ((double) width / height * displayHeight);
            }
            
            Image scaledImage = image.getScaledInstance(displayWidth, displayHeight, Image.SCALE_FAST);
            
            // Update on EDT
            SwingUtilities.invokeLater(() -> {
                imageLabel.setIcon(new ImageIcon(scaledImage));
                currentImage = image;
            });
            
        } catch (Exception e) {
            log.error("Failed to update preview image", e);
        }
    }
    
    /**
     * Update status message
     */
    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }
    
    /**
     * Update instruction message
     */
    public void setInstruction(String instruction) {
        SwingUtilities.invokeLater(() -> {
            instructionLabel.setText(instruction);
        });
    }
    
    /**
     * Show capture success
     */
    public void showCaptureSuccess() {
        SwingUtilities.invokeLater(() -> {
            instructionLabel.setText("CAPTURE SUCCESSFUL!");
            instructionLabel.setForeground(Color.GREEN);
            statusLabel.setText("Fingerprint captured successfully");
        });
    }
    
    /**
     * Show the window
     */
    public void showWindow() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            toFront();
            requestFocus();
        });
    }
    
    /**
     * Hide the window
     */
    public void hideWindow() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
        });
    }
    
    /**
     * Close and dispose the window
     */
    public void closeWindow() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }
    
    /**
     * Reset the window for a new capture
     */
    public void reset() {
        SwingUtilities.invokeLater(() -> {
            imageLabel.setIcon(null);
            instructionLabel.setText("PLACE FINGER ON SCANNER");
            instructionLabel.setForeground(Color.YELLOW);
            statusLabel.setText("Waiting for finger...");
            currentImage = null;
        });
    }
}
