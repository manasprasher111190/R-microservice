package com.R.microservice;

import java.util.List;

class TwoD {

	private List<Services> command;

	public List<Services> getCommand() {
		return command;
	}

	public void setCommand(List<Services> services) {
		this.command = services;
	}

	@Override
	public String toString() {
		return "TwoD [services=" + command + "]";
	}
	
	
}
