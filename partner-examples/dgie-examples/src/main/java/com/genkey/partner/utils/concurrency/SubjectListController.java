package com.genkey.partner.utils.concurrency;

import com.genkey.platform.utils.ArrayIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskController that manages a list of subject that are processed across a number of concurrent
 * threads 1 or more times.
 *
 * @author Gavan
 */
public class SubjectListController implements TaskController {

  List<Number> subjectList = null;

  int index = 0;

  int loopCount = 0;

  int iterationCount = 1;

  String indexMonitor = "Lock";

  public void setSubjectList(Iterable<? extends Number> subjectList) {
    this.subjectList = new ArrayList<>();
    for (Number subject : subjectList) {
      this.subjectList.add(subject);
    }
  }

  public void setSubjectArray(long[] subjects) {
    setSubjectList(new ArrayIterator<Long>(subjects));
  }

  public void setSubjectList(long startValue, int nValues, int step) {
    subjectList = new ArrayList<>();
    for (long ix = 0; ix < nValues; ix += step) {
      subjectList.add(startValue + ix);
    }
  }

  @Override
  public boolean hasMoreTasks() {
    boolean result;
    if (subjectList == null || subjectList.size() == 0) {
      result = false;
    } else if (index < subjectList.size()) {
      result = true;
    } else {
      ++loopCount;
      if (loopCount < this.getIterationCount()) {
        index = 0;
        result = true;
      } else {
        result = false;
      }
    }
    return result;
  }

  @Override
  public TestTask getNextTask() {
    TestTask result = null;
    synchronized (indexMonitor) {
      if (hasMoreTasks()) {
        Number subject = subjectList.get(index++);
        result = new ObjectWrapperTask<>(subject);
      }
    }
    return result;
  }

  @Override
  public <T> T getNextTaskObject(Class<T> clazz) {
    T result = null;
    TestTask task = getNextTask();
    if (task != null) {
      @SuppressWarnings("unchecked")
      ObjectWrapperTask<T> wrapper = (ObjectWrapperTask<T>) (task);
      result = wrapper.getValue();
    }
    return result;
  }

  public int getIterationCount() {
    return iterationCount;
  }

  public void setIterationCount(int iterationCount) {
    this.iterationCount = iterationCount;
  }

  public int getLoopCount() {
    return loopCount;
  }
}
