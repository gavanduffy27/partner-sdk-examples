package com.genkey.partner.example.concurrency;

import com.genkey.platform.test.framework.GKTestCase4;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;

public class ConcurrencyMasterRunner extends GKTestCase4 {

  static List<Class<? extends Object>> clazzList = new ArrayList<>();

  public static void main(String[] args) {
    ConcurrencyMasterRunner master = new ConcurrencyMasterRunner();
    master.runInline();
  }

  public void runInline() {
    initSuite();
    concurrencyTestRunner();
  }

  @BeforeClass
  public void initSuite() {
    //		clazzList.add(ConcurrencyEnrolTest.class);
    clazzList.add(ConcurrencyBiographicFetchTest.class);
    clazzList.add(ConcurrencyBiographicInsertTest.class);
  }

  @Test
  public void concurrencyTestRunner() {
    Class[] classList = getTestClassList();
    JUnitCore.runClasses(ParallelComputer.classes(), classList);
    printHeader("All Tasks complete");
  }

  private Class[] getTestClassList() {
    Class[] result = new Class[clazzList.size()];
    int index = 0;
    for (Class clazz : clazzList) {
      result[index++] = clazz;
    }
    return result;
  }
}
