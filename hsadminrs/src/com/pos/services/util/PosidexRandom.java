/*
 * Copyright � 2010 Posidex Technologies Pvt Ltd. All rights reserved.
 * Class: com.posidex.util.PosidexRandom.java
 * Modified On: Mar 23, 2011 12:00:30 PM
 */
package com.pos.services.util;

import java.util.GregorianCalendar;
import java.util.Random;

//import org.apache.commons.logging.Log;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Class for generating a random number</p>.
 *
 * @author vista16
 * @version $$Revision: 1.1.1.1 $$
 */
public class PosidexRandom {
	
	/** The log. */
	private static Log log = LogFactory.getLog(PosidexRandom.class);
	
	/** The random. */
	private static Random random = new Random();
	
	/**
	 * Next random number.
	 *
	 * @return the long
	 */
	public static long nextRandomNumber()
	{
		long randomLong = random.nextInt(99);
		long currentNano = System.nanoTime();//getCurrentTimeStamp();
		long totalRandom = currentNano*1000+randomLong;
		if (totalRandom < 0){
			totalRandom = totalRandom * -1;
		}
		log.info("totalRandom :"+ totalRandom); //$NON-NLS-1$
		return totalRandom;
	}
	
	public static long nextPasswordRandomNumber()
	{
		long randomLong = random.nextInt(999999999);
		
		long totalRandom = randomLong;
		if (totalRandom < 0){
			totalRandom = totalRandom * -1;
		}
		//log.info("totalRandom :"+ totalRandom); //$NON-NLS-1$
		return totalRandom;
	}
	
	/**
	 * Gets the current time stamp.
	 *
	 * @return the current time stamp
	 */
	public static long getCurrentTimeStamp()
	{
		GregorianCalendar newCalendar = new GregorianCalendar();
		return newCalendar.getTimeInMillis();
	}	
	
	/*public static void main(String[] args) 
	{
		logger.debug(nextRandomNumber());
	}*/
}
