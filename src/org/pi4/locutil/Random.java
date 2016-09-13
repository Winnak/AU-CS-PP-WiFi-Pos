/*
 * This file is part of Locutil2.
 * 
 * Copyright (c) 2007 Thomas King <king@informatik.uni-mannheim.de>,
 * University of Mannheim, Germany
 * 
 * All rights reserved.
 *
 * Loclib is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Loclib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Loclib; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.pi4.locutil;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * This class is a wrapper class for the JDK Random class. The idea is to provide a loceva specific
 * Random class that is configured during loceva startup. This class should be used instead of the
 * JDK Random class to make sure the loceva specific configuration is applied.
 *  
 * @author king
 *
 */
public class Random {
	private static SecureRandom randomizer;
	private static long seed; 
	
	static {
		try {
			randomizer = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException nsae) {
			System.out.println(nsae.getMessage());
		}		
		byte temp[] = randomizer.generateSeed(4); 
		seed = ((long)temp[0] << 24) + (temp[1] << 16) + (temp[2] << 8) + temp[0];
		randomizer.setSeed(seed);
	}
	
	private Random() {
	}
	
	public static boolean nextBoolean() {
		return randomizer.nextBoolean();
	}
	
	public static void nextBytes(byte[] bytes) {
		randomizer.nextBytes(bytes);
	}
	
	public static double nextDouble() {
		return randomizer.nextDouble();
	}
	
	public static float nextFloat() {
		return randomizer.nextFloat();
	}
	
	public static double nextGaussian() {
		return randomizer.nextGaussian();
	}
	
	public static int nextInt() {
		return randomizer.nextInt();
	}
	
	public static int nextInt(int n) {
		return randomizer.nextInt(n);
	}
	
	public static long nextLong() {
		return randomizer.nextLong();
	}
	
	public static void setSeed(long seed) {
		randomizer.setSeed(seed);
	}
	
	public static long getSeed() {
		return seed;
	}
}
