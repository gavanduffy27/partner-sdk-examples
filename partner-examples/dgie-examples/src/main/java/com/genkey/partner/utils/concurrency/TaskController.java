package com.genkey.partner.utils.concurrency;


/**
 * A provider of tasks to a ConcurrencyTestRunner
 * @author Gavan
 *
 */
public interface TaskController {
	boolean hasMoreTasks();
	TestTask getNextTask();
	<T> T getNextTaskObject(Class<T> clazz);
}
