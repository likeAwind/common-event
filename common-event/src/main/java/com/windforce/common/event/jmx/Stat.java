package com.windforce.common.event.jmx;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.windforce.common.event.event.IEvent;

public class Stat {

	private AtomicLong totalPackets = new AtomicLong();
	private AtomicLong totalTimes = new AtomicLong();

	private ConcurrentMap<Class<? extends IEvent>, EventStat> eventStatMap = new ConcurrentHashMap<Class<? extends IEvent>, EventStat>();

	public AtomicLong getTotalPackets() {
		return totalPackets;
	}

	public void setTotalPackets(AtomicLong totalPackets) {
		this.totalPackets = totalPackets;
	}

	public AtomicLong getTotalTimes() {
		return totalTimes;
	}

	public void setTotalTimes(AtomicLong totalTimes) {
		this.totalTimes = totalTimes;
	}

	public ConcurrentMap<Class<? extends IEvent>, EventStat> getEventStatMap() {
		return eventStatMap;
	}

	public void setEventStatMap(ConcurrentMap<Class<? extends IEvent>, EventStat> eventStatMap) {
		this.eventStatMap = eventStatMap;
	}

	public void addEvent(Class<? extends IEvent> clz, long use, boolean over) {
		getEventStat(clz).add(use, over);
		totalPackets.incrementAndGet();
		totalTimes.addAndGet(use);
	}

	private EventStat getEventStat(Class<? extends IEvent> clz) {
		EventStat stat = eventStatMap.get(clz);
		if (stat == null) {
			stat = new EventStat(clz.getName());
			EventStat pre = eventStatMap.put(clz, stat);
			if (pre != null) {
				stat = pre;
			}
		}
		return stat;
	}

	public String[] getEventInfo() {
		List<String> result = new LinkedList<String>();
		long totalPacket = totalPackets.get();
		long totalTime = totalTimes.get() / 1000000;
		result.add("totalPacket : " + totalPacket);
		result.add("totalTime : " + totalTime);

		TreeSet<EventStat> set1 = new TreeSet<EventStat>();

		for (Entry<Class<? extends IEvent>, EventStat> entry : eventStatMap.entrySet()) {
			set1.add(entry.getValue());
		}

		for (EventStat stat : set1) {
			result.add(stat.toString());
		}

		return result.toArray(new String[result.size()]);
	}

	public class EventStat implements Comparable<EventStat> {
		private final AtomicLong eventTimes = new AtomicLong();
		private final AtomicLong eventTotalTime = new AtomicLong();
		private final AtomicLong eventOverTimes = new AtomicLong();

		private final String className;

		public EventStat(String className) {
			this.className = className;
		}

		public void add(long use, boolean over) {
			eventTimes.incrementAndGet();
			eventTotalTime.addAndGet(use);
			if (over) {
				eventOverTimes.incrementAndGet();
			}
		}

		public String toString() {
			long totalPacket = totalPackets.get();
			long packetTimes = eventTimes.get();
			long eventTotalTimes = eventTotalTime.get() / 1000000;

			float packetTimeOpps = packetTimes * 1.0f / totalPacket * 100;
			float averageTime = eventTotalTimes * 1.0f / packetTimes;
			long overTime = eventOverTimes.get();

			return String
					.format("[name : %s]  [packetTimes : %d] [packetOpps : %02.2f%%] [averageTime : %02.2fms] [totalTimes : %dms] [overTime : %d]",
							className, packetTimes, packetTimeOpps, averageTime, eventTotalTimes, overTime);
		}

		@Override
		public int compareTo(EventStat o) {
			long result = o.eventTotalTime.get() - eventTotalTime.get();
			if (result > 0) {
				return 1;
			} else if (result < 0) {
				return -1;
			} else {
				return o.hashCode() - hashCode();
			}
		}

		public AtomicLong getEventTimes() {
			return eventTimes;
		}

		public AtomicLong getEventTotalTime() {
			return eventTotalTime;
		}

		public AtomicLong getEventOverTimes() {
			return eventOverTimes;
		}

		public String getClassName() {
			return className;
		}

	}
}
