package com.genkey.partner.utils.concurrency;

import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FormatUtils;
import com.genkey.platform.utils.StringUtils;

/**
 * Base class implementaton of ConcurrentTaskThread
 * @author Gavan
 *
 */
public abstract class AbstractTestThread  implements ConcurrentTaskThread{

	
	
	TaskController taskController;
	
	boolean stopped=false;
	
	boolean finished=false;
	
	
	long threadID=-1;
	
	
	
	public long getThreadID() {
		if (threadID < 0) {
			this.threadID = Thread.currentThread().getId();
		}
		return threadID;
	}


	@Override
	public void setThreadID(long threadID) {
		this.threadID = threadID;
	}


	@Override
	public void stop() {
		setStopped(true);
	}


	public boolean isStopped() {
		return stopped;
	}


	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}


	public boolean isFinished() {
		return finished;
	}


	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	
	
	@Override
	public void waitFinish() {
		while (this.isFinished()) {
			Commons.waitMillis(1000);
		}
	}

	@Override
	public void run() {
		this.setFinished(false);
		TaskController controller = this.getTaskController();
		while(controller.hasMoreTasks() && ! this.isStopped()) {
			TestTask task = controller.getNextTask();
			if (task != null) {
				executeTask(task);
			} else {
				
			}
		}
		this.setFinished(true);
	}


	public TaskController getTaskController() {
		return taskController;
	}


	public void setTaskController(TaskController taskController) {
		this.taskController = taskController;
	}
	
	
	public void printMessage(String message) {
		String contextMessage = messageContext(message);
		FormatUtils.println(contextMessage);
	}

	public void printResult(Object message, Object result) {
		FormatUtils.printResult(messageContext(message.toString()), StringUtils.asString(result));
	}

	protected String messageContext(String message) {
		return getMessageContext() + ": " + message;
	}
	
	protected String getMessageContext() {
		return Commons.classShortName(this) + "["  + this.getThreadID() +"]";
	}
	

	
	
}
