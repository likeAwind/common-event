package com.windforce.common.event.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class EventNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("config", new EventBeanDefinitionParser());
	}

}
