package com.windforce.common.event.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.windforce.common.event.anno.ReceiverAnno;
import com.windforce.common.event.event.IEvent;
import com.windforce.common.event.util.EnhanceUtil;
import com.windforce.common.threadpool.AbstractDispatcherHashCodeRunable;
import com.windforce.common.threadpool.IdentityEventExecutorGroup;

public class EventBusManager implements IEventBusManager {

	private static final Logger logger = LoggerFactory.getLogger(EventBusManager.class);

	private Map<Class<? extends IEvent>, List<IReceiverInvoke>> ReceiverDefintionMap = new HashMap<Class<? extends IEvent>, List<IReceiverInvoke>>();

	private static IEventBusManager self;

	public EventBusManager() {
		init();
	}

	private synchronized void init() {
		self = this;
	}

	public static IEventBusManager getInstance() {
		return self;
	}

	public void submit(final IEvent event, final String eventName, final int dispatharCode) {
		IdentityEventExecutorGroup.addTask(new AbstractDispatcherHashCodeRunable() {

			@Override
			public int getDispatcherHashCode() {
				return dispatharCode;
			}

			@Override
			public String name() {
				return eventName;
			}

			@Override
			protected void doRun() {
				doSubmitEvent(event);
			}

		});
	}

	protected void doSubmitEvent(IEvent event) {
		try {
			List<IReceiverInvoke> defintions = getReceiversByEvent(event);
			if (defintions != null) {
				for (IReceiverInvoke defintion : defintions) {
					try {
						defintion.invoke(event);
					} catch (Exception e) {
						logger.error("事件处理异常", e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("事件处理异常", e);
		}
	}

	private List<IReceiverInvoke> getReceiversByEvent(IEvent event) {
		Class<?> clz = event.getClass();
		List<IReceiverInvoke> temp = ReceiverDefintionMap.get(clz);
		if (temp == null || temp.isEmpty()) {
			logger.warn("no any receivers found for event : " + event.getClass());
		}
		return temp;
	}

	public void registReceiver(Object bean) throws Exception {
		Class<?> clz = bean.getClass();
		Method[] methods = clz.getDeclaredMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(ReceiverAnno.class)) {
				ReceiverDefintion defintion = ReceiverDefintion.valueOf(bean, method);
				registDefintion(defintion.getClz(), EnhanceUtil.createReceiverInvoke(defintion));
			}
		}
	}

	public void registReceiver(Object bean, Method method, Class<? extends IEvent> clazz) throws Exception {
		registDefintion(clazz, EnhanceUtil.createReceiverInvoke(ReceiverDefintion.valueOf(bean, method, clazz)));
	}

	private void registDefintion(Class<? extends IEvent> clz, IReceiverInvoke defintion) {
		if (!ReceiverDefintionMap.containsKey(clz)) {
			ReceiverDefintionMap.put(clz, new CopyOnWriteArrayList<IReceiverInvoke>());
		}
		if (!ReceiverDefintionMap.get(clz).contains(defintion)) {
			ReceiverDefintionMap.get(clz).add(defintion);
		}
	}

}
