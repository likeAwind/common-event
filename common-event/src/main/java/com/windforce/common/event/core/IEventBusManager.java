package com.windforce.common.event.core;

import com.windforce.common.event.event.IEvent;

/**
 * 事件处理器
 * @author Kuang Hao
 * @since v1.0 2018年3月9日
 *
 */
public interface IEventBusManager {

	/**
	 * 异步抛出事件
	 * @param event
	 * @param eventName
	 * @param dispatharCode
	 */
	public void submit(final IEvent event, final String eventName, final int dispatharCode);

}
