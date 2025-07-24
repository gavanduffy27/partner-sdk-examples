package com.genkey.abisclient.examples;

import java.util.ArrayList;
import java.util.List;

public abstract class ExampleSuite extends ExampleModule{

	List<IExampleTest> testSuite = new ArrayList<IExampleTest>();
	
	public void runTestExamples() {
		initExampleSuite();
		for(IExampleTest example : testSuite) {
			example.runTestExamples();
		}
	}
	
	abstract protected void initExampleSuite(); 

	protected void addExample(IExampleTest test) {
		testSuite.add(test);
	}

	@Override
	public UserMessageHandler getUserMessageHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void runAllExamples() {
		runTestExamples();
	}

	
}
