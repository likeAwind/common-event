package com.windforce.common.event.core;

import com.windforce.common.event.event.IEvent;

public interface IReceiverInvoke {

	public Object getBean();

	public void invoke(IEvent event);

}
