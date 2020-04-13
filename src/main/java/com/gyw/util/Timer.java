package com.gyw.util;

import java.util.Calendar;

public class Timer {
	private long startTime = 0;
	private boolean isTimerBegin = false;
	private long timeCost = -1;
	 
	/** 
	* Begin timer
	*/
	public void tic() {
		clear();
		isTimerBegin = true;
		timeCost = -1;
		startTime = Calendar.getInstance().getTimeInMillis();
	}

	
	/** 
	* End timer
	* @return time startTimed in ms
	*/
	public long toc() {
		timeCost = Calendar.getInstance().getTimeInMillis() - startTime;
		if (isTimerBegin == false) {
			throw new RuntimeException("tic() must be called before toc()");
		} else {
			isTimerBegin = false;
			return timeCost;
		}
	}

	
	/** 
	* Begin timer; Record unit is ns 
	*/
	public void tic_accurate() {
		clear();
		isTimerBegin = true;
		timeCost = -1;
		startTime = System.nanoTime();
	}

	
	/** 
	* End timer
	* @return time startTimed in ns
	*/
	public long toc_accurate() {
		timeCost = System.nanoTime()- startTime;
		if (isTimerBegin == false) {
			throw new RuntimeException("tic() must be called before toc()");
		} else {
			isTimerBegin = false;
			return timeCost;
		}
	}

	
	/** 
	* Clear record
	*/
	public void clear() {
		startTime = 0;
		timeCost = -1;
		isTimerBegin = false;
	}
	
	
	public long getTimeCost() {
		return timeCost;
	}
}
