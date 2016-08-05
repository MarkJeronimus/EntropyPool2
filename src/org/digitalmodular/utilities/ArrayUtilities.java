/*
 * This file is part of Utilities.
 *
 * Copyleft 2016 Mark Jeronimus. All Rights Reversed.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalmodular.utilities;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Mark Jeronimus
 */
// Created 2016-08-02
public enum ArrayUtilities {
	;

	public static void shuffle(byte[] array, Random random) {
		for (int i = array.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);

			swap(array, i, j);
		}
	}

	public static void swap(byte[] array, int i, int j) {
		if (i == j) return;

		byte temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	public static byte[] generateRandom(int length, Random random) {
		byte[] array = new byte[length];
		random.nextBytes(array);
		return array;
	}

	public static int getLengthBeforeWrap(int arrayLength, int offset, int length) {
		return Math.min(length, arrayLength - offset);
	}

	public static int getLengthAfterWrap(int arrayLength, int offset, int length) {
		return Math.max(0, offset + length - arrayLength);
	}

	public static int compareUnsigned(byte[] array1, byte[] array2) {
		boolean null1 = array1 == null;
		boolean null2 = array2 == null;

		if (null1 && null2) return 0;
		if (null1 != null2) return null1 ? -1 : 1;

		int len1   = array1.length;
		int len2   = array2.length;
		int length = Math.min(len1, len2);

		for (int i = 0; i < length; i++) {
			int byte1 = array1[i] & 0xFF;
			int byte2 = array2[i] & 0xFF;

			if (byte1 == byte2) continue;
			return byte1 < byte2 ? -1 : 1;
		}

		if (len1 == len2) return 0;
		return (len1 < len2) ? -1 : 1;
	}

	public static int compareZeroTerminatedString(byte[] array1, byte[] array2) {
		boolean null1 = array1 == null;
		boolean null2 = array2 == null;

		if (null1 && null2) return 0;
		if (null1 != null2) return null1 ? -1 : 1;

		int len1   = array1.length;
		int len2   = array2.length;
		int length = Math.min(len1, len2);

		for (int i = 0; i < length; i++) {
			int byte1 = array1[i] & 0xFF;
			int byte2 = array2[i] & 0xFF;

			if (byte1 == 0 || byte2 == 0) {
				if (byte1 == 0 && byte2 == 0) continue;
				return byte1 == 0 ? -1 : 1;
			}

			if (byte1 == byte2) continue;
			return byte1 < byte2 ? -1 : 1;
		}

		if (len1 == len2) return 0;
		return (len1 < len2) ? -1 : 1;
	}

	public static byte[] stringToZeroTerminatedString(String string, int arrayLength) {
		byte[] array;

		try {
			array = string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new LinkageError("UTF-8 not available!", ex);
		}

		if (array.length > arrayLength) {
			throw new IllegalArgumentException("String has more than " + array + " bytes: " + array.length +
			                                   ". How many characters that is depends on UTF-8.");
		}

		array = Arrays.copyOf(array, arrayLength);
		return array;
	}
}
