package com.genkey.abisclient.examples.imagecapture;

import com.genkey.abisclient.ImageContextSDK;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.platform.utils.FileUtils;

public class LogDiagnosticTest extends ExampleModule {

  static final String CONFIG_FILE = "ABISClientLog.cfg";
  static final String CONFIG_FILE2 = "ABISClientLogTest.cfg";

  static final String LOG_CONFIG_PATH = "configuration/logging";

  static final String LOGFILE_NAME = "javaTestlog";

  static enum DiagnosticTest {
    LogDiagnosticFileTest,
    LogDiagnosticLoggerTest,
    LogDiagnosticConsoleTest,
    LogDiagnosticConfigFileTest,
    LogDiagnosticConfigStringTest
  };

  @Override
  protected void runAllExamples() {
    testReadLog();
    logConsoleTest();
    logFileOpenTest();
    logStandardTest();
    logConfigFileTest();
    logConfigStringTest();
  }

  private void testReadLog() {
    String result = ImageContextSDK.readLogDiagnostics(LOGFILE_NAME, 0);
    printObject("Result", result);
  }

  public void logConsoleTest() {
    executeDiagnosticTest(LOGFILE_NAME, DiagnosticTest.LogDiagnosticConsoleTest);
  }

  public void logFileOpenTest() {
    executeDiagnosticTest(LOGFILE_NAME, DiagnosticTest.LogDiagnosticFileTest);
  }

  public void logStandardTest() {
    executeDiagnosticTest(LOGFILE_NAME, DiagnosticTest.LogDiagnosticLoggerTest);
  }

  public void logConfigFileTest() {
    executeDiagnosticTest(CONFIG_FILE, DiagnosticTest.LogDiagnosticConfigFileTest);
  }

  public void logConfigStringTest() {
    try {
      String logConfigFile = FileUtils.expandConfigFile(LOG_CONFIG_PATH, CONFIG_FILE2, "cfg");
      String logSpec = FileUtils.stringFromFile(logConfigFile);
      executeDiagnosticTest(logSpec, DiagnosticTest.LogDiagnosticConfigStringTest);
    } catch (Exception e) {
      super.handleException(e);
    }
  }

  void executeDiagnosticTest(String logName, DiagnosticTest test) {
    int result = ImageContextSDK.exploreLogDiagnostics(logName, test.ordinal());
    printResult(test.toString(), result);
  }
}
