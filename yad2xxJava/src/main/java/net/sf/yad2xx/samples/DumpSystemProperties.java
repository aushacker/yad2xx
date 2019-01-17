/*
 * Copyright 2015-2016 Stephen Davies
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
package net.sf.yad2xx.samples;

import java.util.Enumeration;

/**
 * <p>Utility program that dumps the JVM's properties to the console. Useful
 * for working out where to put the native library. Does not use any D2XX
 * library functions.</p>
 * 
 * <p>A better solution is: java -XshowSettings:all. Leaving this as documentation.</p>
 * 
 * @author Stephen Davies
 * @since 14 April 2016
 * @since 0.4
 * @deprecated
 */
public class DumpSystemProperties {

	public static void main(String[] args) {
		
		Enumeration<?> names = System.getProperties().propertyNames();
		
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String value = System.getProperty(name);
			System.out.print(name);
			System.out.print("=");
			System.out.println(value);
		}

	}

}
