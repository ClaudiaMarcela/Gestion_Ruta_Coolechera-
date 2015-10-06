/**
 * 
 */
package com.android.data;

import java.io.Serializable;

/**
 * Permite dar una jerarquia al tiempo
 * @author JICZ4
 *
 */
public class Tiempo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6076886037400996340L;
	public int year;
	public int Month;
	public int dayOfMonth;
	public int hour;
	public int minute;
	public String lastUpdateTime;
}
