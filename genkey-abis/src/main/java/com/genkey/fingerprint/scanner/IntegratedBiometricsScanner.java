package com.genkey.fingerprint.scanner;

import com.genkey.fingerprint.config.ScannerConfig;
import com.genkey.fingerprint.model.CaptureResult;
import com.genkey.fingerprint.model.MultipleFingerCaptureResult;
import com.genkey.fingerprint.websocket.PreviewBroadcaster;
import lombok.extern.slf4j.Slf4j;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Integrated Biometrics Scanner Implementation.
 * 
 * This implementation interfaces with the IBScanUltimate SDK.
 * SDK Location: C:\Program Files\Integrated Biometrics\IBScanUltimateSDK_x64\Bin
 * 
 * Requirements:
 * - IBScanUltimate SDK installed
 * - Scanner device connected via USB
 * - IBScanUltimate.jar in the SDK Bin directory
 */
@Slf4j
public class IntegratedBiometricsScanner implements FingerprintScanner {
    
    private final ScannerConfig config;
    private boolean initialized = false;
    private String deviceModel = "Integrated Biometrics Scanner";
    
    // SDK paths
    private static final String SDK_BASE_PATH = "C:\\Program Files\\Integrated Biometrics\\IBScanUltimateSDK_x64";
    private static final String SDK_BIN_PATH = SDK_BASE_PATH + "\\Bin";
    private static final String SDK_JAR_PATH = SDK_BIN_PATH + "\\IBScanUltimate.jar";
    private static final String SDK_COMMON_JAR_PATH = SDK_BIN_PATH + "\\IBScanCommon.jar";
    
    // SDK objects (loaded dynamically)
    private Object ibScan;
    private Object ibDevice;
    private ClassLoader sdkClassLoader;
    private Object[] lastSplitImages; // Store split images from last capture
    private Object[] lastSegmentPositions; // Store segment positions for cropping
    private Object lastFullImageData; // Store full unsegmented ImageData from last capture
    
    public IntegratedBiometricsScanner(ScannerConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean initialize() {
        log.info("Initializing Integrated Biometrics Scanner...");
        log.info("SDK Path: {}", SDK_BIN_PATH);
        
        try {
            // Check if SDK directory exists
            File sdkDir = new File(SDK_BIN_PATH);
            if (!sdkDir.exists()) {
                log.error("IBScanUltimate SDK directory not found at: {}", SDK_BIN_PATH);
                log.error("Please install the IBScanUltimate SDK");
                return false;
            }
            
            // Check if SDK JAR exists
            File sdkJar = new File(SDK_JAR_PATH);
            if (!sdkJar.exists()) {
                log.error("IBScanUltimate.jar not found at: {}", SDK_JAR_PATH);
                log.error("Please verify SDK installation");
                return false;
            }
            
            log.info("Found IBScanUltimate SDK at: {}", SDK_BIN_PATH);
            
            // Load the SDK JAR dynamically
            try {
                URL jarUrl = sdkJar.toURI().toURL();
                
                // Also check for IBScanCommon.jar
                File commonJar = new File(SDK_COMMON_JAR_PATH);
                URL[] urls;
                if (commonJar.exists()) {
                    log.info("Found IBScanCommon.jar at: {}", SDK_COMMON_JAR_PATH);
                    URL commonJarUrl = commonJar.toURI().toURL();
                    urls = new URL[]{jarUrl, commonJarUrl};
                } else {
                    log.warn("IBScanCommon.jar not found at: {}, proceeding with only IBScanUltimate.jar", SDK_COMMON_JAR_PATH);
                    urls = new URL[]{jarUrl};
                }
                
                sdkClassLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
                
                // Set context classloader for the current thread to sdkClassLoader to help JNI/ServiceLoader
                ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(sdkClassLoader);
                
                try {
                    // Load IBScan class
                    Class<?> ibScanClass = sdkClassLoader.loadClass("com.integratedbiometrics.ibscanultimate.IBScan");
                    
                    // Get singleton instance
                    Method getInstanceMethod = ibScanClass.getMethod("getInstance");
                    ibScan = getInstanceMethod.invoke(null);
                    
                    log.info("IBScan SDK loaded successfully");
                    
                    // Initialize the SDK
                    try {
                        Method initMethod = ibScanClass.getMethod("init");
                        initMethod.invoke(ibScan);
                        log.info("IBScan.init() called successfully");
                    } catch (NoSuchMethodException e) {
                        log.warn("Method init() not found. This might be normal for this version of SDK.");
                    }
                    
                    // Get device count
                    Method getDeviceCountMethod = ibScanClass.getMethod("getDeviceCount");
                    int deviceCount = (int) getDeviceCountMethod.invoke(ibScan);
                    
                    log.info("Found {} IB scanner device(s)", deviceCount);
                    
                    if (deviceCount == 0) {
                        log.warn("No IB scanner devices detected");
                        log.warn("Please ensure scanner is connected and drivers are installed");
                        // Don't fail immediately, maybe it connects later? But for now let's just warn.
                    } else {
                        // Open the first device
                        Method openDeviceMethod = ibScanClass.getMethod("openDevice", int.class);
                        ibDevice = openDeviceMethod.invoke(ibScan, 0); // Open index 0 normally, ignoring config.getDeviceIndex() for simplicity first
                        
                        // Get device description if possible (reflection on IBDevice)
                        try {
                            // IBDevice is an interface or class in the separate classloader
                            // We can't cast it to a local type easily unless we share interface.
                            // But we can invoke methods on it via reflection.
                            Method getPropertyMethod = ibDevice.getClass().getMethod("getProperty", String.class);
                            // Need access to IBScanDevice.PropertyEnum or equivalent strings if they are constants
                            // For now, assuming getProperty takes a string or enum. 
                            // The sample code uses property constants.
                        } catch (Exception e) {
                            log.warn("Could not retrieve device properties: {}", e.getMessage());
                        }

                        deviceModel = "Integrated Biometrics Scanner (Index 0)";
                        log.info("Opened device: {}", deviceModel);
                    }
                    
                    initialized = true;
                    return true;
                    
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }
                
            } catch (ClassNotFoundException e) {
                log.error("Failed to load IBScan SDK classes. Ensure IBScanUltimate.jar is in: {}", SDK_JAR_PATH);
                log.error("Error: {}", e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Failed to initialize Integrated Biometrics scanner", e);
            return false;
        }
    }
    
    @Override
    public boolean isReady() {
        return initialized && ibDevice != null;
    }
    
    @Override
    public CaptureResult capture(int finger, int timeout) {
        return capture(finger, timeout, false);
    }
    
    private CaptureResult capture(int finger, int timeout, boolean isMultipleFingerCapture) {
        if (!initialized || ibDevice == null) {
            return CaptureResult.builder()
                    .success(false)
                    .statusCode(-1)
                    .statusMessage("Scanner not initialized or device not opened")
                    .finger(finger)
                    .build();
        }
        
        long startTime = System.currentTimeMillis();
        
        log.info("==========================================");
        log.info("  INTEGRATED BIOMETRICS CAPTURE");
        log.info("  Finger: {} ({})", finger, getFingerName(finger));
        log.info("  Timeout: {} ms", timeout);
        log.info("  *** PLACE FINGER ON SCANNER ***");
        log.info("==========================================");
        
        // Broadcast scanning started to WebSocket clients
        PreviewBroadcaster.broadcastScanningStarted(finger, getFingerName(finger));
        
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(sdkClassLoader);
        
        final Object[] resultHolder = new Object[1]; // [0] = capturedImage
        final Object lock = new Object();
        
        Object listenerProxy = null;
        
        // Create and show preview window (optional - may fail in headless environment)
        FingerprintPreviewWindow previewWindow = null;
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                previewWindow = new FingerprintPreviewWindow();
                // Check if this is a multiple finger capture by checking the finger value
                // For multiple capture, finger might be -1 or a special value
                String instruction = "PLACE " + getFingerName(finger).toUpperCase() + " ON SCANNER";
                if (finger <= 0) {
                    instruction = "PLACE MULTIPLE FINGERS ON SCANNER (4-4-2 CONFIGURATION)";
                }
                previewWindow.setInstruction(instruction);
                previewWindow.showWindow();
                log.info("Preview window opened with instruction: {}", instruction);
            } else {
                log.info("Running in headless mode - preview window disabled");
            }
        } catch (Exception e) {
            log.warn("Could not create preview window: {}. Continuing without preview.", e.getMessage());
            previewWindow = null;
        }
        
        final FingerprintPreviewWindow finalPreviewWindow = previewWindow;
        
        try {
            // Prepare Types
            Class<?> imageTypeClass;
            try {
                imageTypeClass = sdkClassLoader.loadClass("com.integratedbiometrics.ibscanultimate.IBScanDevice$ImageType");
            } catch (ClassNotFoundException e) {
                log.warn("IBScanDevice$ImageType not found, trying IBScan$ImageType");
                imageTypeClass = sdkClassLoader.loadClass("com.integratedbiometrics.ibscanultimate.IBScan$ImageType");
            }

            Class<?> imageResolutionClass;
            try {
                 imageResolutionClass = sdkClassLoader.loadClass("com.integratedbiometrics.ibscanultimate.IBScanDevice$ImageResolution");
            } catch (ClassNotFoundException e) {
                 imageResolutionClass = sdkClassLoader.loadClass("com.integratedbiometrics.ibscanultimate.IBScan$ImageResolution");
            }

            Class<?> listenerInterface = sdkClassLoader.loadClass("com.integratedbiometrics.ibscanultimate.IBScanDeviceListener");
            
            // Create Listener Proxy
            listenerProxy = java.lang.reflect.Proxy.newProxyInstance(
                sdkClassLoader,
                new Class<?>[]{listenerInterface},
                new java.lang.reflect.InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        
                        if (name.equals("deviceImageResultAvailable") || name.equals("deviceImageResultExtendedAvailable")) {
                            log.info("Callback received {}! Args count: {}", name, args != null ? args.length : 0);
                            // Log all arguments to understand the callback structure
                            if (args != null) {
                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] != null) {
                                        log.info("  Arg[{}]: {} - {}", i, args[i].getClass().getName(), args[i]);
                                    } else {
                                        log.info("  Arg[{}]: null", i);
                                    }
                                }
                            }
                            // For deviceImageResultAvailable: (IBScanDevice device, ImageData image, ImageType imageType, ImageData[] splitImageArray)
                            // For deviceImageResultExtendedAvailable: (IBScanDevice device, IBScanException exception, ImageData image, ImageType imageType, int fingerCount, ImageData[] splitImageArray, SegmentPosition[] positions)
                            if (args != null && args.length > 1) {
                                Object imageArg = null;
                                // For multiple finger capture, try to use split image array which contains individual finger images
                                Object[] splitImages = null;
                                int splitImageIndex = 3; // Default for regular callback
                                
                                if (name.equals("deviceImageResultExtendedAvailable") && args.length > 5) {
                                    splitImages = (Object[]) args[5]; // ImageData[] is at index 5 for extended callback
                                    imageArg = args[2]; // ImageData is at index 2 for extended callback
                                    // Store the full unsegmented ImageData for ABIS SDK segmentation
                                    lastFullImageData = args[2];
                                    log.info("Stored full unsegmented ImageData for ABIS SDK segmentation");
                                    // Store segment positions for cropping
                                    if (args.length > 6) {
                                        lastSegmentPositions = (Object[]) args[6]; // SegmentPosition[] is at index 6
                                        log.info("Stored {} segment positions for cropping", lastSegmentPositions != null ? lastSegmentPositions.length : 0);
                                    }
                                } else if (args.length > 3) {
                                    splitImages = (Object[]) args[3]; // ImageData[] is at index 3 for regular callback
                                    imageArg = args[1]; // ImageData is at index 1 for regular callback
                                    // Store the full unsegmented ImageData for ABIS SDK segmentation
                                    lastFullImageData = args[1];
                                    log.info("Stored full unsegmented ImageData for ABIS SDK segmentation");
                                } else {
                                    imageArg = args[1]; // Fallback to full image
                                    lastFullImageData = args[1];
                                }
                                
                                // If split images are available and this is a multiple finger capture, use the first split image
                                if (splitImages != null && splitImages.length > 0 && isMultipleFingerCapture) {
                                    log.info("Using split image array for multiple finger capture ({} images available)", splitImages.length);
                                    lastSplitImages = splitImages; // Store all split images for later use
                                    
                                    // Save the full unsegmented image (the main image before segmentation)
                                    try {
                                        Object fullImage = (name.equals("deviceImageResultExtendedAvailable") && args.length > 2) 
                                                ? args[2] 
                                                : (args.length > 1 ? args[1] : null);
                                        
                                        if (fullImage != null && fullImage.getClass().getName().contains("ImageData")) {
                                            byte[] bmpData = extractBmpImageData(fullImage);
                                            saveImageToFile(bmpData, "full_capture", splitImages.length);
                                        }
                                    } catch (Exception e) {
                                        log.error("Failed to save full unsegmented image: {}", e.getMessage());
                                    }
                                    
                                    imageArg = splitImages[0]; // Use first split image for now
                                } else {
                                    lastSplitImages = null; // Clear split images if not available
                                }
                                
                                // Verify the image is not null and is an ImageData object
                                if (imageArg != null && imageArg.getClass().getName().contains("ImageData")) {
                                    // IMMEDIATELY set the result and notify - this is critical for race condition
                                    synchronized (lock) {
                                        if (resultHolder[0] == null) { // Only set if not already set
                                            log.info("Setting result from {} callback", name);
                                            resultHolder[0] = imageArg;
                                        }
                                        lock.notifyAll();
                                    }
                                    
                                    // Now do the non-critical UI updates (after result is already set)
                                    log.debug("Image received from callback - updating UI");
                                    
                                    // Show success on preview window
                                    if (finalPreviewWindow != null) {
                                        finalPreviewWindow.showCaptureSuccess();
                                    }
                                    
                                    // Broadcast capture success to WebSocket clients
                                    try {
                                        byte[] capturedBuffer = null;
                                        int capturedWidth = 0;
                                        int capturedHeight = 0;
                                        
                                        try {
                                            java.lang.reflect.Method toImageMethod = imageArg.getClass().getMethod("toImage");
                                            java.awt.image.BufferedImage bufferedImage = (java.awt.image.BufferedImage) toImageMethod.invoke(imageArg);
                                            
                                            if (bufferedImage != null) {
                                                capturedWidth = bufferedImage.getWidth();
                                                capturedHeight = bufferedImage.getHeight();
                                                
                                                // Convert BufferedImage to BMP for browser display
                                                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                                                javax.imageio.ImageIO.write(bufferedImage, "bmp", baos);
                                                capturedBuffer = baos.toByteArray();
                                            }
                                        } catch (Exception e) {
                                            log.warn("Preview broadcast: Failed to convert ImageData: {}", e.getMessage());
                                        }
                                        
                                        if (capturedBuffer != null && capturedBuffer.length > 0) {
                                            PreviewBroadcaster.broadcastCaptureSuccess(finger, getFingerName(finger), capturedBuffer, capturedWidth, capturedHeight);
                                        }
                                    } catch (Exception ex) {
                                        log.warn("Could not broadcast capture success: {}", ex.getMessage());
                                    }
                                } else {
                                    log.warn("Callback {} received but args[1] (image) is null", name);
                                }
                            }
                        } else if (name.equals("deviceAcquisitionCompleted")) {
                             log.info("Callback: deviceAcquisitionCompleted - finger capture completed");
                             if (finalPreviewWindow != null) {
                                 finalPreviewWindow.setStatus("Acquisition completed - processing...");
                             }
                             PreviewBroadcaster.broadcastStatus("Acquisition completed - processing...");
                        } else if (name.equals("deviceImagePreviewAvailable")) {
                             // Preview frames - update the preview window and broadcast to WebSocket clients
                             log.debug("=== PREVIEW FRAME RECEIVED ===");
                             if (args != null && args.length > 1) {
                                 log.debug("Preview args count: {}, attempting to extract image data", args.length);
                                 try {
                                     Object previewImageData = args[1]; // ImageData object
                                      if (previewImageData != null) {
                                         log.debug("Preview ImageData class: {}", previewImageData.getClass().getName());
                                         
                                         // Try to get raw buffer from SDK (original format that browser expects)
                                         byte[] buffer = null;
                                         int width = 0;
                                         int height = 0;
                                         
                                         try {
                                             // Try direct field access first (original approach)
                                             buffer = (byte[]) previewImageData.getClass().getField("buffer").get(previewImageData);
                                             width = previewImageData.getClass().getField("width").getInt(previewImageData);
                                             height = previewImageData.getClass().getField("height").getInt(previewImageData);
                                         } catch (NoSuchFieldException e) {
                                             // Fallback: use toImage() and extract raw data
                                             java.lang.reflect.Method toImageMethod = previewImageData.getClass().getMethod("toImage");
                                             java.awt.image.BufferedImage bufferedImage = (java.awt.image.BufferedImage) toImageMethod.invoke(previewImageData);
                                             
                                             if (bufferedImage != null) {
                                                 width = bufferedImage.getWidth();
                                                 height = bufferedImage.getHeight();
                                                 
                                                 // Extract raw pixel data in original format
                                                 int[] pixels = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
                                                 buffer = new byte[pixels.length * 3]; // RGB only (no alpha)
                                                 int index = 0;
                                                 for (int pixel : pixels) {
                                                     buffer[index++] = (byte) (pixel & 0xFF);         // B
                                                     buffer[index++] = (byte) ((pixel >> 8) & 0xFF);  // G
                                                     buffer[index++] = (byte) ((pixel >> 16) & 0xFF); // R
                                                 }
                                             }
                                         }
                                         
                                         log.debug("Preview frame: {}x{}, buffer size: {}", width, height, buffer != null ? buffer.length : 0);
                                         
                                         // Update local preview window
                                         if (finalPreviewWindow != null) {
                                             finalPreviewWindow.updatePreview(buffer, width, height);
                                             finalPreviewWindow.setStatus("Scanning... Keep finger steady");
                                         }
                                         
                                         // Broadcast to WebSocket clients for web UI
                                         if (buffer != null && buffer.length > 0) {
                                             log.debug("Broadcasting preview frame to WebSocket clients");
                                             PreviewBroadcaster.broadcastPreview(buffer, width, height);
                                         }
                                     } else {
                                         log.warn("Preview ImageData is null");
                                     }
                                 } catch (Exception e) {
                                     log.warn("Preview frame error: {}", e.getMessage(), e);
                                 }
                             } else {
                                 log.warn("Preview args is null or empty: {}", args != null ? args.length : "null");
                             }
                        } else if (name.equals("deviceFingerCountChanged")) {
                             log.info("Callback: deviceFingerCountChanged");
                             if (finalPreviewWindow != null) {
                                 finalPreviewWindow.setStatus("Finger detected");
                             }
                             PreviewBroadcaster.broadcastStatus("Finger detected");
                        }
                        return null;
                    }
                }
            );
            
            // Set Listener
            Method setScanDeviceListenerMethod = ibDevice.getClass().getMethod("setScanDeviceListener", listenerInterface);
            setScanDeviceListenerMethod.invoke(ibDevice, listenerProxy);
            
            log.info("Capture started... waiting for finger");

            // Ensure previous capture is cancelled
            try {
                Method cancelCaptureImageMethod = ibDevice.getClass().getMethod("cancelCaptureImage");
                cancelCaptureImageMethod.invoke(ibDevice);
            } catch (Exception e) {
                // Ignore
            }
            
            // Enable live preview images
            try {
                // Try to find and set the ENABLE_POWER_SAVE_MODE property to false for better preview
                // Also set preview image property if available
                Class<?> propertyIdClass = null;
                try {
                    propertyIdClass = sdkClassLoader.loadClass("com.integratedbiometrics.ibscanultimate.IBScanDevice$PropertyId");
                } catch (ClassNotFoundException e) {
                    log.debug("PropertyId class not found, skipping preview property setting");
                }
                
                if (propertyIdClass != null) {
                    // Try to enable preview callback by setting relevant properties
                    try {
                        Method setPropertyMethod = ibDevice.getClass().getMethod("setProperty", propertyIdClass, String.class);
                        
                        // Try ENABLE_POWER_SAVE_MODE = false
                        try {
                            Object powerSaveMode = Enum.valueOf((Class<Enum>)propertyIdClass, "ENABLE_POWER_SAVE_MODE");
                            setPropertyMethod.invoke(ibDevice, powerSaveMode, "FALSE");
                            log.info("Disabled power save mode for better preview");
                        } catch (Exception e) {
                            log.debug("Could not set ENABLE_POWER_SAVE_MODE: {}", e.getMessage());
                        }
                        
                        // Try to enable preview
                        try {
                            Object previewImage = Enum.valueOf((Class<Enum>)propertyIdClass, "PREVIEW_IMAGE");
                            setPropertyMethod.invoke(ibDevice, previewImage, "TRUE");
                            log.info("Enabled preview image callback");
                        } catch (Exception e) {
                            log.debug("Could not set PREVIEW_IMAGE: {}", e.getMessage());
                        }
                    } catch (NoSuchMethodException e) {
                        log.debug("setProperty method not found: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.debug("Could not configure preview properties: {}", e.getMessage());
            }
            
            // Start Capture
            // beginCaptureImage(ImageType imageType, ImageResolution imageResolution, int captureOptions)
            Object flatImageType = Enum.valueOf((Class<Enum>)imageTypeClass, "FLAT_SINGLE_FINGER");
            Object resolution500 = Enum.valueOf((Class<Enum>)imageResolutionClass, "RESOLUTION_500");
            
            // Get Option constants dynamically
            int captureOptions;
            try {
                int autoContrast = ibDevice.getClass().getField("OPTION_AUTO_CONTRAST").getInt(null);
                int autoCapture = ibDevice.getClass().getField("OPTION_AUTO_CAPTURE").getInt(null);
                int ignoreFingerCount = ibDevice.getClass().getField("OPTION_IGNORE_FINGER_COUNT").getInt(null);
                captureOptions = autoContrast | autoCapture | ignoreFingerCount;
                log.info("Using capture options: {} (AutoContrast | AutoCapture | IgnoreFingerCount)", captureOptions);
            } catch (Exception e) {
                log.warn("Could not load OPTION constants, using default 3");
                captureOptions = 3;
            }

            // Check if capture is available
            try {
                Method isCaptureAvailableMethod = ibDevice.getClass().getMethod("isCaptureAvailable", imageTypeClass, imageResolutionClass);
                boolean available = (boolean) isCaptureAvailableMethod.invoke(ibDevice, flatImageType, resolution500);
                if (!available) {
                    log.error("Capture not available for type FLAT_SINGLE_FINGER and RESOLUTION_500");
                    return CaptureResult.builder().success(false).statusMessage("Capture mode not available").build();
                }
            } catch (Exception e) {
                 log.warn("Could not check isCaptureAvailable: {}", e.getMessage());
            }

            
            Method beginCaptureImageMethod = ibDevice.getClass().getMethod("beginCaptureImage", imageTypeClass, imageResolutionClass, int.class);
            try {
                beginCaptureImageMethod.invoke(ibDevice, flatImageType, resolution500, captureOptions);
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                boolean handled = false;
                if (cause != null) {
                     log.error("IBScan failure: {}", cause.toString());
                     // Try to get type or code if it is IBScanException
                     try {
                         Method getTypeMethod = cause.getClass().getMethod("getType");
                         Object type = getTypeMethod.invoke(cause);
                         log.error("IBScanException Type: {}", type);
                         
                         // Handle CAPTURE_STILL_RUNNING by canceling and retrying
                         if (type.toString().equals("CAPTURE_STILL_RUNNING")) {
                             log.warn("Capture is still running. Attempting to cancel and retry...");
                             Method cancelCaptureImageMethod = ibDevice.getClass().getMethod("cancelCaptureImage");
                             cancelCaptureImageMethod.invoke(ibDevice);
                             
                             // Wait a moment for cancellation to take effect
                             Thread.sleep(500);
                             
                             // Retry capture
                             log.info("Retrying capture...");
                             beginCaptureImageMethod.invoke(ibDevice, flatImageType, resolution500, captureOptions);
                             handled = true;
                         }
                     } catch (Exception ex) {
                         log.warn("Failed to handle IBScan exception recovery: {}", ex.getMessage());
                     }
                }
                if (!handled) {
                    throw e; // rethrow if not handled
                }
            }
            
            log.info("Capture started... waiting for finger");
            
            // Wait for result with multiple check cycles to handle race conditions
            synchronized (lock) {
                long waitTime = timeout;
                long startWait = System.currentTimeMillis();
                
                while (resultHolder[0] == null && waitTime > 0) {
                    try {
                        // Wait in chunks to allow callbacks to complete
                        lock.wait(Math.min(waitTime, 500));
                    } catch (InterruptedException e) {
                        log.error("Capture wait interrupted", e);
                        throw e;
                    }
                    
                    // Recalculate remaining wait time
                    long elapsed = System.currentTimeMillis() - startWait;
                    waitTime = timeout - elapsed;
                }
                
                // After normal timeout, give extra grace period for race conditions
                // The callback might be in the process of setting the result
                if (resultHolder[0] == null) {
                    log.debug("Initial wait complete, no result yet. Adding grace period...");
                    for (int i = 0; i < 5 && resultHolder[0] == null; i++) {
                        try {
                            lock.wait(200); // 5 x 200ms = up to 1 second grace period
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                        if (resultHolder[0] != null) {
                            log.info("Result received during grace period (iteration {})", i + 1);
                            break;
                        }
                    }
                }
            }
            
            Object capturedImage = resultHolder[0];
            
            if (capturedImage == null) {
                log.warn("Capture timeout - no image received");
                return CaptureResult.builder()
                        .success(false)
                        .statusCode(-3)
                        .statusMessage("Capture timeout")
                        .finger(finger)
                        .build();
            }
            
            log.info("Image received from callback");
            
            // Extract buffer from ImageData.buffer (byte[]) or toBitmapImage
            // ImageData has public byte[] buffer;
            // Or getBuffer()
            /*
            public class ImageData {
               public final byte[] buffer;
               public final int    width;
               public final int    height;
               public final double resolutionX;
               public final double resolutionY;
               public final double frameTime;
               public final int    pitch;
               public final int    bitsPerPixel;
               public final int    format;
            }
            */
            // Since it's a public field in the SDK usually, but via reflection we access fields.
            
            byte[] imageBuffer = null;
            int width = 0;
            int height = 0;
            
            log.info("Captured image class: {}", capturedImage.getClass().getName());
            log.info("Available methods: {}", 
                java.util.Arrays.toString(capturedImage.getClass().getDeclaredMethods()));
            
            // Try to get raw buffer data using various method names
            try {
                // Try toImage() method first
                java.lang.reflect.Method toImageMethod = capturedImage.getClass().getMethod("toImage");
                java.awt.image.BufferedImage bufferedImage = (java.awt.image.BufferedImage) toImageMethod.invoke(capturedImage);
                
                if (bufferedImage != null) {
                    width = bufferedImage.getWidth();
                    height = bufferedImage.getHeight();
                    
                    // Extract BMP data for ABIS to convert to WSQ
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(bufferedImage, "bmp", baos);
                    imageBuffer = baos.toByteArray();
                    
                    log.info("Successfully extracted BMP data: {}x{}, size={} bytes", width, height, imageBuffer.length);
                }
            } catch (Exception e) {
                log.error("Failed to extract BMP data: {}", e.getMessage());
                throw e;
            }
            
            log.info("Image Dimensions: {}x{}", width, height);
            
            // Return BMP data for ABIS to convert to WSQ
            long captureTime = System.currentTimeMillis() - startTime;
            
            return CaptureResult.builder()
                    .success(true)
                    .statusCode(0)
                    .statusMessage("Capture successful")
                    .finger(finger)
                    .imageData(imageBuffer)
                    .imageFormat("BMP")
                    .quality(80) // Mock quality
                    .width(width)
                    .height(height)
                    .resolution(500)
                    .captureTimeMs(captureTime)
                    .build();

        } catch (Exception e) {
            log.error("IB Scanner capture failed", e);
            return CaptureResult.builder()
                    .success(false)
                    .statusCode(-2)
                    .statusMessage("Capture failed: " + e.getMessage())
                    .finger(finger)
                    .build();
        } finally {
             // Ensure capture is cancelled in all cases (exception, timeout, success)
             if (ibDevice != null) {
                 try {
                    Method cancelCaptureImageMethod = ibDevice.getClass().getMethod("cancelCaptureImage");
                    cancelCaptureImageMethod.invoke(ibDevice);
                 } catch (Exception e) {
                    // Ignore
                 }
                 
                 // Remove listener
                 try {
                     if (listenerProxy != null) {
                         Method setScanDeviceListenerMethod = ibDevice.getClass().getMethod("setScanDeviceListener", 
                            sdkClassLoader.loadClass("com.integratedbiometrics.ibscanultimate.IBScanDeviceListener"));
                         setScanDeviceListenerMethod.invoke(ibDevice, new Object[]{null});
                    }
                 } catch (Exception e) {
                     log.error("Failed to remove listener", e);
                 }
             }
             Thread.currentThread().setContextClassLoader(originalClassLoader);
             
             // Broadcast scanning ended to WebSocket clients
             PreviewBroadcaster.broadcastScanningEnded();
             
             // Close preview window after a short delay to show the result
             if (finalPreviewWindow != null) {
                 new Thread(() -> {
                     try {
                         Thread.sleep(1500); // Show result for 1.5 seconds
                     } catch (InterruptedException e) {
                         // Ignore
                     }
                     finalPreviewWindow.closeWindow();
                 }).start();
             }
        }
    }
    
    @Override
    public MultipleFingerCaptureResult captureMultiple(int[] fingers, int timeout) {
        if (!initialized || ibDevice == null) {
            return MultipleFingerCaptureResult.multiBuilder()
                    .success(false)
                    .statusCode(-1)
                    .statusMessage("Scanner not initialized or device not opened")
                    .fingers(fingers)
                    .build();
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Use the first finger for the capture (simplified approach)
            int primaryFinger = fingers.length > 0 ? fingers[0] : 1;
            
            // Capture using the existing single finger method with multiple finger flag
            CaptureResult singleResult = capture(primaryFinger, timeout, true);
            
            // Extract the full unsegmented image for ABIS SDK to segment
            // ABIS SDK's ImageContext expects a single full multi-finger image and will do the segmentation itself
            byte[] fullRawImageData = null;
            int fullWidth = singleResult.getWidth();
            int fullHeight = singleResult.getHeight();
            try {
                // Use the stored full ImageData object from the callback
                if (lastFullImageData != null && lastFullImageData.getClass().getName().contains("ImageData")) {
                    fullRawImageData = extractRawImageData(lastFullImageData);
                    // Get dimensions from the full ImageData object
                    java.lang.reflect.Field widthField = lastFullImageData.getClass().getField("width");
                    java.lang.reflect.Field heightField = lastFullImageData.getClass().getField("height");
                    fullWidth = widthField.getInt(lastFullImageData);
                    fullHeight = heightField.getInt(lastFullImageData);
                    log.info("Extracted full unsegmented image for ABIS SDK: {}x{}, size={} bytes", fullWidth, fullHeight, fullRawImageData != null ? fullRawImageData.length : 0);
                } else {
                    log.warn("No full ImageData stored, falling back to single result image data");
                    // Fallback to the BMP data from single result (this won't work for ABIS SDK segmentation)
                    fullRawImageData = singleResult.getImageData();
                }
            } catch (Exception e) {
                log.error("Failed to extract full raw image data: {}", e.getMessage());
            }

            // Keep split images for preview broadcast only (not for ABIS SDK segmentation)
            byte[][] fingerImages = null;
            if (lastSplitImages != null && lastSplitImages.length > 0) {
                fingerImages = new byte[lastSplitImages.length][];
                for (int i = 0; i < lastSplitImages.length && i < fingers.length; i++) {
                    try {
                        Object imageData = lastSplitImages[i];
                        if (imageData != null && imageData.getClass().getName().contains("ImageData")) {
                            // Extract BMP for preview only
                            Object segmentPosition = null;
                            if (lastSegmentPositions != null && i < lastSegmentPositions.length) {
                                segmentPosition = lastSegmentPositions[i];
                            }
                            byte[] bmpData = extractBmpImageData(imageData, segmentPosition);
                            fingerImages[i] = bmpData;
                        }
                    } catch (Exception e) {
                        log.error("Error extracting BMP for preview split image {}: {}", i, e.getMessage());
                        fingerImages[i] = null;
                    }
                }
            }

            // Convert to MultipleFingerCaptureResult
            // Use the full unsegmented RAW image for ABIS SDK segmentation
            byte[] mainImageData = (fullRawImageData != null)
                    ? fullRawImageData
                    : singleResult.getImageData();
            
            MultipleFingerCaptureResult result = MultipleFingerCaptureResult.multiBuilder()
                    .success(singleResult.isSuccess())
                    .statusCode(singleResult.getStatusCode())
                    .statusMessage(singleResult.getStatusMessage())
                    .fingers(fingers)
                    .imageData(mainImageData)
                    .imageFormat("RAW") // Use RAW format for ABIS SDK segmentation
                    .quality(singleResult.getQuality())
                    .width(fullWidth) // Use full image dimensions
                    .height(fullHeight) // Use full image dimensions
                    .resolution(singleResult.getResolution())
                    .captureTimeMs(System.currentTimeMillis() - startTime)
                    .build();
            
            // Broadcast capture success for each finger in multiple finger capture
            if (singleResult.isSuccess()) {
                for (int i = 0; i < fingers.length; i++) {
                    int finger = fingers[i];
                    byte[] bmpData = null;
                    int width = singleResult.getWidth();
                    int height = singleResult.getHeight();
                    
                    if (lastSplitImages != null && i < lastSplitImages.length && lastSplitImages[i] != null) {
                        // Extract BMP data directly from split image for preview, with cropping if segment position available
                        try {
                            Object segmentPosition = null;
                            if (lastSegmentPositions != null && i < lastSegmentPositions.length) {
                                segmentPosition = lastSegmentPositions[i];
                            }
                            bmpData = extractBmpImageData(lastSplitImages[i], segmentPosition);
                            
                            // Get dimensions from the split image (after cropping)
                            java.lang.reflect.Method toImageMethod = lastSplitImages[i].getClass().getMethod("toImage");
                            java.awt.image.BufferedImage bufferedImage = (java.awt.image.BufferedImage) toImageMethod.invoke(lastSplitImages[i]);
                            if (bufferedImage != null) {
                                width = bufferedImage.getWidth();
                                height = bufferedImage.getHeight();
                            }
                        } catch (Exception e) {
                            log.error("Error extracting BMP data for split image {}: {}", i, e.getMessage());
                            bmpData = singleResult.getImageData();
                        }
                    } else {
                        // Use single result image
                        bmpData = singleResult.getImageData();
                    }
                    
                    log.info("Broadcasting capture success for finger {} (index {} of {})", finger, i+1, fingers.length);
                    PreviewBroadcaster.broadcastCaptureSuccess(finger, getFingerName(finger), 
                            bmpData, width, height);
                    
                    // Add a small delay between broadcasts to avoid WebSocket state issues
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error in captureMultiple: {}", e.getMessage(), e);
            return MultipleFingerCaptureResult.multiBuilder()
                    .success(false)
                    .statusCode(-1)
                    .statusMessage("Capture failed: " + e.getMessage())
                    .fingers(fingers)
                    .build();
        }
    }
    
    /**
     * Extract image data from ImageData object using reflection
     * Extracts BMP data for ABIS to convert to WSQ
     */
    private byte[] extractImageData(Object imageData) throws Exception {
        try {
            // Try to convert to BufferedImage first
            java.lang.reflect.Method toImageMethod = imageData.getClass().getMethod("toImage");
            java.awt.image.BufferedImage bufferedImage = (java.awt.image.BufferedImage) toImageMethod.invoke(imageData);
            
            if (bufferedImage != null) {
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();
                
                // Extract BMP data for ABIS to convert to WSQ
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(bufferedImage, "bmp", baos);
                byte[] bmpData = baos.toByteArray();
                
                log.debug("Extracted BMP data: {}x{}, size={} bytes", width, height, bmpData.length);
                return bmpData;
            }
        } catch (Exception e) {
            log.error("Failed to extract image data: {}", e.getMessage());
            throw e;
        }
        return null;
    }
    
    /**
     * Extract raw buffer data from ImageData object for ABIS SDK segmentation
     * ABIS SDK ImageContext expects raw grayscale pixel data, not BMP
     */
    private byte[] extractRawImageData(Object imageData) throws Exception {
        try {
            // Try to get raw buffer data directly from ImageData object
            java.lang.reflect.Field bufferField = imageData.getClass().getField("buffer");
            byte[] buffer = (byte[]) bufferField.get(imageData);
            
            java.lang.reflect.Field widthField = imageData.getClass().getField("width");
            int width = widthField.getInt(imageData);
            
            java.lang.reflect.Field heightField = imageData.getClass().getField("height");
            int height = heightField.getInt(imageData);
            
            log.info("Extracted raw buffer data: {}x{}, size={} bytes", width, height, buffer != null ? buffer.length : 0);
            return buffer;
        } catch (NoSuchFieldException e) {
            // Fallback: extract from BufferedImage
            log.warn("Could not find buffer field, extracting from BufferedImage");
            java.lang.reflect.Method toImageMethod = imageData.getClass().getMethod("toImage");
            java.awt.image.BufferedImage bufferedImage = (java.awt.image.BufferedImage) toImageMethod.invoke(imageData);
            
            if (bufferedImage != null) {
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();
                
                // Extract raw grayscale pixel data
                byte[] buffer = new byte[width * height];
                int index = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = bufferedImage.getRGB(x, y);
                        // Extract grayscale value (using green channel for simplicity)
                        buffer[index++] = (byte) ((rgb >> 8) & 0xFF);
                    }
                }
                
                log.info("Extracted raw data from BufferedImage: {}x{}, size={} bytes", width, height, buffer.length);
                return buffer;
            }
        } catch (Exception e) {
            log.error("Failed to extract raw image data: {}", e.getMessage());
            throw e;
        }
        return null;
    }
    
    /**
     * Extract BMP image data from ImageData object for preview broadcast
     * Returns BMP data directly to avoid dimension mismatch issues
     */
    private byte[] extractBmpImageData(Object imageData) throws Exception {
        return extractBmpImageData(imageData, null);
    }
    
    /**
     * Extract BMP image data from ImageData object with optional cropping using segment position
     */
    private byte[] extractBmpImageData(Object imageData, Object segmentPosition) throws Exception {
        try {
            java.lang.reflect.Method toImageMethod = imageData.getClass().getMethod("toImage");
            java.awt.image.BufferedImage bufferedImage = (java.awt.image.BufferedImage) toImageMethod.invoke(imageData);
            
            if (bufferedImage != null) {
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();
                
                // // If segment position is provided, crop the image to the finger region
                // if (segmentPosition != null) {
                //     try {
                //         // Log all available fields in SegmentPosition to understand its structure
                //         java.lang.reflect.Field[] fields = segmentPosition.getClass().getDeclaredFields();
                //         log.info("SegmentPosition fields: {}", java.util.Arrays.toString(fields));
                        
                //         for (java.lang.reflect.Field field : fields) {
                //             field.setAccessible(true);
                //             try {
                //                 Object value = field.get(segmentPosition);
                //                 log.info("  Field: {} = {} (type: {})", field.getName(), value, field.getType().getSimpleName());
                //             } catch (Exception e) {
                //                 log.error("  Failed to read field {}: {}", field.getName(), e.getMessage());
                //             }
                //         }
                        
                //         // Try common field names for segment position
                //         int x = 0, y = 0, segWidth = width, segHeight = height;
                //         boolean foundFields = false;
                        
                //         // Try various possible field names
                //         String[] xFieldNames = {"x", "X", "left", "Left", "offsetX", "OffsetX"};
                //         String[] yFieldNames = {"y", "Y", "top", "Top", "offsetY", "OffsetY"};
                //         String[] widthFieldNames = {"width", "Width", "w", "W"};
                //         String[] heightFieldNames = {"height", "Height", "h", "H"};
                        
                //         for (String fieldName : xFieldNames) {
                //             try {
                //                 java.lang.reflect.Field field = segmentPosition.getClass().getField(fieldName);
                //                 x = field.getInt(segmentPosition);
                //                 log.info("Found x field: {} = {}", fieldName, x);
                //                 foundFields = true;
                //                 break;
                //             } catch (NoSuchFieldException e) {
                //                 // Continue to next field name
                //             }
                //         }
                        
                //         for (String fieldName : yFieldNames) {
                //             try {
                //                 java.lang.reflect.Field field = segmentPosition.getClass().getField(fieldName);
                //                 y = field.getInt(segmentPosition);
                //                 log.info("Found y field: {} = {}", fieldName, y);
                //                 foundFields = true;
                //                 break;
                //             } catch (NoSuchFieldException e) {
                //                 // Continue to next field name
                //             }
                //         }
                        
                //         for (String fieldName : widthFieldNames) {
                //             try {
                //                 java.lang.reflect.Field field = segmentPosition.getClass().getField(fieldName);
                //                 segWidth = field.getInt(segmentPosition);
                //                 log.info("Found width field: {} = {}", fieldName, segWidth);
                //                 foundFields = true;
                //                 break;
                //             } catch (NoSuchFieldException e) {
                //                 // Continue to next field name
                //             }
                //         }
                        
                //         for (String fieldName : heightFieldNames) {
                //             try {
                //                 java.lang.reflect.Field field = segmentPosition.getClass().getField(fieldName);
                //                 segHeight = field.getInt(segmentPosition);
                //                 log.info("Found height field: {} = {}", fieldName, segHeight);
                //                 foundFields = true;
                //                 break;
                //             } catch (NoSuchFieldException e) {
                //                 // Continue to next field name
                //             }
                //         }
                        
                //         if (foundFields) {
                //             log.info("Cropping image using segment position: x={}, y={}, width={}, height={}, original={}x{}", 
                //                     x, y, segWidth, segHeight, width, height);
                            
                //             // Validate crop coordinates
                //             if (x >= 0 && y >= 0 && segWidth > 0 && segHeight > 0 && 
                //                 x + segWidth <= width && y + segHeight <= height) {
                //                 bufferedImage = bufferedImage.getSubimage(x, y, segWidth, segHeight);
                //                 width = segWidth;
                //                 height = segHeight;
                //                 log.info("Successfully cropped image to {}x{}", width, height);
                //             } else {
                //                 log.warn("Invalid crop coordinates, using original image: x={}, y={}, width={}, height={}, original={}x{}", 
                //                         x, y, segWidth, segHeight, width, height);
                //             }
                //         } else {
                //             log.warn("Could not find segment position fields, using original image");
                //         }
                //     } catch (Exception e) {
                //         log.error("Failed to apply segment position for cropping: {}", e.getMessage(), e);
                //         // Continue with original image
                //     }
                // }
                
                // Convert to BMP format
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(bufferedImage, "bmp", baos);
                byte[] bmpData = baos.toByteArray();
                
                log.debug("Extracted BMP data: {}x{}, size={} bytes", width, height, bmpData.length);
                return bmpData;
            }
        } catch (Exception e) {
            log.error("Failed to extract BMP image data: {}", e.getMessage());
            throw e;
        }
        return null;
    }
    
    /**
     * Convert raw grayscale image to BMP format with proper row padding.
     * BMP rows must be padded to 4-byte boundaries.
     */
    private byte[] convertRawToBmp(byte[] rawData, int width, int height) {
        try {
            // Calculate row stride with padding (must be multiple of 4)
            int rowStride = ((width + 3) / 4) * 4;
            int padding = rowStride - width;
            
            // BMP header for 8-bit grayscale
            int imageDataSize = rowStride * height;
            int fileSize = 54 + 1024 + imageDataSize; // Header + palette + padded data
            int dataOffset = 1078; // Header (54) + palette (1024)
            
            byte[] bmp = new byte[fileSize];
            int offset = 0;
            
            // BMP File Header (14 bytes)
            bmp[offset++] = 'B'; bmp[offset++] = 'M'; // Signature
            writeInt(bmp, offset, fileSize); offset += 4; // File size
            writeInt(bmp, offset, 0); offset += 4; // Reserved
            writeInt(bmp, offset, dataOffset); offset += 4; // Data offset
            
            // DIB Header (40 bytes)
            writeInt(bmp, offset, 40); offset += 4; // Header size
            writeInt(bmp, offset, width); offset += 4; // Width
            writeInt(bmp, offset, height); offset += 4; // Height
            writeShort(bmp, offset, 1); offset += 2; // Planes
            writeShort(bmp, offset, 8); offset += 2; // Bits per pixel
            writeInt(bmp, offset, 0); offset += 4; // Compression
            writeInt(bmp, offset, imageDataSize); offset += 4; // Image size (with padding)
            writeInt(bmp, offset, 2835); offset += 4; // X pixels per meter (~72 DPI)
            writeInt(bmp, offset, 2835); offset += 4; // Y pixels per meter (~72 DPI)
            writeInt(bmp, offset, 256); offset += 4; // Colors used
            writeInt(bmp, offset, 256); offset += 4; // Important colors
            
            // Color palette (grayscale)
            for (int i = 0; i < 256; i++) {
                bmp[offset++] = (byte) i; // Blue
                bmp[offset++] = (byte) i; // Green
                bmp[offset++] = (byte) i; // Red
                bmp[offset++] = 0; // Reserved
            }
            
            // Image data (bottom-up, so reverse rows) with padding
            for (int y = height - 1; y >= 0; y--) {
                System.arraycopy(rawData, y * width, bmp, offset, width);
                offset += width;
                // Add padding bytes for this row
                for (int p = 0; p < padding; p++) {
                    bmp[offset++] = 0;
                }
            }
            
            log.debug("BMP conversion: {}x{}, rowStride={}, padding={}, fileSize={}", 
                     width, height, rowStride, padding, fileSize);
            
            return bmp;
        } catch (Exception e) {
            log.error("Failed to convert raw image to BMP", e);
            return rawData; // Return raw data as fallback
        }
    }
    
    private void writeInt(byte[] buffer, int offset, int value) {
        buffer[offset] = (byte) (value & 0xFF);
        buffer[offset + 1] = (byte) ((value >> 8) & 0xFF);
        buffer[offset + 2] = (byte) ((value >> 16) & 0xFF);
        buffer[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }
    
    private void writeShort(byte[] buffer, int offset, int value) {
        buffer[offset] = (byte) (value & 0xFF);
        buffer[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }
    
    /**
     * Save image data to a file on the local machine
     */
    private void saveImageToFile(byte[] imageData, String prefix, int fingerCount) {
        try {
            // Create captures directory if it doesn't exist
            java.io.File capturesDir = new java.io.File("C:\\Users\\Rodb\\Desktop\\captures");
            if (!capturesDir.exists()) {
                capturesDir.mkdirs();
            }
            
            // Generate timestamped filename
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = now.format(formatter);
            String filename = String.format("%s_%d_fingers_%s.bmp", prefix, fingerCount, timestamp);
            
            java.io.File outputFile = new java.io.File(capturesDir, filename);
            java.nio.file.Files.write(outputFile.toPath(), imageData);
            
            log.info("Saved capture image to: {}", outputFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to save image to file: {}", e.getMessage());
        }
    }
    
    private int estimateQuality(byte[] imageData, int width, int height) {
        // Simple quality estimation based on contrast and variance
        if (imageData == null || imageData.length == 0) return 0;
        
        long sum = 0;
        int min = 255, max = 0;
        
        for (byte b : imageData) {
            int pixel = b & 0xFF;
            sum += pixel;
            if (pixel < min) min = pixel;
            if (pixel > max) max = pixel;
        }
        
        int contrast = max - min;
        int avgBrightness = (int) (sum / imageData.length);
        
        // Quality based on contrast (more contrast = better quality)
        int quality = Math.min(100, (contrast * 100) / 255);
        
        // Penalize very dark or very bright images
        if (avgBrightness < 50 || avgBrightness > 200) {
            quality = Math.max(40, quality - 20);
        }
        
        return quality;
    }
    
    @Override
    public String getDeviceInfo() {
        if (!initialized) {
            return "Integrated Biometrics Scanner (Not initialized)";
        }
        return deviceModel;
    }
    
    @Override
    public void release() {
        if (initialized) {
            log.info("Releasing IB Scanner resources");
            
            try {
                if (ibDevice != null) {
                    Method closeMethod = ibDevice.getClass().getMethod("close");
                    closeMethod.invoke(ibDevice);
                    ibDevice = null;
                }
                
                if (ibScan != null) {
                    Method disposeMethod = ibScan.getClass().getMethod("dispose");
                    disposeMethod.invoke(ibScan);
                    ibScan = null;
                }
            } catch (Exception e) {
                log.warn("Error releasing IB Scanner resources", e);
            }
            
            initialized = false;
        }
    }
    
    @Override
    public String getScannerType() {
        return "INTEGRATED_BIOMETRICS";
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
