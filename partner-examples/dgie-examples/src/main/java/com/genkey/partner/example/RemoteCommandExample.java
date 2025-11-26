package com.genkey.partner.example;

import com.genkey.abisclient.service.TestABISService;
import com.genkey.platform.utils.Commons;
import org.junit.Test;

/**
 * This test executes commands on the remote server which is strictly a test framework function.
 *
 * <p>The objective is only to support the construction of remote restart and reset of a TEST ABIS
 * installation to support automated regression tests that need to return to a prior state
 *
 * @author Gavan
 */
public class RemoteCommandExample extends PartnerExample {

  private static final String TEST_COMMAND = "testCommand";
  private static final String TEST_SUDO_COMMAND = "testSudoCommand";
  private static final String ABIS_STOP_COMMAND = "stop_abis";
  private static final String ABIS_START_COMMAND = "start_abis";
  private static final String ABIS_RESET_COMMAND = "reset_cache";

  public static void main(String[] args) {
    RemoteCommandExample test = new RemoteCommandExample();
    test.runTestExamples();
  }

  @Override
  protected void runAllExamples() {
    // remoteCommandTest();
    //		abisResetTest();
    abisResetTest3();
  }

  @Test
  public void remoteCommandTest() {
    for (int ix = 0; ix < 100; ix++) {
      println("Executing command sequence " + ix);
      remoteCommandTest(TEST_COMMAND);
      remoteCommandTest(TEST_SUDO_COMMAND);
    }
  }

  @Test
  public void abisResetTest() {
    TestABISService abisService = getTestABISService();
    boolean isAvailable = abisService.testAvailable();
    String abisSatus = abisService.testABISConnection();
    // String connection = abisService.t
    abisService.remoteCommand(ABIS_STOP_COMMAND);
    abisService.remoteCommand(ABIS_RESET_COMMAND);
    abisService.remoteCommand(ABIS_START_COMMAND);
  }

  @Test
  public void abisResetTest2() {
    TestABISService abisService = getTestABISService();
    boolean avail = abisService.testAvailable();
    assertTrue(avail);
    String abisConnection = abisService.testABISConnection();
    printObject("ABIS Connection (BEfore)", abisConnection);
    abisService.systemReset();

    while (!abisService.testAvailable()) {
      println("Waiting for abisService");
      Commons.waitMillis(3000);
    }
    println("ABIS Client service available");

    // Access with standard time-out
    abisConnection = abisService.waitABISConnection(10000, 1000);
    while (abisConnection == null) {
      println("ABIS Connection delay - NOT EXPECTED");
      Commons.waitMillis(3000);
      abisConnection = abisService.testABISConnection();
    }
    println("ABIS core service available");
    printObject("ABIS Connection String (After)", abisConnection);
  }

  public void abisResetTest3() {
    for (int ix = 0; ix < 10; ix++) {
      printHeader("Running test " + ix);
      abisResetTest2();
    }
  }

  private void remoteCommandTest(String testCommand) {
    TestABISService abisService = getTestABISService();
    int result = abisService.remoteCommand(testCommand);
    printResult(testCommand, result);
  }
}
