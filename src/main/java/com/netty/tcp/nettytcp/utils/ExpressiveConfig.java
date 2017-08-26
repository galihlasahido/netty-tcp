package com.netty.tcp.nettytcp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ExpressiveConfig implements EnvironmentAware {

	@Autowired
	private Environment environment;

	@Override
	public void setEnvironment(Environment arg0) {
		this.environment = arg0;
	}
	
	public String getProperty(String args) {
		return environment.getProperty(args);
	}
}