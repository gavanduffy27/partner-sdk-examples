package com.genkey.platform.test.framework;

import java.io.Externalizable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.genkey.platform.utils.AssertUtils;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FormatUtils;
import com.genkey.platform.utils.ReflectUtils;

public class GKTestCase4 {

	
	public String getTestName() {
		return Commons.classShortName(this);
	}
    
    public void runTestCase4() {
		Result result = JUnitCore.runClasses(this.getClass());
	    for (Failure failure : result.getFailures()) {
	      System.out.println(failure.toString());
	    }		   	
    }
    
    
    
	static class GKTestCaseException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public GKTestCaseException(String message) {
			super(message);
		}
		
	}

	
	static public void handleTestException(Exception e) {
		handleTestException(e, false);
	}
	
	static public void handleTestException(Exception e, boolean expected) {
		if  (!expected) {
			e.printStackTrace();
		}
		assertTrue(expected);
	}

	static long DefaultSuccessCode = 0;
	long successCode = DefaultSuccessCode;
	
	public void assertSuccess(long status) {
		assertEqual(status, getSuccessCode());
	}
	public long getSuccessCode() {
		return successCode;
	}

	public void setSuccessCode(long successCode) {
		this.successCode = successCode;
	}

	public static void sleep(long delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			
		}
	}
	

	public static void assertEqual(double x, double y) {
		AssertUtils.assertEqual(x, y);
	}
	
	public static void assertEqual(Object x, Object y) {
		AssertUtils.assertEqual(x, y);
	}
	
	public static void assertEqual(Object x, Object y, String message) {
		if (! x.equals(y)) {
			printError(message);
			assertEqual(x,y);
		}
	}
	
	public static void assertEqual(byte [] array1, byte [] array2) {
		AssertUtils.assertEqual(array1, array2);
	}

	public static void assertTextEquals(Object object1, Object object2) {
		AssertUtils.assertTextEquals(object1, object2);
	}

	public static void assertBinaryEquals(Serializable object1, Serializable object2) {
		AssertUtils.assertBinaryEquals(object1, object2);
	}
	
	public static <T> void assertEqual(Collection<T> c1, Collection<T> c2) {
		assertCollectionEqual(c1, c2);
	}
	
	public static <T> void assertCollectionEqual(Collection<T> c1, Collection<T> c2) {
		assertTrue(c1.size() == c2.size());
		Iterator<T> iter1 = c1.iterator();
		Iterator<T> iter2 = c2.iterator();
		while(iter1.hasNext()) {
			T value1 = iter1.next();
			T value2 = iter2.next();
			assertEqual(value1, value2);
		}
		
	}
	
	public static void assertTrue(boolean predicate, String message) {
		if (! predicate) {
			printError(message);
			assertTrue(predicate);
		}
	}
	
	public static void assertTrue(boolean b) {
		AssertUtils.assertTrue(b);
	}
	
	public static void assertFalse(boolean b, String message) {
		assertTrue(!b, message);
	}

	public static void assertFalse(boolean b) {
		assertTrue(!b, "Check false failure");
	}
	

	public static void printObject(Object object) {
		FormatUtils.printObject(object);
	}

	public static void printNamedObject(Object object) {
		FormatUtils.printNamedObject( object);
	}
	
	public static void printList(Collection<? extends Object> list) {
		FormatUtils.printList(list);
	}
	
	public static void printEndTestS(String testName) {
		printHeader("Test " + testName + " completed",'*');
	}

	public  void printEndTest(String testName) {
		FormatUtils.printBanner("End of test " + Commons.classShortName(this) + "::" + testName);
	}	
	
	public static void printMap(Map<? extends Object, ? extends Object> map) {
		FormatUtils.printMap(map);
	}
	
	public static void printCollection(String header, Iterable<? extends Object> coll) {
		FormatUtils.printCollection(header, coll);
	}

	public static void printCollection(Iterable<? extends Object> coll) {
		FormatUtils.printCollection(coll);
	}

	public static void print(String text) {
		FormatUtils.print(text);
	}
	
	
	public static void printError(String message) {
		printCategoryMessage("Error", message);
	}
	
	public static void printCategoryMessage(String typeName, String message) {
		printMessage(typeName + "::" + message);
	}

	public static void printMessage(String text) {
		FormatUtils.println(text);
	}
	
	
	public static void println(String text) {
		FormatUtils.println(text);
	}
	
	public static void nl() {
		FormatUtils.nl();
	}
	
	public static void printHeader(String text) {
		FormatUtils.printHeader(text);
	}
	
	public static void printHeader1(String text) {
		FormatUtils.printHeader1(text);
	}

	public static void printHeader2(String text) {
		FormatUtils.printHeader2(text);
	}

	public static void printHeader3(String text) {
		FormatUtils.printHeader3(text);
	}
	
	
	public static void printHeader(String text, char ch) {
		FormatUtils.printHeader(text, ch);
	}
	
	public static void printResult(Object value) {
		FormatUtils.printResult(value);
	}
	public static void printResult(Object name, Object value) {
		FormatUtils.printResult(name.toString(), value);
	}

	public static void printIndexResult(Object name, int index, Object value) {
		FormatUtils.printResult(indexName(name.toString(), index), value);
	}
	
	private static String indexName(String name, int index) {
		return name + "[" + index + "]";
	}

	public static void printObject(String header, Object object) {
		FormatUtils.printObject(header, object);
	}
	
	public static void serializeTest(Serializable object) {
		try {
			byte [] serialData = Commons.serializeObject(object);
			Serializable object2 = (Serializable) Commons.deSerializeObject(serialData);
			byte [] serialData2 = Commons.serializeObject(object2);
			assertEqual(serialData, serialData2);
			//assertEqual(object, object2);
			
		} catch (Exception e) {
			handleTestException(e);
		}	
	}
	
	
	public static <T> boolean checkAllEquals(Iterable<T> list) {
		Set<T> set = CollectionUtils.newSet(list);
		return set.size() < 2;
		
	}
	
	public static <T> void assertAllEquals(Iterable<T> list) {
		assertTrue(checkAllEquals(list));
	}

	public static void externalizeTest(Externalizable object) {
		externalizeTest(object, false);
	}
	
	public static void externalizeTest(Externalizable object, boolean flgZip) {
		try {
			Externalizable  object2 = (Externalizable ) TestUtils.newInstance(object);
			byte [] data1 = Commons.externalizeObject(object, flgZip);
			Commons.internalizeObject(object2, data1, flgZip);
			byte [] data2 = Commons.externalizeObject(object2, flgZip);
			assertEqual(data1, data2);
		} catch (Exception e) {
			GKTestCase.handleTestException(e);
		}
		
	}
	
    	
}
