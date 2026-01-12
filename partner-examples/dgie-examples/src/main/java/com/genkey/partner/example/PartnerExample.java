package com.genkey.partner.example;

import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.ExampleTestUtils;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.RestServices;
import com.genkey.abisclient.service.TestABISService;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.biographic.BiographicAttribute;
import com.genkey.partner.biographic.BiographicIdentifier;
import com.genkey.partner.biographic.BiographicProfileRecord;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.dgie.LegacyMatchingService;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FormatUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class PartnerExample extends ExampleModule {

  public static String PartnerImagePath = "partner/images";
  public static String ImageSet100 = "set_100";

  protected static String LocalServiceHost = "abis.production"; // 10.11.11.28";
  protected static String LocalServiceHostDR = "abis.dr"; // 10.11.11.28";
  // protected static String RemoteServiceHost = "10.22.74.51";
  protected static String RemoteServiceHost = "abis.production.remote";
  protected static final String RemoteLegacyHost =
      RemoteServiceHost; // "DGIETestHost"; //10.22.72.51";
  protected static final String RemoteServiceHostDR = RemoteServiceHost; // "10.22.174.51";

  static boolean DeletesPending = false;

  static boolean FaceMatchEnabled = true;

  static boolean enforcePendingDelete = false;

  //protected static boolean UseRemote = false;

  protected static boolean FlgRemote = false;
  
  protected static boolean UseDR = false;

  public static String LocalPrimaryHost = UseDR ? LocalServiceHostDR : LocalServiceHost;
  public static String RemotePrimaryHost = UseDR ? RemoteServiceHostDR : RemoteServiceHost;
  public static String LocalSecondaryHost = UseDR ? LocalServiceHost : LocalServiceHostDR;
  public static final String RemoteSecondaryHost = UseDR ? RemoteServiceHost : RemoteServiceHostDR;

  private static final String ServiceHost = FlgRemote ? RemotePrimaryHost : LocalPrimaryHost;
  private static final String FailoverHost = FlgRemote ? RemoteSecondaryHost : LocalSecondaryHost;
  private static final String LegacyServiceHost = ServiceHost;
  private static final int ServicePortLocal = 9091;
  private static final int ServicePortRemote = 8091;
  private static final int ServicePort = FlgRemote ? ServicePortRemote : ServicePortLocal;
  private static final int ABISPort = 0;
  private static final String DomainName = "EnrollmentSDK";
  private static final String TestDomain = "test";
  
  public static int[] FourFingers = Commons.generateRangeV(2, 3, 7, 8);

  public static int[] SixFingers = Commons.generateRangeV(1, 2, 3, 6, 7, 8);

  public static int[] TenFingers = Commons.generateRangeArray(1, 10);

  public static int[] Thumbs = Commons.generateRangeV(1, 6);

  public static int[] IndexFingers = Commons.generateRangeV(2, 7);

  public static int[] RightHand = Commons.generateRangeArray(2, 4);

  public static int[] LeftHand = Commons.generateRangeV(10, 9, 8, 7);

  public static String OPT_HOSTNAME = "abis.partner.hostName";
  public static String OPT_HOSTNAME_LEGACY = "abis.partner.hostName.legacy";
  public static String OPT_HOSTNAME_DR = "abis.partner.hostName.dr";
  public static String OPT_PORT = "abis.partner.port";
  public static String OPT_DOMAIN_NAME = "abis.partner.domainName";
  public static String OPT_USE_REMOTE= "abis.partner.remote";
  public static String OPT_FACE_ENABLED= "abis.partner.faceEnabled";
  
  public static void initImageDirectory() {
    TestDataManager.setImageRootPath(PartnerImagePath);
    TestDataManager.setImageSet(ImageSet100);
  }

  public static String getPrimaryHost() {
    return ExampleTestUtils.getPropertyValue(OPT_HOSTNAME, RemotePrimaryHost);
  }

  public static String getSecondaryHost() {
    return ExampleTestUtils.getPropertyValue(OPT_HOSTNAME_DR, RemoteSecondaryHost);
  }

  public static String getLegacyHost() {
    return ExampleTestUtils.getPropertyValue(OPT_HOSTNAME_LEGACY, LegacyServiceHost);
  }

  public static int getServicePort() {
    return ExampleTestUtils.getPropertyInteger(OPT_PORT, ServicePortRemote);
  }

  public static String getPartnerDomainName() {
    return ExampleTestUtils.getPropertyValue(OPT_DOMAIN_NAME, DomainName);
  }

  public static boolean isUseRemote() {
	  return ExampleTestUtils.getPropertyBoolean(OPT_USE_REMOTE, FlgRemote);
  }
  
  @BeforeClass
  public static void checkTestsuiteInit() {
    PartnerTestSuite.init();
  }

  @Override
  protected void setUp() {
    PartnerTestSuite.init();
  }

  long tearDownPauseTime = 5000;

  @Override
  protected void tearDown() {
    // TODO Auto-generated method stub

    super.tearDown();
    FormatUtils.printBanner(
        " Waiting " + getTearDownPauseTime() / 1000.0 + " seconds before shutdown");
    Commons.waitMillis(getTearDownPauseTime());
    printMessage("Exiting now");
  }

  public long getTearDownPauseTime() {
    return tearDownPauseTime;
  }

  public void setTearDownPauseTime(long tearDownPauseTime) {
    this.tearDownPauseTime = tearDownPauseTime;
  }

  @Override
  protected void runAllExamples() {
    // TODO Auto-generated method stub

  }

  public GenkeyABISService getABISService() {
    return RestServices.getInstance().accessServiceInstance(GenkeyABISService.class);
  }

  public BiographicService getBiographicService() {
    return RestServices.getInstance().accessServiceInstance(BiographicService.class);
  }

  public LegacyMatchingService getLegacyService() {
    return RestServices.getInstance().accessServiceInstance(LegacyMatchingService.class);
  }

  public void showTests() {
    List<String> testMethods = new ArrayList<>();
    for (Method method : this.getClass().getMethods()) {
      Test annotation = method.getAnnotation(Test.class);
      if (annotation != null) {
        testMethods.add(method.getName());
      }
    }
    FormatUtils.printObject("TestMethods", CollectionUtils.containerToString(testMethods, "\n"));
  }

  public static String getBiographicManifest(BiographicProfileRecord matchRecord) {
    StringBuffer formatter = new StringBuffer();
    for (BiographicAttribute attribute : matchRecord.getBiographicAttributes()) {
      formatter
          .append(attribute.getAttribute())
          .append('=')
          .append(attribute.getTextValue())
          .append('\n');
    }
    return formatter.toString();
  }

  public static void forceTestDomain(SubjectEnrollmentReference subject) {
    reassignDomainName(subject, TestDomain);
  }

  public static void reassignDomainName(SubjectEnrollmentReference subject, String domainName) {
    String subjectId = forceDomainName(subject.getSubjectID(), domainName);
    subject.setSubjectID(subjectId);
  }

  public static String forceDomainName(String biographicId, String domainName) {
    biographicId = BiographicIdentifier.resolveExternalID(biographicId, domainName);
    BiographicIdentifier id = BiographicIdentifier.valueOf(biographicId);
    id.setDomainName(domainName);
    return id.getExternalID();
  }

  public static TestABISService getTestABISService() {
    TestABISService abisService = (TestABISService) ABISServiceModule.getABISService();
    return abisService;
  }

  public static void commitABISDeletes() {
    if (isDeletesPending()) {
      println("Processing pending ABIS deletes - system will be restarted");
      TestABISService service = getTestABISService();
      service.commitSubjectDeletes();
      println("ABIS deletes committed and system restarting");
    }
  }

  public static void setDeletesPending(boolean status) {
    DeletesPending = status;
  }

  public static boolean isDeletesPending() {
    return DeletesPending;
  }

  public static boolean isFaceMatchEnabled() {
    return ExampleTestUtils.getPropertyBoolean(OPT_FACE_ENABLED, PartnerExample.FaceMatchEnabled);
  }

  public static void setFaceMatchEnabled(boolean faceMatchEnabled) {
    ExampleTestUtils.setPropertyValue(OPT_FACE_ENABLED, faceMatchEnabled);
  }

  public static boolean isEnforcePendingDelete() {
    return enforcePendingDelete;
  }

  public static void setEnforcePendingDelete(boolean enforcePendingDelete) {
    PartnerExample.enforcePendingDelete = enforcePendingDelete;
  }
}
