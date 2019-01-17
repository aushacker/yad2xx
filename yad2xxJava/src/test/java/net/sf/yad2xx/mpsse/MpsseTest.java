/*
 * Copyright 2018 Stephen Davies
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
package net.sf.yad2xx.mpsse;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.DeviceType;

/**
 * @author		Stephen Davies
 * @since		18 July 2018
 * @since		1.0
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MpsseTest {

	/**
	 * Object under test.
	 */
	private Mpsse mpsse;

	private Device mockedDevice;
	
	@Before
	public void setUp() {
		mockedDevice = mock(Device.class);
		when(mockedDevice.getType()).thenReturn(DeviceType.FT_DEVICE_232H);
		
		mpsse = new Mpsse(mockedDevice);
	}

	/**
	 * Constructor should fail when a non-MPSSE capable device is provided.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testFailureWithNonMpsseCapableDevice() {
		mockedDevice = mock(Device.class);
		when(mockedDevice.getType()).thenReturn(DeviceType.FT_DEVICE_100AX);
		
		mpsse = new Mpsse(mockedDevice);
	}
	
	@Test
	public void testClose() throws Exception {
		mpsse.close();
		
		verify(mockedDevice).close();
	}
}
