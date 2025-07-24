package com.genkey.partner.utils.concurrency;

public class ObjectWrapperTask<T> implements TestTask {
	
	T value;
	
	
	public ObjectWrapperTask(T value) {
		setValue(value);
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
	

}
