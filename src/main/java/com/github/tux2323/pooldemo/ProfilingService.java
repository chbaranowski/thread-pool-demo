package com.github.tux2323.pooldemo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class ProfilingService {

    private final Timer timer = new Timer();

    private final AtomicLong jobsCounter = new AtomicLong();

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    private final Map<String, Integer> events = new HashMap<String, Integer>();

    private final Object lock = new Object();

    private int maxJobCount = 1000;

    private long compactionIntervall = 5000;

    private final class AddProfilingEventCommand implements Runnable {

	private final ProfilingEvent event;

	public AddProfilingEventCommand(final ProfilingEvent event) {
	    this.event = event;
	}

	@Override
	public void run() {
	    // Add profiling event for compaction
	    synchronized (lock) {
		events.put(event.getId(), event.getTime());
	    }
	    jobsCounter.decrementAndGet();
	}

    }

    private final class CompactionTask extends TimerTask {

	@Override
	public void run() {
	    // do compaction
	    synchronized (lock) {
		System.out.println("--");
		System.out.println("START compact - waiting add profing events jobs : " + jobsCounter.get());
		System.out.println("Events Size : " + events.size());
		Set<String> keySet = events.keySet();
		for (String key : keySet) {
		    Integer time = events.get(key);
		}
		events.clear();
		System.out.println("STOP compact - waiting add profing events jobs :" + jobsCounter.get());
		System.out.println("--");
	    }
	}

    }

    public void start() {
	final CompactionTask task = new CompactionTask();
	timer.schedule(task, 0, compactionIntervall);
    }

    public void stop() {
	timer.cancel();
	executor.shutdown();
    }

    public void addProfilingEvent(ProfilingEvent event) {
	if (jobsCounter.get() < maxJobCount) {
	    jobsCounter.incrementAndGet();
	    final AddProfilingEventCommand command = new AddProfilingEventCommand(
		    event);
	    executor.execute(command);
	} else {
	    System.out.println("Too much Jobs !!!");
	}
    }

    public void setMaxJobCount(int maxJobCount) {
	this.maxJobCount = maxJobCount;
    }

    public int getMaxJobCount() {
	return maxJobCount;
    }

    public void setCompactionIntervall(long compactionIntervall) {
	this.compactionIntervall = compactionIntervall;
    }

    public long getCompactionIntervall() {
	return compactionIntervall;
    }

    public int getEventsCount() {
	synchronized (lock) {
	    return events.size();
	}
    }
    
    public long getJobsCount(){
	return jobsCounter.get();
    }
    
}
