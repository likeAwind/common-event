package com.windforce.common.event.core;

import com.windforce.common.event.event.IEvent;

public interface IEventBusManager {

	public void submit(final IEvent event, final String eventName, final int dispatharCode);

}
