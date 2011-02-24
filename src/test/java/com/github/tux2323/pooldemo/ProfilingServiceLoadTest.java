package com.github.tux2323.pooldemo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProfilingServiceLoadTest {

    private ProfilingService profilingService;

    class MyThread extends Thread {
	
	final int id;
	
	public MyThread(final int id) {
	    this.id = id;
	}
	
    }
    
    @Before
    public void setup() {
	profilingService = new ProfilingService();
    }

    @Test
    public void testAddProfilingEvent() throws Exception {
	final int capacity = 100000;
	final int parallelThreads = 10;
	final long maxTime = 5;
	
	// generate test data
	Queue<Thread> threads = new ArrayBlockingQueue<Thread>(capacity);
	for (int i = 0; i < capacity; i++) {
	    Thread thread = new MyThread(i) {
		@Override
		public void run() {
		    UUID.randomUUID();
		    long start = System.currentTimeMillis();
		    ProfilingEvent event = new ProfilingEvent();
		    event.setId(String.valueOf(id));
		    event.setTime(id);
		    profilingService.addProfilingEvent(event);
		    long stop = System.currentTimeMillis();
		    long time = stop - start;
		    if (time > maxTime)
			System.out.println(time);
		}

	    };
	    threads.add(thread);
	}
	
	profilingService.start();
	
	// Run load test
	while(threads.size() > 0)
	{
	    Queue<Thread> runThreads = new ArrayBlockingQueue<Thread>(parallelThreads);
	    for (int i = 0; i < parallelThreads; i++) {
		Thread thread = threads.poll();
		runThreads.add(thread);
	    }
	    for (Thread thread : runThreads) {
		thread.start();
	    }
	    for (Thread thread : runThreads) {
		thread.join();
	    }
	}
	Thread.sleep(profilingService.getCompactionIntervall() + 1000);
	assertEquals(0, profilingService.getEventsCount());
	assertEquals(0, profilingService.getJobsCount());
    }

    @After
    public void tearDown() {
	profilingService.stop();
    }

}
