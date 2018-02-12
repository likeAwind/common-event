package com.windforce.common.event.monitor;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.windforce.common.event.core.EventBusManager;
import com.windforce.common.event.event.IEvent;
import com.windforce.common.event.jmx.EventMBean;
import com.windforce.common.event.jmx.Stat;

public class MonitorEventBusManager extends EventBusManager implements EventMBean {

	private static final int maximumRuntimeInNanoWithoutWarning = 1000000;

	private static final Stat stat = new Stat();

	public MonitorEventBusManager() {
		super();
		regist();
	}

	private void regist() {
		// 注册监控
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName(this + ":type=EventMBean");
			mbs.registerMBean(this, name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void doSubmitEvent(IEvent event) {
		long start = System.nanoTime();
		super.doSubmitEvent(event);
		long use = System.nanoTime() - start;
		stat.addEvent(event.getClass(), use, use > maximumRuntimeInNanoWithoutWarning);
	}

	@Override
	public String[] getEventInfo() {
		return stat.getEventInfo();
	}

	public static Stat getStat() {
		return stat;
	}

}
