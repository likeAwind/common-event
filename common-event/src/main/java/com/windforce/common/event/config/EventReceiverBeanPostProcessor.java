package com.windforce.common.event.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;

import com.windforce.common.event.core.EventBusManager;

public class EventReceiverBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, Ordered {

	private ApplicationContext applicationContext;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		try {
			getEventBusManager().registReceiver(bean);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return bean;
	}

	private EventBusManager getEventBusManager() {
		return applicationContext.getBean(EventBusManager.class);
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public int getOrder() {
		return Integer.MAX_VALUE;
	}

}
