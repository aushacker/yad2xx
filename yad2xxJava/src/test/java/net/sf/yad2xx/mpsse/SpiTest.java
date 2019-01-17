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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.sf.yad2xx.Device;
import net.sf.yad2xx.DeviceType;

/**
 * @author		Stephen Davies
 * @since		18 July 2018
 * @since		1.0
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SpiTest {

	/**
	 * Object under test.
	 */
	private Spi spi;

	@Mock
	private Device mockedDevice;
	
	@Before
	public void setUp() {
		when(mockedDevice.getType()).thenReturn(DeviceType.FT_DEVICE_232H);
		
		spi = new Spi(mockedDevice);
	}

	@Test
	public void testTransactReadWriteWithSmallBuffer() throws Exception {
		byte[] data = new byte[50];		// small buffer
		
		spi.transactReadWrite(data);

		//verify(mockedDevice).write(data);
	}

	@Test
	public void testTransactReadWriteWithMediumBuffer() throws Exception {
		byte[] data = new byte[10000];
		
		spi.transactReadWrite(data);

		//verify(mockedDevice).write(data);
	}
}
