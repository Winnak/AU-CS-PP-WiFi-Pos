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

import java.util.List;
import java.util.Vector;

import org.pi4.locutil.PositioningError;

/**
 * Provides methods to calculate statistical values.
 * 
 * @author koelsch
 * @author abiskop
 * @author king
 */
public class Statistics {

	/**
	 * Calculates the average over the specified <code>double</code> values.
	 * 
	 * @param data  input values that shall be averaged
	 * @return  the average as a <code>double</code>
     * @throws IllegalArgumentException  if the specified array is empty
	 */
	public static double avg(double[] data) {
        if (data.length == 0)
            throw new IllegalArgumentException("Array length = 0");
        int n = data.length;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += data[i];
        }
		return (sum / (double) n);
	}
  
	/**
	 * Calculates the average over the specified <code>int</code> values.
	 * 
	 * @param data  input values that shall be averaged
	 * @return  the average as a <code>double</code>
     * @throws IllegalArgumentException  if the specified array is empty
	 */
	public static double avg(int[] data) {
        if (data.length == 0)
            throw new IllegalArgumentException("Array length = 0");
        int n = data.length;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += data[i];
        }
		return (sum / (double) n);
	}
    
    /**
     * Calculates the average over the specified values.
     * 
     * @param list  input values that shall be averaged
     * @return  the average as a <code>double</code>
     * @throws IllegalArgumentException  if the specified <code>ArrayList</code> is empty
     */
	public static double avg(List<? extends Number> list) {
		if (list.size() == 0)
            throw new IllegalArgumentException("List length = 0");
		int n = list.size();
		double sum = 0;
		for (int i = 0; i < n; i++) {
			sum += list.get(i).doubleValue();
		}
		return (sum / (double) n);
	}
	
	/**
     * Calculates the average over the specified values.
     * 
     * @param list  input values that shall be averaged
     * @return  the average as a <code>double</code>
     * @throws IllegalArgumentException  if the specified <code>ArrayList</code> is empty
     */
	// TODO [TK]: fixme
	public static double avg(Vector<PositioningError> vector) {
		if (vector.size() == 0)
            throw new IllegalArgumentException("Vector length = 0");
		int n = vector.size();
		double sum = 0;
		for (int i = 0; i < n; i++) {
			sum += vector.get(i).getPositioningError();
		}
		return (sum / (double) n);
	}
		
	/**
	 * Averages k positions.
	 * 
	 * @param values	the positions
	 * @return	the averaged position
	 */
	public static GeoPosition avgPosition(List<GeoPosition> values) {
		int k = values.size();
		if (k < 1)
			throw new IllegalArgumentException("Length must be >= 1");
		double x = 0, y = 0, z = 0, o = 0;
		for (int i = 0; i < k; i++) {
			GeoPosition value = values.get(i);
			x += value.getX();
			y += value.getY();
			z += value.getZ();
			o += value.getOrientation();
		}
		x /= (double) k;
		y /= (double) k;
		z /= (double) k;
		o /= (double) k;
		return new GeoPosition(x, y, z, o % 360.0);
	}
	
	/**
	 * Averages k positions.
	 * 
	 * @param values	the positions
	 * @return	the averaged position
	 */
	public static GeoPosition avg(GeoPosition[] values) {
		int k = values.length;
		if (k < 1)
			throw new IllegalArgumentException("Length must be >= 1");
		double x = 0, y = 0, z = 0, o = 0;
		for (int i = 0; i < k; i++) {
			GeoPosition value = values[i];
			x += value.getX();
			y += value.getY();
			z += value.getZ();
			o += value.getOrientation();
		}
		x /= (double) k;
		y /= (double) k;
		z /= (double) k;
		o /= (double) k;
		return new GeoPosition(x, y, z, o % 360.0);
	}
	
	/**
     * Selects the maximum over the specified values.
     * 
     * @param list  input values that shall be investigated
     * @return  the maximum as a <code>double</code>
     * @throws IllegalArgumentException  if the specified <code>ArrayList</code> is empty
     */
	public static double max(List<? extends Number> list) {
		if (list.size() == 0)
            throw new IllegalArgumentException("List length = 0");
		double max = list.get(0).doubleValue();
		for (int i = 0; i < list.size(); i++) {
			double temp = list.get(i).doubleValue();
			if (temp > max) max = temp;
		}
		return max;
	}
	
	/**
     * Selects the minimum over the specified values.
     * 
     * @param list  input values that shall be investigated
     * @return  the minimum as a <code>double</code>
     * @throws IllegalArgumentException  if the specified <code>ArrayList</code> is empty
     */
	public static double min(List<? extends Number> list) {
		if (list.size() == 0)
            throw new IllegalArgumentException("List length = 0");
		double min = list.get(0).doubleValue();
		for (int i = 0; i < list.size(); i++) {
			double temp = list.get(i).doubleValue();
			if (temp < min) min = temp;
		}
		return min;
	}
	
	/**
	 * Calculates the median from the specified values.<br />
     * 
	 * <u>NOTE:</u><br />
     * If the number of input values is even, the median is calculated by averaging the two middle values.
     * 
     * @param data  input values
	 * @return  the median as a <code>double</code>
     * @throws IllegalArgumentException  if the specified array is empty
     * @see <a href="http://de.wikipedia.org/wiki/Median">http://de.wikipedia.org/wiki/Median</a>
	 */
	public static double median(double[] data) {
        if (data.length == 0)
            throw new IllegalArgumentException("Array length = 0");
        int n = data.length;
        double[] temp = sort(data);
        
        // Check if the number of input values is odd
        if (n % 2 == 1) {
            return temp[((n + 1) / 2) - 1];
        } else {
            double d1 = temp[n / 2];
            double d2 = temp[(n / 2) - 1];
            return (d1 + d2) / 2;
        }
	}
    
    /**
     * Calculates the median from the specified values.<br />
     * 
     * <u>NOTE:</u><br />
     * If the number of input values is even, the median is calculated by averaging the two middle values.
     * 
     * @param list  input values
     * @return  the median as a <code>double</code>
     * @throws IllegalArgumentException  if the specified <code>ArrayList</code> is empty
     * @see <a href="http://de.wikipedia.org/wiki/Median">http://de.wikipedia.org/wiki/Median</a>
     */
    public static double median(List<? extends Number> list) {
        if (list.size() == 0)
           throw new IllegalArgumentException("ArrayList size = 0");
        double[] values = new double[list.size()];
        for(int i = 0; i < list.size(); i++) {
            values[i] = list.get(i).doubleValue();
        }
        return median(values);
    }
	
    /**
     * Calculates the variance from the specified values.
     * 
     * @param data  input values
     * @return  the variance as a <code>double</code>
     * @throws IllegalArgumentException  if the specified array is empty or has only one element
     */
	public static double var(double[] data) {
        if (data.length <= 1) return 0.0;
        double avg = avg(data);
        int n = data.length;
        double sum = 0;
        for (int i = 0; i < n; i++) {
        	double placeholder = data[i] - avg;
        	sum += placeholder*placeholder;
        }
        return (sum / (double) (n - 1));
	}
	
	/**
     * Calculates the standard deviation from the specified values.
     * 
     * @param data  input values
     * @return  the standard deviation as a <code>double</code>
     * @throws IllegalArgumentException  if the specified array is empty or has only one element
     */
	public static double stdDev(double[] data) {
		return Math.sqrt(var(data));
	}

    /**
     * Calculates the variance from the specified values.
     * 
     * @param list  input values
     * @return  the variance as a <code>double</code>
     * @throws IllegalArgumentException  if the specified <code>ArrayList</code> is empty or has only one element
     */
    public static double var(List<? extends Number> list) {
        if (list.size() <= 1) return 0.0;
        double avg = avg(list);
        int n = list.size();
        double sum = 0;
        for (int i = 0; i < n; i++) {
        	double placeholder = list.get(i).doubleValue() - avg;
        	sum += placeholder * placeholder;
        }
        return (sum / (double) (n - 1));
    }
    
    /**
     * Calculates the standard deviation from the specified values.
     * 
     * @param list  input values
     * @return  the standard deviation as a <code>double</code>
     * @throws IllegalArgumentException  if the specified <code>ArrayList</code> is empty or has only one element
     */
    public static double stdDev(List <? extends Number> list ) {
    	return Math.sqrt(var(list));
    }
    
    /**
     * Calculates the variance from the specified values.
     * 
     * @param list  input values
     * @return  the variance as a <code>double</code>
     * @throws IllegalArgumentException  if the specified <code>ArrayList</code> is empty or has only one element
     */
    public static double var(Vector<PositioningError> list) {
        if (list.size() <= 1) return 0.0;
        double avg = avg(list);
        int n = list.size();
        double sum = 0;
        for (int i = 0; i < n; i++) {
        	double placeholder = list.get(i).getPositioningError() - avg;
        	sum += placeholder * placeholder;
        }
        return (sum / (double) (n - 1));
    }
    
    /**
     * Calculates the standard deviation from the specified values.
     * 
     * @param list  input values
     * @return  the standard deviation as a <code>double</code>
     * @throws IllegalArgumentException  if the specified <code>ArrayList</code> is empty or has only one element
     */
    public static double stdDev(Vector<PositioningError> list) {
    	return Math.sqrt(var(list));
    }
    
    /**
     * Calculates the Spearman rank-order correlation coefficient as referenced in
     * the "Accuracy Characterization for Metropolitan-scale Wi-Fi Localization" paper (Chapter 3.2.2).
     * This implementation is based on the correct formula according to the referenced book
     * "Numerical Recipes in C" (see figure 14.6.1, p. 640).
     * The formula was then modified to reduce the need for sqrt
     * as that caused Unit tests to fail by between 2*10e-16 and 3*10e-16 for simple values.
     * 
     * <code>r_s = \frac{\sum_i^{}{\left(R_i - \bar{R}\right) \left(R'_i - \bar{R'}\right)}}{\sqrt{sum_i^{}{\left(R_i - \bar{R}\right)}^2} \sqrt{sum_i^{}{{\left(R'_i - \bar{R'}\right)}^2}}}</code>
     * 
     * @param   r
     * @param   rs
     * @return  Spearman rank-order correlation coefficient
     * @throws  IllegalArgumentException  if arguments do not have the same length or are too short
     */
	public static double spearmanCoefficient(double[] r, double[] rs) {
		if ((r == null) || (rs == null))
			throw new IllegalArgumentException("Array cannot be null.");
		if (r.length != rs.length)
			throw new IllegalArgumentException("Arrays need to have same length.");
		if (r.length <= 1)
			throw new IllegalArgumentException("Arrays must be of length 2 or longer.");
		double mr = Statistics.avg(r); // mean über Ri
		double mrs = Statistics.avg(rs); // mean über Ri'
		double n = 0, z1 = 0, z2 = 0; // calculate sums
		for (int i = 0; i < r.length; i++) {
			double x = r[i] - mr;
			double xs = rs[i] - mrs;
			n += x * xs;
			z1 += x * x;
			z2 += xs * xs;
		}
		//double rho = n / (Math.sqrt(z1) * Math.sqrt(z2)); // original formula
		double rho = n / Math.sqrt(z1 * z2); // optimization to avoid delta, see above
		if (Double.isNaN(rho))
			throw new ArithmeticException("Spearman coefficient is NaN");
		return rho;
	}
    
    /**
     * Sorts the specified array with insertion sort.
     * 
     * @param values  an array of <code>double</code> that shall be sorted
     * @return  a sorted array of <code>double</code>
     */
    private static double[] sort(double[] values) {
        int i, j;
        int n = values.length;
        double[] a = new double[n];
        System.arraycopy(values, 0, a, 0, n);
        for(i = 1; i < n; i++) {
            double temp = a[i];
            for (j = i - 1; j >= 0 && a[j] > temp; j--) {
                a[j + 1] = a[j];
            }
            a[j + 1] = temp;
        }
        return a;
    }
    
	/**
	 * A good (polynomial and rational) approximation to the normal cumulative distribution function. 
	 *
	 * This simple polynomial and rational approximation for P(X) is from "Abramowitz and Stegun:
	 * Handbook of mathematical functions", p. 933:
	 * 
	 * Let t = 1/(1+pX) where p=.33267
	 * then P(x) = 1-Z(x)*[a_1*t + a_2*t^2 + a_3*t^3]+e(x)
	 * where a_1 = 0.4361836 a_2 = -0.1201676 a_3 = 0.9372980
	 * and |e(x)| < 1 * 10^(-5)
	 *
	 * A simple polynomial and rational approximation for Z(X) is from "Abramowitz and Stegun:
	 * Handbook of mathematical functions", p. 933:
	 * 
	 * Be a_0 = 2.490895 a_2 = 1.466003 a_4 = -0.024393 a_6 = 0.178257
	 * then Z(x) = (a_0 + a_2*x^2 + a_4*x^4 + a_6*x^6)^(-1) + e(x)
	 * with |e(x)| < 2.7*10^(-3) 
	 * 
	 * @param x_	The value that is used to calculate the probability of a value is (equal or) less than x
	 * @param avg	Mean of the gaussian distribution
	 * @param stddev	Standard deviation of the gaussian distribution
     * @return      approximation to the normal cumulative distribution function
     * @see http://www.pitt.edu/~wpilib/statfaq/gaussfaq.html
	 */
	public static double approximationCumulativeDistributionFunction(double x_, double avg, double stddev) {
		// special case: stddev == 0.0
		if (stddev == 0.0) {
			if (x_ >= avg) return 1;
			return 0;
		}
		
		double x = (x_ - avg) / stddev;
		double x_old = x;
		if (x < 0.0)
			x *= -1;
		// Z(x)
		double x_2 = x * x;
		double x_4 = x_2 * x_2;
		double x_6 = x_2 * x_4;
		double z = 2.49089 + (1.466003 * x_2) + (-0.024393 * x_4) + (0.178257 * x_6);	
		// according to the book, Z(x) would be 1/z, but for better performance, we later
		// use it as a divider instead of a factor
		
		// P(x)
		double t = 1.0 / (1.0 + (0.33267 * x));
		double t_2 = t * t;
		double t_3 = t_2 * t;
		double approx = 1 - (((0.4361836 * t) + (-0.1201676 * t_2) + (0.9372980 * t_3)) / z);
		
		if (x_old < 0.0) 
			return (1 - approx);
		else
			return approx;
	}
}
