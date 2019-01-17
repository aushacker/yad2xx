/*
 * Copyright 2012-2018 Stephen Davies
 * 
 * This file is part of yad2xx.
 * 
 * yad2xx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * yad2xx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with yad2xx.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.yad2xx;

/**
 * Indicates a failure occurred within the native library component i.e.
 * the native C library received a non-zero status code (return code) from
 * a D2XX API function call.
 * <p>
 * Fields function and status provide further detail about what actually was
 * being attempted and what the D2XX API reported.
 *
 * @author		Stephen Davies
 * @since		24 May 2012
 * @since		0.1
 */
public class FTDIException extends Exception {

	private static final long serialVersionUID = -1165467097910709048L;

	/**
	 * Enumerated status code returned by D2XX function call.
	 */
	private final FTStatus status;
	
	/**
	 * Name of D2XX function that failed. E.g. "FT_Open".
	 */
	private final String function;
	
	public FTDIException(FTStatus status, String function) {
		super("Exception in D2XX native library call.");
		this.status = status;
		this.function = function;
	}
	
	public String getFunction() {
		return function;
	}
	
	public FTStatus getStatus() {
		return status;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(super.toString());
		result.append(" Function: ");
		result.append(function);
		result.append(", Status: ");
		result.append(status);
		result.append('.');
		return result.toString();
	}
}
