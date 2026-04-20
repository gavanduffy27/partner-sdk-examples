package com.genkey.partner.workshop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.genkey.abisclient.ImageBlob;
import com.genkey.abisclient.examples.utils.ExampleTestUtils;
import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.service.MatchEngineResponse;
import com.genkey.abisclient.service.UpdateResponse;
import com.genkey.abisclient.service.params.EnquireStatus;
import com.genkey.partner.biographic.BiographicService;
import com.genkey.partner.dgie.DGIEServiceModule;
import com.genkey.partner.dgie.LegacyMatchingService;
import com.genkey.partner.example.PartnerExample;
import com.genkey.platform.utils.ArrayIterator;
import com.genkey.platform.utils.CollectionUtils;

public abstract class BMSWorkshopExample extends PartnerExample {

  GenkeyABISService abisService;
  BiographicService biographicService;
  LegacyMatchingService legacyService;

  @Override
  protected void setUp() {
    ExampleTestUtils.setCodeDefaultSettings();
    ExampleTestUtils.loadDefaultSettings();

    String hostName = PartnerExample.getPrimaryHost();
    int port = PartnerExample.getServicePort();
    String DomainName = PartnerExample.getPartnerDomainName();

    // Initialise core services ..
    // Currently for production this would be //10.22.74.51, 8091, BMS
    DGIEServiceModule.initCoreServices(hostName, port, DomainName);

    // To use Legacy services .. not required
    DGIEServiceModule.initLegacyService(hostName, port);

    abisService = ABISServiceModule.getABISService();
    biographicService = DGIEServiceModule.getBiographicService();
    legacyService = DGIEServiceModule.getLegacyService();
  }

  public GenkeyABISService getAbisService() {
    return abisService;
  }

  public BiographicService getBiographicService() {
    return biographicService;
  }

  public LegacyMatchingService getLegacyService() {
    return legacyService;
  }

  public boolean adjudicateCheck(ImageBlob querySubjectPortrait, ImageBlob matchFace) {
    // TODO Auto-generated method stub
    return false;
  }

  public void handleLateDuplicate(String testSubject, MatchEngineResponse updateResponse) {
    processFlow("Handle duplicate on existing subject (e.g. with delete and merge)");
  }

  public static boolean acceptMatchResults(MatchEngineResponse response, boolean veto) {
    return !veto;
  }

  public static void handleKnownSubject(String testSubject, MatchEngineResponse response) {
    processFlow("Handle known subject");
  }

  public static void processFlow(String processName) {
    printMessage("Hollow substitute for process " + processName);
  }

  public static int[] checkAFISComplete(EnquireStatus status, int[] required) {
    return arrayDiff(required, status.getFingers());
  }

  public static int[] arrayDiff(int targetSet[], int currentSet[]) {
    Set<Integer> set = asSet(currentSet);
    List<Integer> missing = CollectionUtils.newList();
    for (int value : targetSet) {
      if (!set.contains(value)) {
        missing.add(value);
      }
    }
    return asIntArray(missing);
  }

  private static int[] asIntArray(List<Integer> list) {
    int[] result = new int[list.size()];
    for (int ix = 0; ix < list.size(); ix++) {
      result[ix] = list.get(ix);
    }
    return result;
  }

  private static Set<Integer> asSet(int[] values) {
    Set<Integer> result = new HashSet<>();
    collectionAdd(result, values);
    return result;
  }

  private static void collectionAdd(Collection<Integer> c, int[] list) {
    for (int value : list) c.add(value);
  }

 
}
