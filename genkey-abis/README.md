# Fingerprint Capture Service

A Spring Boot microservice for live fingerprint capture and integration with GenKey ABIS SDK.

## Features

- **Live Fingerprint Capture**: Capture fingerprints from hardware scanners
- **ABIS Integration**: Full integration with GenKey ABIS for enrollment, verification, and identification
- **Multiple Scanner Support**: Extensible architecture for different scanner manufacturers
- **REST APIs**: Complete REST API for all biometric operations
- **Swagger UI**: Interactive API documentation

## Prerequisites

1. **Java 17+** installed
2. **Maven 3.6+** installed
3. **GenKey ABIS SDK** installed with native libraries
4. **CodeMeter License** activated
5. **Fingerprint Scanner** (optional - Mock scanner available for testing)

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
# ABIS Server Configuration
abis:
  server:
    host: 10.22.74.51      # Your ABIS server IP
    port: 8091             # ABIS server port
    domain: EnrollmentSDK  # Your domain name
  library:
    path: c:/Users/Rodb/Desktop/AFIS/6.2.2-SNAPSHOT/abisclient-dll

# Scanner Configuration
scanner:
  type: MOCK              # MOCK, SECUGEN, FUTRONIC, DIGITAL_PERSONA, CROSSMATCH
  device-index: 0
  resolution: 500
  image-format: BMP
```

## Running the Service

### Using the startup script:
```cmd
run.cmd
```

### Using Maven:
```cmd
set JAVA_TOOL_OPTIONS=-Djava.library.path=c:\Users\Rodb\Desktop\AFIS\6.2.2-SNAPSHOT\abisclient-dll
mvn spring-boot:run
```

### Using JAR:
```cmd
mvn package
java -Djava.library.path=c:\Users\Rodb\Desktop\AFIS\6.2.2-SNAPSHOT\abisclient-dll -jar target\fingerprint-capture-service-1.0.0-SNAPSHOT.jar
```

## API Endpoints

The service runs on `http://localhost:8080/api`

### Scanner Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/fingerprint/scanner/status` | Check scanner status |
| GET | `/fingerprint/scanner/connection` | Test ABIS connection |

### Capture Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/fingerprint/capture/single?finger=2` | Capture single fingerprint |
| POST | `/fingerprint/capture/multiple` | Capture multiple fingerprints |

### Enrollment Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/fingerprint/enroll/capture` | Capture and enroll |
| POST | `/fingerprint/enroll` | Enroll with provided images |
| POST | `/fingerprint/enroll/upload` | Enroll with uploaded files |

### Verification Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/fingerprint/verify/capture?subjectId=123` | Capture and verify |
| POST | `/fingerprint/verify` | Verify with provided images |

### Identification Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/fingerprint/identify/capture` | Capture and identify (1:N) |
| POST | `/fingerprint/identify` | Identify with provided images |

### Subject Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/fingerprint/subject/{id}/exists` | Check if subject exists |
| DELETE | `/fingerprint/subject/{id}` | Delete subject |

## API Documentation

Swagger UI available at: `http://localhost:8080/api/swagger-ui.html`

## Example Usage

### 1. Check Scanner Status
```bash
curl http://localhost:8080/api/fingerprint/scanner/status
```

### 2. Capture and Enroll
```bash
curl -X POST "http://localhost:8080/api/fingerprint/enroll/capture?subjectId=TEST001&firstName=John&lastName=Doe" \
  -H "Content-Type: application/json" \
  -d "[1, 2, 6, 7]"
```

### 3. Capture and Verify
```bash
curl -X POST "http://localhost:8080/api/fingerprint/verify/capture?subjectId=TEST001" \
  -H "Content-Type: application/json" \
  -d "[2, 7]"
```

### 4. Capture and Identify
```bash
curl -X POST "http://localhost:8080/api/fingerprint/identify/capture?maxCandidates=10" \
  -H "Content-Type: application/json" \
  -d "[2, 7]"
```

## Finger Position Mapping

| Position | Finger |
|----------|--------|
| 1 | Right Thumb |
| 2 | Right Index |
| 3 | Right Middle |
| 4 | Right Ring |
| 5 | Right Little |
| 6 | Left Thumb |
| 7 | Left Index |
| 8 | Left Middle |
| 9 | Left Ring |
| 10 | Left Little |

## Adding Scanner Support

To add support for a new scanner manufacturer:

1. Create a class implementing `FingerprintScanner` interface
2. Add the scanner type to `ScannerConfig.ScannerType` enum
3. Add the case to `ScannerFactory.fingerprintScanner()` method
4. Include the manufacturer's SDK JAR in dependencies

Example for SecuGen:
```java
public class SecuGenScanner implements FingerprintScanner {
    @Override
    public boolean initialize() {
        // Initialize SecuGen SDK
    }
    
    @Override
    public CaptureResult capture(int finger, int timeout) {
        // Capture using SecuGen API
    }
}
```

## Troubleshooting

### Native Library Issues
Ensure `JAVA_TOOL_OPTIONS` or `java.library.path` points to the ABIS DLL directory.

### Connection Issues
- Verify ABIS server is running
- Check firewall rules for port 8091
- Ensure CodeMeter license is activated

### Scanner Not Detected
- Install manufacturer drivers
- Check USB connection
- Verify scanner SDK is in classpath
