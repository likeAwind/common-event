package com.windforce.common.event.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.windforce.common.event.core.EventBusManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class EventReceiverBeanPostProcessorTest {

	@Test
	public void test() {
		for (int i = 0; i < 1000; i++) {
			EventBusManager.getInstance().submit(new TestEvent(), "test", 1);
		}
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
