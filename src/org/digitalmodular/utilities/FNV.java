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

/**
 * Implementation of the Fowler-Noll-Vo hash function for fast hash table use.
 * <p>
 * FNV hashes are designed to be fast while maintaining a low collision rate. The FNV speed allows one to quickly hash
 * lots of data while maintaining a reasonable collision rate. The high dispersion of the FNV hashes makes them well
 * suited for hashing nearly identical strings such as URLs, hostnames, filenames, text, IP addresses, etc.
 * <p>
 * This class implements variation FNV-1a, as recommended by
 * <a href="http://www.isthe.com/chongo/tech/comp/fnv/">http://www.isthe.com/chongo/tech/comp/fnv/</a>.
 *
 * @author Mark Jeronimus
 */
// Created 2016-08-01
public enum FNV {
	;

	public static final int OFFSET_BASIS = 0x811C9DC5;
	public static final int PRIME        = 0x01000193;

	public static int startFNV() { return OFFSET_BASIS; }

	public static int hashFNV(int hashCode, Object value) {
		if (value == null)
			return hashCode * PRIME;
		else
			return hashFNV(hashCode, value.hashCode());
	}

	public static int hashFNV(int hashCode, byte value) {
		hashCode = (hashCode ^ (value & 0xFF)) * PRIME;
		return hashCode;
	}

	public static int hashFNV(int hashCode, short value) {
		hashCode = (hashCode ^ ((value >> 8) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ (value & 0xFF)) * PRIME;
		return hashCode;
	}

	public static int hashFNV(int hashCode, int value) {
		hashCode = (hashCode ^ ((value >> 24) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ ((value >> 16) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ ((value >> 8) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ (value & 0xFF)) * PRIME;
		return hashCode;
	}

	public static int hashFNV(int hashCode, long value) {
		hashCode = (hashCode ^ (int) ((value >> 56) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ (int) ((value >> 48) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ (int) ((value >> 40) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ (int) ((value >> 32) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ (int) ((value >> 24) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ (int) ((value >> 16) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ (int) ((value >> 8) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ (int) (value & 0xFF)) * PRIME;
		return hashCode;
	}

	public static int hashFNV(int hashCode, float value) {
		return hashFNV(hashCode, Float.floatToIntBits(value));
	}

	public static int hashFNV(int hashCode, double value) {
		return hashFNV(hashCode, Double.doubleToLongBits(value));
	}

	public static int hashFNV(int hashCode, char value) {
		hashCode = (hashCode ^ ((value >> 8) & 0xFF)) * PRIME;
		hashCode = (hashCode ^ (value & 0xFF)) * PRIME;
		return hashCode;
	}
}
