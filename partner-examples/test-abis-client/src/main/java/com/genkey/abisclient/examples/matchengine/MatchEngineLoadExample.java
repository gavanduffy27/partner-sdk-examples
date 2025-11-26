package com.genkey.abisclient.examples.matchengine;

import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.matchengine.Subject;
import com.genkey.abisclient.matchengine.Subset;
import java.util.List;

public class MatchEngineLoadExample extends MatchEngineExample {

  static String TestDatabase = "MyTestDatabase";

  @Override
  protected void runAllExamples() {
    testSubjectName();
    testLoadMatchEngine();
    testLoadLargeDatabase();
    testMultiFileLoad();
  }

  public static void testSubjectName() {
    Subject s1 = new Subject("myName");
    String name = s1.getSubjectID();
    printResult("Name", name);
    s1.setSubjectID("myNewName");
    printResult("New Name", s1.getSubjectID());
  }

  public void testLoadMatchEngine() {
    // First create an in-memory subset
    Subset subset = new Subset();

    // Use these fingers for enrolment - we have image files for 10 subjects
    int[] fingers = {3, 4, 7, 8};

    // Now add some subjects
    for (int ix = 1; ix <= 10; ix++) {
      long subjectID = ix;

      Subject subject = new Subject(subjectID);
      printHeader("Creating subject " + subjectID);
      // Return the image files for sample-index 0
      List<String> imageFiles = TestDataManager.getImageFiles(subjectID, fingers, 1);

      // Load these as ImageData
      List<ImageData> imageList = TestDataManager.loadImages(imageFiles);

      // Simulate an enrolment by just extracting a reference for each image
      for (int fix = 0; fix < fingers.length; fix++) {
        int fingerPos = fingers[fix];
        ImageData image = imageList.get(fix);
        PrintMessage("Adding reference for " + fingerPos);
        // Generate instance of ImageContext with image and allocated finger
        ImageContext context = new ImageContext(image, fingerPos);

        // Obtain the reference data - no index required as this is single finger image
        ReferenceDataItem reference = context.getReferenceData();

        byte[] refEncoding = reference.getReferenceData();

        // Incrementally build up subject by adding each reference as we generate them
        // We show 3 methods of adding a reference
        int mode = fix % 3;
        subject.addReference(reference);
      }
      // Subject now created with all references.

      int subjectMode = (int) (subjectID % 2);
      switch (subjectMode) {
        case 0:
          // Could save this to persistent storage ..
          byte[] subjectEncoding = subject.getEncoding();

          // recover this at later point in time.
          Subject restoredSubject = new Subject(subjectEncoding);
          // restoredSubject.Encoding = subjectEncoding;

          subset.addSubject(restoredSubject);
          break;

        case 1:
          // Could just add the subject
          subset.addSubject(subject);
          break;
      }
    }

    // Subset now fully created ..

    int nSubjects = subset.size();
    printHeader(" Subset created with number of subjects = " + nSubjects);

    // Save the subset to file. useful for testing but typically for real system
    // data would be managed at the granularity of the subjects.
    String saveFile = MatchEngineTestUtils.getDatabaseFileName(TestDatabase);
    subset.exportToFile(saveFile);
  }

  /// <summary>
  /// Same function as above but using the test utility, which has a more concise implementation
  /// </summary>
  /// <param name="firstSubject"></param>
  /// <param name="lastSubject"></param>
  /// <param name="fingers"></param>
  /// <param name="sampleIndex"></param>
  /// <param name="useCache"></param>
  public static void easyLoadMatchEngine(
      long firstSubject, long lastSubject, int[] fingers, int sampleIndex, boolean useCache) {
    Subset subset =
        MatchEngineTestUtils.loadSubset(firstSubject, lastSubject, fingers, sampleIndex, useCache);
    PrintMessage("Loaded subset of size " + subset.size());
  }

  /// <summary>
  /// Test loading of very large database.
  /// </summary>
  public static void testLoadLargeDatabase() {
    simulateLargeDatabaseLoad(1000, 5000);
  }

  /// <summary>
  /// Simulates generation of a large in memory database
  /// </summary>
  /// <param name="rangeStart"></param>
  /// <param name="nSubjects"></param>
  public static void simulateLargeDatabaseLoad(int rangeStart, int nSubjects) {
    int[] fingers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    // int[] fingers = { 1, 2, 9, 10 };
    String fileName = MatchEngineTestUtils.getDatabaseFileName("LargeDatabaseTest");
    String dirNameSplit = MatchEngineTestUtils.getDatabaseCacheDirectory("LargeDatabaseTest");
    Subject matchSubject = MatchEngineTestUtils.enrolSubject(1, fingers, 1);

    Subset subset = new Subset();

    MatchEngineTestUtils.extendSubsetWithClones(subset, matchSubject, rangeStart, nSubjects);
    PrintMessage("Database is loaded");

    subset.exportToFile(fileName);
    PrintMessage("Database is saved to " + fileName);
    subset.clear();
  }

  /// <summary>
  /// Illustrates multi-file import/export using Subset of previous example.
  /// </summary>

  public static void testMultiFileLoad() {
    ExampleModule.PrintHeader("TestMultiFileLoad");
    String fileName = MatchEngineTestUtils.getDatabaseFileName("LargeDatabaseTest");
    String dirNameSplit = MatchEngineTestUtils.getDatabaseCacheDirectory("LargeDatabaseTest");

    PrintMessage("Importing from " + fileName);
    Subset subset = new Subset();
    subset.importFromFile(fileName);

    // Export with 500 subjects per page
    PrintMessage("exporting to multi-file");
    subset.exportToDisk(dirNameSplit, 500);
    PrintMessage("Database is saved as multiple files to " + dirNameSplit);
    subset.clear();

    // Recover from file with ImportFromDisk
    Subset readBack1 = new Subset();
    readBack1.importFromDisk(fileName);
    PrintMessage(
        "Database read back from file " + fileName + " with " + readBack1.size() + " subjects");
    readBack1.clear();

    // Recover from file with ImportFromDisk
    Subset readBack2 = new Subset();
    readBack2.importFromDisk(dirNameSplit);
    PrintMessage(
        "Database read back from directory "
            + dirNameSplit
            + " with "
            + readBack2.size()
            + " subjects");
    readBack2.clear();
  }
}
