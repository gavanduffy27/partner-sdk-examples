package com.genkey.fingerprint.controller;

import com.genkey.fingerprint.model.*;
import com.genkey.fingerprint.service.AbisService;
import com.genkey.fingerprint.service.CaptureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fingerprint")
@Tag(name = "Fingerprint Operations", description = "APIs for fingerprint capture and biometric operations")
public class FingerprintController {
    
    private final CaptureService captureService;
    private final AbisService abisService;
    
    public FingerprintController(CaptureService captureService, AbisService abisService) {
        this.captureService = captureService;
        this.abisService = abisService;
    }
    
    // ==================== Scanner Operations ====================
    
    @GetMapping("/scanner/status")
    @Operation(summary = "Get scanner status", description = "Check if fingerprint scanner is ready")
    public ResponseEntity<Map<String, Object>> getScannerStatus() {
        boolean ready = captureService.isScannerReady();
        String info = captureService.getScannerInfo();
        
        return ResponseEntity.ok(Map.of(
                "ready", ready,
                "deviceInfo", info,
                "abisInitialized", abisService.isInitialized()
        ));
    }
    
    @GetMapping("/scanner/connection")
    @Operation(summary = "Test ABIS connection", description = "Test connection to ABIS server")
    public ResponseEntity<Map<String, Object>> testConnection() {
        String connection = abisService.testConnection();
        boolean connected = connection != null;
        
        return ResponseEntity.ok(Map.of(
                "connected", connected,
                "connectionInfo", connection != null ? connection : "Not connected"
        ));
    }
    
    // ==================== Capture Operations ====================
    
    @PostMapping("/capture/single")
    @Operation(summary = "Capture single fingerprint", description = "Capture fingerprint from scanner for specified finger")
    public ResponseEntity<CaptureResult> captureSingle(@RequestParam int finger) {
        if (finger < 1 || finger > 10) {
            return ResponseEntity.badRequest().body(
                    CaptureResult.builder()
                            .success(false)
                            .statusCode(-1)
                            .statusMessage("Finger must be between 1 and 10")
                            .build()
            );
        }
        
        CaptureResult result = captureService.captureSingleFinger(finger);
        
        // Encode image data as base64 for JSON response
        if (result.getImageData() != null) {
            result.setTemplateBase64(Base64.getEncoder().encodeToString(result.getImageData()));
            result.setImageData(null); // Don't send raw bytes in JSON
        }
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/capture/multiple")
    @Operation(summary = "Capture multiple fingerprints", description = "Capture fingerprints for multiple fingers")
    public ResponseEntity<List<CaptureResult>> captureMultiple(@RequestBody int[] fingers) {
        List<CaptureResult> results = captureService.captureMultipleFingers(fingers);
        
        // Encode image data as base64
        for (CaptureResult result : results) {
            if (result.getImageData() != null) {
                result.setTemplateBase64(Base64.getEncoder().encodeToString(result.getImageData()));
                result.setImageData(null);
            }
        }
        
        return ResponseEntity.ok(results);
    }
    
    // ==================== Enrollment Operations ====================
    
    @PostMapping("/enroll/capture")
    @Operation(summary = "Capture and enroll", description = "Capture fingerprints from scanner and enroll subject")
    public ResponseEntity<EnrollmentResponse> captureAndEnroll(
            @RequestParam String subjectId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestBody int[] fingers) {
        
        EnrollmentResponse response = captureService.captureAndEnroll(subjectId, firstName, lastName, fingers);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/enroll")
    @Operation(summary = "Enroll with images", description = "Enroll subject with provided fingerprint images")
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        log.info("Enroll request received: subjectId={}, fingerprints={}, faceImagePresent={}",
                request.getSubjectId(),
                request.getFingerprints() != null ? request.getFingerprints().size() : 0,
                request.getFaceImage() != null && request.getFaceImage().length > 0);
        EnrollmentResponse response = abisService.enrollSubject(request);
        log.info("Enroll response: subjectId={}, success={}, statusCode={}, statusMessage={}",
                response.getSubjectId(), response.isSuccess(), response.getStatusCode(), response.getStatusMessage());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping(value = "/enroll/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enroll with uploaded images", description = "Enroll subject with uploaded fingerprint and face images")
    public ResponseEntity<EnrollmentResponse> enrollWithUpload(
            @RequestParam String subjectId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam int[] fingers,
            @RequestPart("images") List<MultipartFile> images,
            @RequestPart(value = "face", required = false) MultipartFile face) {
        
        try {
            List<FingerprintData> fingerprints = new ArrayList<>();
            
            for (int i = 0; i < images.size() && i < fingers.length; i++) {
                MultipartFile file = images.get(i);
                String filename = file.getOriginalFilename();
                String format = filename != null && filename.contains(".") ? 
                        filename.substring(filename.lastIndexOf(".") + 1).toUpperCase() : "BMP";
                
                fingerprints.add(FingerprintData.builder()
                        .finger(fingers[i])
                        .imageData(file.getBytes())
                        .imageFormat(format)
                        .build());
            }
            
            EnrollmentRequest.EnrollmentRequestBuilder requestBuilder = EnrollmentRequest.builder()
                    .subjectId(subjectId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .targetFingers(fingers)
                    .fingerprints(fingerprints);

            if (face != null && !face.isEmpty()) {
                String faceFilename = face.getOriginalFilename();
                String faceFormat = faceFilename != null && faceFilename.contains(".") ?
                        faceFilename.substring(faceFilename.lastIndexOf(".") + 1).toUpperCase() : "JPG";
                requestBuilder.faceImage(face.getBytes()).faceImageFormat(faceFormat);
            }
            
            EnrollmentResponse response = abisService.enrollSubject(requestBuilder.build());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Upload enrollment failed", e);
            return ResponseEntity.ok(EnrollmentResponse.builder()
                    .success(false)
                    .subjectId(subjectId)
                    .statusCode(-2)
                    .statusMessage("Upload failed: " + e.getMessage())
                    .build());
        }
    }
    
    // ==================== Verification Operations ====================
    
    @PostMapping("/verify/capture")
    @Operation(summary = "Capture and verify", description = "Capture fingerprints from scanner and verify against enrolled subject")
    public ResponseEntity<VerifyResponse> captureAndVerify(
            @RequestParam String subjectId,
            @RequestBody int[] fingers) {
        
        VerifyResponse response = captureService.captureAndVerify(subjectId, fingers);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    @Operation(summary = "Verify with images", description = "Verify subject with provided fingerprint images")
    public ResponseEntity<VerifyResponse> verify(@Valid @RequestBody VerifyRequest request) {
        VerifyResponse response = abisService.verifySubject(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/verify/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Verify with uploaded images", description = "Verify subject with uploaded fingerprint or face image files")
    public ResponseEntity<VerifyResponse> verifyWithUpload(
            @RequestParam String subjectId,
            @RequestParam(required = false) int[] fingers,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "face", required = false) MultipartFile face) {
        
        try {
            VerifyRequest.VerifyRequestBuilder requestBuilder = VerifyRequest.builder()
                    .subjectId(subjectId)
                    .targetFingers(fingers != null ? fingers : new int[0]);

            if (images != null && fingers != null) {
                List<FingerprintData> fingerprints = new ArrayList<>();
                for (int i = 0; i < images.size() && i < fingers.length; i++) {
                    MultipartFile file = images.get(i);
                    String filename = file.getOriginalFilename();
                    String format = filename != null && filename.contains(".") ? 
                            filename.substring(filename.lastIndexOf(".") + 1).toUpperCase() : "BMP";
                    
                    fingerprints.add(FingerprintData.builder()
                            .finger(fingers[i])
                            .imageData(file.getBytes())
                            .imageFormat(format)
                            .build());
                }
                requestBuilder.fingerprints(fingerprints);
            }

            if (face != null && !face.isEmpty()) {
                String faceFilename = face.getOriginalFilename();
                String faceFormat = faceFilename != null && faceFilename.contains(".") ?
                        faceFilename.substring(faceFilename.lastIndexOf(".") + 1).toUpperCase() : "JPG";
                requestBuilder.faceImage(face.getBytes()).faceImageFormat(faceFormat);
            }
            
            VerifyResponse response = abisService.verifySubject(requestBuilder.build());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Upload verification failed", e);
            return ResponseEntity.ok(VerifyResponse.builder()
                    .verified(false)
                    .subjectId(subjectId)
                    .statusCode(-2)
                    .statusMessage("Upload failed: " + e.getMessage())
                    .build());
        }
    }
    
    // ==================== Identification Operations ====================
    
    @PostMapping("/identify/capture")
    @Operation(summary = "Capture and identify", description = "Capture fingerprints and perform 1:N search")
    public ResponseEntity<IdentifyResponse> captureAndIdentify(
            @RequestBody int[] fingers,
            @RequestParam(defaultValue = "10") int maxCandidates) {
        
        IdentifyResponse response = captureService.captureAndIdentify(fingers, maxCandidates);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/identify")
    @Operation(summary = "Identify with images", description = "Perform 1:N search with provided fingerprint images")
    public ResponseEntity<IdentifyResponse> identify(@Valid @RequestBody IdentifyRequest request) {
        IdentifyResponse response = abisService.identifySubject(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/identify/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Identify with uploaded images", description = "Identify subject with uploaded fingerprint or face image files")
    public ResponseEntity<IdentifyResponse> identifyWithUpload(
            @RequestParam(required = false) int[] fingers,
            @RequestParam(defaultValue = "10") int maxCandidates,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "face", required = false) MultipartFile face) {
        
        try {
            IdentifyRequest.IdentifyRequestBuilder requestBuilder = IdentifyRequest.builder()
                    .maxCandidates(maxCandidates);

            if (images != null && fingers != null) {
                List<FingerprintData> fingerprints = new ArrayList<>();
                for (int i = 0; i < images.size() && i < fingers.length; i++) {
                    MultipartFile file = images.get(i);
                    String filename = file.getOriginalFilename();
                    String format = filename != null && filename.contains(".") ? 
                            filename.substring(filename.lastIndexOf(".") + 1).toUpperCase() : "BMP";
                    
                    fingerprints.add(FingerprintData.builder()
                            .finger(fingers[i])
                            .imageData(file.getBytes())
                            .imageFormat(format)
                            .build());
                }
                requestBuilder.fingerprints(fingerprints);
            }

            if (face != null && !face.isEmpty()) {
                String faceFilename = face.getOriginalFilename();
                String faceFormat = faceFilename != null && faceFilename.contains(".") ?
                        faceFilename.substring(faceFilename.lastIndexOf(".") + 1).toUpperCase() : "JPG";
                requestBuilder.faceImage(face.getBytes()).faceImageFormat(faceFormat);
            }
            
            IdentifyResponse response = abisService.identifySubject(requestBuilder.build());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Upload identification failed", e);
            return ResponseEntity.ok(IdentifyResponse.builder()
                    .found(false)
                    .statusCode(-2)
                    .statusMessage("Upload failed: " + e.getMessage())
                    .build());
        }
    }
    
    // ==================== Subject Management ====================
    
    @GetMapping("/subject/{subjectId}/exists")
    @Operation(summary = "Check subject exists", description = "Check if subject is enrolled in the system")
    public ResponseEntity<Map<String, Object>> checkSubjectExists(@PathVariable String subjectId) {
        boolean exists = abisService.subjectExists(subjectId);
        return ResponseEntity.ok(Map.of(
                "subjectId", subjectId,
                "exists", exists
        ));
    }
    
    @DeleteMapping("/subject/{subjectId}")
    @Operation(summary = "Delete subject", description = "Delete enrolled subject from the system")
    public ResponseEntity<Map<String, Object>> deleteSubject(@PathVariable String subjectId) {
        boolean deleted = abisService.deleteSubject(subjectId);
        return ResponseEntity.ok(Map.of(
                "subjectId", subjectId,
                "deleted", deleted,
                "message", deleted ? "Subject deleted successfully" : "Failed to delete subject"
        ));
    }
    
    // ==================== Utility Endpoints ====================
    
    @GetMapping("/fingers")
    @Operation(summary = "Get finger names", description = "Get mapping of finger positions to names")
    public ResponseEntity<Map<Integer, String>> getFingerNames() {
        Map<Integer, String> fingers = Map.of(
                1, "Right Thumb",
                2, "Right Index",
                3, "Right Middle",
                4, "Right Ring",
                5, "Right Little",
                6, "Left Thumb",
                7, "Left Index",
                8, "Left Middle",
                9, "Left Ring",
                10, "Left Little"
        );
        return ResponseEntity.ok(fingers);
    }
}
