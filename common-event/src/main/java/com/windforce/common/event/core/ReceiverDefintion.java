package com.windforce.common.event.core;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

import com.windforce.common.event.event.IEvent;

public class ReceiverDefintion implements IReceiverInvoke {
	private final Object bean;
	private final Method method;
	private final Class<? extends IEvent> clz;

	private ReceiverDefintion(Object bean, Method method, Class<? extends IEvent> clz) {
		this.bean = bean;
		this.method = method;
		this.clz = clz;
	}

	public Class<? extends IEvent> getClz() {
		return clz;
	}

	public void invoke(IEvent event) {
		ReflectionUtils.makeAccessible(method);
		ReflectionUtils.invokeMethod(method, bean, event);
	}

	@SuppressWarnings("unchecked")
	public static ReceiverDefintion valueOf(Object bean, Method method) {
		Class<? extends IEvent> clz = null;
		Class<?>[] clzs = method.getParameterTypes();
		if (clzs.length != 1) {
			throw new IllegalArgumentException("class" + bean.getClass().getSimpleName() + " method" + method.getName()
					+ " must only has one parameter Exception");
		}
		if (!IEvent.class.isAssignableFrom(clzs[0])) {
			throw new IllegalArgumentException("class" + bean.getClass().getSimpleName() + " method" + method.getName()
					+ " must only has one [IEvent] type parameter Exception");
		}
		clz = (Class<? extends IEvent>) clzs[0];
		return new ReceiverDefintion(bean, method, clz);
	}

	public static ReceiverDefintion valueOf(Object bean, Method method, Class<? extends IEvent> clz) {
		return new ReceiverDefintion(bean, method, clz);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bean == null) ? 0 : bean.hashCode());
		result = prime * result + ((clz == null) ? 0 : clz.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReceiverDefintion other = (ReceiverDefintion) obj;
		if (bean == null) {
			if (other.bean != null)
				return false;
		} else if (!bean.equals(other.bean))
			return false;
		if (clz == null) {
			if (other.clz != null)
				return false;
		} else if (!clz.equals(other.clz))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	public Object getBean() {
		return bean;
	}

	public Method getMethod() {
		return method;
	}

}
