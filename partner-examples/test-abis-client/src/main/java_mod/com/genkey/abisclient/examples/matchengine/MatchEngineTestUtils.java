package com.genkey.abisclient.examples.matchengine;

import java.util.ArrayList;
import java.util.List;

import com.genkey.abisclient.ImageContext;
import com.genkey.abisclient.ImageData;
import com.genkey.abisclient.ReferenceDataItem;
import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.examples.utils.FileUtils;
import com.genkey.abisclient.examples.utils.TestDataManager;
import com.genkey.abisclient.matchengine.Subject;
import com.genkey.abisclient.matchengine.Subset;

public class MatchEngineTestUtils {
    static String DBPath = "dbj/subsets";
    static String DBFormat = "sdb";

    public static int[] DefaultFingers = { 3, 4, 7, 8 };


    protected static void setUp() {
        TestDataManager.setImageDirectory("images/ABISClientBMP/SingleFinger");
    }



    public static Subset loadSubset(long start, long last, int[] fingers, int sampleIndex, boolean useCache)
    {
        return useCache ? loadCachedSubset(start, last, fingers, sampleIndex) : enrollSubset(start, last, fingers, sampleIndex);
    }

    public static Subset loadSubset(int start, int last, int sampleIndex, boolean useCache)
    {
        return loadSubset(start, last, DefaultFingers, sampleIndex, useCache);
    }



    public static Subset loadCachedSubset( long start, long last, int sampleIndex)
    {
        return loadCachedSubset( start, last, DefaultFingers, sampleIndex);
    }

    public static Subset loadCachedSubset(long start, long last, int[] fingers, int sampleIndex)
    {
        Subset subset;

        // Check for cache file.
        String cacheFile = getCacheFile(start, last, fingers, sampleIndex);
        if (FileUtils.existsFile(cacheFile) )
        {
            subset = new Subset();
            subset.importFromDisk(cacheFile);
        }
        else
        {
            subset = enrollSubset(start, last, fingers, sampleIndex);
            subset.exportToFile(cacheFile);
        }
        return subset;
    }

    public static Subset enrollSubset(long start, long last, int[] fingers, int sampleIndex)
    {
        Subset subset = new Subset();

        for (long subjectId = start; subjectId <= last; subjectId++)
        {
            Subject matchSubject = enrolSubject(subjectId, fingers, sampleIndex);
            subset.addSubject(matchSubject);
        }
        return subset;
    }

    public static Subject cloneSubject(Subject source, int targetId)
    {
        byte[] encoding = source.getEncoding();
        String name = "subject_" + targetId;
        Subject clone = new Subject(encoding);
        //clone.Name = name;
        clone.setSubjectLongID(targetId);
        return clone;
    }


    public static void extendSubsetWithClones(Subset subset, Subject subject, int startId, int nSubjects)
    {
        int last = startId + nSubjects;
        //int count
        for (int ix = 0; ix <= nSubjects; ix++)
        {
            int subjectId = ix + startId;
            Subject clone = cloneSubject(subject, subjectId);
            subset.addSubject(clone);
            if (ix % 100 == 0 && ix > 0)
            {
                ExampleModule.PrintMessage("Loaded " + ix + " subjects");
            }
            if (ix % 1000 == 0 && ix > 0)
            {
            	ExampleModule.PrintHeader("Loaded " + ix + " subjects");
            }
        }
    }

    static String getCacheFile(long start, long last, int[] fingers, int sampleIndex)
    {
        String subsetName = getSubsetName(start, last, fingers, sampleIndex);
        return getDatabaseFileName(subsetName);

    }

    static String getSubsetName(long start, long last, int[] fingers, int sampleIndex)
    {
        return "db_" + start + "_" + (last - start + 1) + "_" + fingers.length + "_" +sampleIndex;
    }

    public static String getDatabaseFileName(String dbName)
    {
        return FileUtils.expandConfigFile(DBPath, dbName, DBFormat);
    }

    public static String getDatabaseCacheDirectory(String dbName)
    {
        return FileUtils.expandConfigPath(DBPath, dbName + "_mf");
    }

    public static Subject enrolSubject(long subjectId, int sampleIndex)
    {
        return enrolSubject(subjectId, MatchEngineTestUtils.DefaultFingers, sampleIndex);
    }

    public static Subject enrolSubject(long subjectId, int[] fingers, int sampleIndex)
    {
        Subject subject = new Subject(subjectId);
        ExampleModule.PrintHeader("Enrolling subject " + subjectId);
        List<ReferenceDataItem> referenceList = new ArrayList<>();
        List<String> fileNames = TestDataManager.getImageFiles((int) subjectId, fingers, sampleIndex);
        for (int ix = 0; ix < fingers.length; ix++)
        {
            ExampleModule.PrintMessage("Enrolling finger " + fingers[ix]);
            ImageData image = TestDataManager.loadImage(fileNames.get(ix));
            ImageContext context = new ImageContext(image, fingers[ix]);
            ReferenceDataItem reference = context.getReferenceData();
            //MatchReference matchReference = new MatchReference(reference);
            subject.addReference(reference);
        }
        return subject;
    }

}
