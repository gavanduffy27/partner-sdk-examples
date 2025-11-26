package com.genkey.partner.example;

import com.genkey.abisclient.service.RestClientException;
import com.genkey.abisclient.service.RestServices;
import com.genkey.abisclient.service.VerifyResponse;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.dgie.LegacyMatchingService;
import com.genkey.partner.utils.EnrollmentUtils;
import com.genkey.platform.rest.RemoteAccessService;
import com.genkey.platform.rest.RemoteAccessService.ExceptionSuppressionMode;
import com.genkey.platform.rest.RestResponseParameter;
import org.junit.Test;

/**
 * These tests show-case how to test and handle errors that arise from failure in the connection
 * between client and server.
 *
 * <p>Much consideration has been given as to whether or not such issues should be handled as errors
 * or thrown as exceptions. The prevailing view is that the default behaviour should be to
 * preferentially return error status such as http response code as properties within response
 * objects, which can be directly tested for error states, or can be accessed in a thread safe
 * manner from the service object.
 *
 * <p>It is however to be flexible and to adapt the behavior in accordance to the preferences of the
 * application. The examples below simulate the various error conditions, for example by using bad
 * host names, and show how the application can manage errors for the different modes.
 *
 * @author Gavan
 */
public class ErrorConnectionTests extends PartnerExample {

  SubjectEnrollmentReference legacySubject = null;

  public static void main(String[] args) {
    ErrorConnectionTests test = new ErrorConnectionTests();
    test.runTestExamples();
  }

  @Override
  protected void runAllExamples() {
    this.testAlwaysSuppress();

    this.testDefaultSuppression();
    this.testNoSuppression();
    this.testAlwaysSuppress();

    hostUnavailableTest();
    badPortTest();
  }

  /**
   * Obtain the subject object for test calls
   *
   * @return
   */
  public SubjectEnrollmentReference getLegacyTestSubject() {
    if (legacySubject == null) {
      legacySubject = EnrollmentUtils.getLegacyTestSubject();
    }
    return legacySubject;
  }

  static boolean useFastFail = true;

  public void setBadHostMode() {
    DGIEServiceModule.closeService();
    // In fast fail we get the quickly failing BADHostName - otherwise it is the much slower
    // connection timeout error
    String badHostName = useFastFail ? "BadServiceHostName" : "10.22.75.57";
    DGIEServiceModule.initLegacyService(badHostName, getServicePort());
  }

  /**
   * In this mode we show how exceptions are handled by default.
   *
   * <p>This mode observes the preference that when REST status is incorporated as part of the
   * method return value then no exception is thrown
   *
   * <p>However when the return method is of a simple type such as boolean then an exception will be
   * thrown.
   *
   * <p>These preferences are configurable as shown in subsequent examples.
   */
  @Test
  public void testDefaultSuppression() {
    // Simulate that host is not available by using bad host name
    setBadHostMode();

    LegacyMatchingService legacyService = DGIEServiceModule.getLegacyService();

    // Set the exception mode for the service
    legacyService.setExceptionSuppressionMode(ExceptionSuppressionMode.SuppressDefault);

    // Note we can also do this as a shortcut to set exception mode for all registered services
    RestServices.getInstance()
        .setExceptionSuppressionMode(ExceptionSuppressionMode.SuppressDefault);

    // access a test subject
    SubjectEnrollmentReference subject = getLegacyTestSubject();

    VerifyResponse response = null;
    response = legacyService.verifySubject(subject);

    // Note no exception is thrown but we check directly from either the response or the service

    if (!response.isSuccess()) {
      showResponseParameter(response);
    }

    // We can also check for REST response status directly from the service
    if (!legacyService.isSuccess()) {
      showResponseParameter(legacyService);
    }

    // In default mode we still throw exceptions for methods who do not return a value of
    // type RestResponseParameter

    // Let us check for whether subject exists
    boolean existsSubject = false;
    boolean exceptionStatus = false;
    try {
      existsSubject = legacyService.existsSubject("anySubject");

      if (existsSubject) {
        // do something
      } else {
        // something else
      }

    } catch (RestClientException e) {
      // Catch the exception here
      showRestClientException(e);
      exceptionStatus = true;
    }
    // We can test that the exception was thrown
    assertTrue(exceptionStatus);
  }

  /**
   * In this mode we set the exception handling to always throw. This is maximum mode for throwing
   * errors and it is shown in the example that in this case an exception will be thrown even though
   * the response parameter implements the RestResponseParameter interface
   */
  @Test
  public void testNoSuppression() {
    // Simulate that host is not available by using bad host name
    setBadHostMode();

    // Operate with no suppression so that exceptions are thrown even when the REST method returns
    // a parameter that implements RestResponseParameter which includes REST return code.

    RestServices.getInstance().setExceptionSuppressionMode(ExceptionSuppressionMode.NoSuppression);

    LegacyMatchingService legacyService = DGIEServiceModule.getLegacyService();

    // access a test subject
    SubjectEnrollmentReference subject = getLegacyTestSubject();

    VerifyResponse response = null;

    try {
      response = legacyService.verifySubject(subject);
    } catch (RestClientException exception) {

      if (exception.isRestReturnCode()) {
        this.showResponseParameter(exception);
      } else {
        moreComplexErrorHandler(exception);
      }
    }

    // However one exception .. that will never throw
    boolean isAvailable = legacyService.testAvailable();

    if (isAvailable) {
      // The response is printable from the errorMessage
      printMessage(legacyService.getErrorMessage());
    } else {
      this.showResponseParameter(legacyService);
    }
  }

  /**
   * In this mode there is no exceptions thrown for any errors.
   *
   * <p>For methods which do not return a value that implements RestResponseParameter we access this
   * directly from the service object which also provides a thread safe implementation of the
   * interface based on the status attributes of the last call on the current thread.
   */
  @Test
  public void testAlwaysSuppress() {
    // Simulate that host is not available by using bad host name or IP
    setBadHostMode();

    // Operate with no suppression so that exceptions are thrown even when the REST method returns
    // a parameter that implements RestResponseParameter which includes REST return code.

    RestServices.getInstance().setExceptionSuppressionMode(ExceptionSuppressionMode.SuppressAll);

    LegacyMatchingService legacyService = DGIEServiceModule.getLegacyService();

    // access a test subject
    SubjectEnrollmentReference subject = getLegacyTestSubject();

    // Let us check for whether subject exists
    boolean existsSubject = legacyService.existsSubject("anySubject");

    // Note no exception is thrown but we check directly from either the response or the service

    // We can also check for REST response status directly from the service
    if (!legacyService.isSuccess()) {

      showResponseParameter(legacyService);
    }

    // check for success status directly from the service object
    if (!legacyService.isSuccess()) {
      // Process the error noting legacyService is thread safe implementation of
      // RestResponseParameter
      moreComplexErrorHandler(legacyService);

    } else {
      if (existsSubject) {
        // do something
      } else {
        // something else
      }
    }
  }

  public void moreComplexErrorHandler(RestResponseParameter parameter) {
    // Show the error status
    int status = parameter.getStatusCode();
    String errorMessage = parameter.getErrorMessage();

    Throwable cause = parameter.getCause();

    if (parameter.isRestReturnCode()) {
      // Then this is a REST return code error
      int restCode = parameter.getStatusCode();
      String description = parameter.getErrorMessage();
      printError(restCode + "=>" + description);
    } else {
      // This has some other cause
      if (status == RemoteAccessService.UnknownHostException) {
        // Handling of unspecified host
        printMessage("This is a bad hostname buddy");
      } else if (status == RemoteAccessService.ConnectException) {
        // Handling of Connect Exception - typical for wrong port or host not accessible
        printMessage("Unable to connect to the server and port that are specified");
      } else if (status == RemoteAccessService.ProcessingException) {
        // Handle general REST processing exception
        cause.printStackTrace();
      } else if (status == RemoteAccessService.UnhandledException) {
        cause.printStackTrace();
      } else {
        // Should not happen
        cause.printStackTrace();
      }
    }
  }

  private void showResponseParameter(RestResponseParameter parameter) {
    int statusCode = parameter.getStatusCode();
    String message = parameter.getErrorMessage();
    printError(statusCode + "=>" + message);
  }

  /** Shows error testing when the host is unavailable. */
  @Test
  public void hostUnavailableTest() {
    LegacyMatchingService legacyService = DGIEServiceModule.getLegacyService();
    boolean available = legacyService.testAvailable();
    int statusCode = legacyService.getStatusCode();
    String messageCode = legacyService.getLastErrorMessage();
    printResult("initial error", statusCode);

    SubjectEnrollmentReference subject = EnrollmentUtils.getLegacyTestSubject();

    // Operate with no suppression - all exceptions are thrown
    RestServices.getInstance().setExceptionSuppressionMode(ExceptionSuppressionMode.NoSuppression);

    VerifyResponse response = null;
    boolean exceptionStatus = false;
    try {
      response = legacyService.verifySubject(subject);
    } catch (RestClientException e) {
      showRestClientException(e);
      exceptionStatus = true;
    }

    assertTrue(exceptionStatus);

    // Operate with default suppression - exceptions only get thrown for methods where the response
    // does not contain a status code
    RestServices.getInstance()
        .setExceptionSuppressionMode(ExceptionSuppressionMode.SuppressDefault);

    // Try again
    response = legacyService.verifySubject(subject);

    // No exception is thrown but we can check for failure..
    boolean status = response.isSuccess();
    super.assertFalse(status);

    /**
     * Next call is on a web service interface that returns a boolean value For these calls the
     * exception is always thrown because the result parameter does not provide access to the return
     * status code.
     */
    exceptionStatus = false;
    try {
      legacyService.existsSubject("anySubject");
    } catch (RestClientException e) {
      showRestClientException(e);
      exceptionStatus = true;
    }

    assertTrue(exceptionStatus);

    // However we can suppress exceptions entirely
    RestServices.getInstance().setExceptionSuppressionMode(ExceptionSuppressionMode.SuppressAll);
    boolean result = legacyService.existsSubject("anySubject");
    // Returns false but take care ..
    assertFalse(result);

    // Although the result does not contain a status code we can obtain it from the service
    // Note the errors state of the service objects is based on ThreadLocal variables and this is
    // thread safe
    // without any limitations on concurrency
    if (!legacyService.isSuccess()) {
      int errorCode = legacyService.getStatusCode();
      if (errorCode != 200) {
        // Certainly true because 200 is a success code
        String errorMessage = legacyService.getErrorMessage();
        printError(
            "Legacy service failed with error " + errorCode + " and message " + errorMessage);
      }
    }
  }

  public void badPortTest() {
    DGIEServiceModule.closeService();

    DGIEServiceModule.initLegacyService(getPrimaryHost(), getServicePort() + 50);
    LegacyMatchingService legacyService = DGIEServiceModule.getLegacyService();
    boolean available = legacyService.testAvailable();

    if (!available) {
      // of course it wont be true ..
      int statusCode = legacyService.getStatusCode();
      String message = legacyService.getLastErrorMessage();
      printResult("initial error", statusCode + " " + message);
    } else {
      printMessage("A miracle has occurred");
    }
  }

  private void showRestClientException(RestClientException e) {
    int restErrorCode = e.getStatusCode();
    String message = e.getErrorMessage();
    printResult("Error Message", message + ":" + restErrorCode);
  }
}
