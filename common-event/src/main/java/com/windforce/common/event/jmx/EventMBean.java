package com.windforce.common.event.jmx;

import javax.management.MXBean;

@MXBean
public interface EventMBean {

	public String[] getEventInfo();
}
