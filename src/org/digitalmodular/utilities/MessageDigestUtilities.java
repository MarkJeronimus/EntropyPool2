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

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import static org.digitalmodular.utilities.ArrayUtilities.getLengthAfterWrap;
import static org.digitalmodular.utilities.ArrayUtilities.getLengthBeforeWrap;
import org.digitalmodular.utilities.container.LoggingCount;
import org.digitalmodular.utilities.container.LoggingVariable;

/**
 * @author Mark Jeronimus
 */
// Created 2016-08-02
public enum MessageDigestUtilities {
	;

	public static void hashByte(MessageDigest digest, byte value) {
		digest.update(value);
	}

	public static void hashShort(MessageDigest digest, short value) {
		digest.update((byte) (value >>> 8));
		digest.update((byte) value);
	}

	public static void hashInt(MessageDigest digest, int value) {
		digest.update((byte) (value >>> 24));
		digest.update((byte) (value >>> 16));
		digest.update((byte) (value >>> 8));
		digest.update((byte) value);
	}

	public static void hashLong(MessageDigest digest, long value) {
		digest.update((byte) (value >>> 56));
		digest.update((byte) (value >>> 48));
		digest.update((byte) (value >>> 40));
		digest.update((byte) (value >>> 32));
		digest.update((byte) (value >>> 24));
		digest.update((byte) (value >>> 16));
		digest.update((byte) (value >>> 8));
		digest.update((byte) value);
	}

	public static void hashChar(MessageDigest digest, char value) {
		digest.update((byte) (value >>> 24));
		digest.update((byte) (value >>> 16));
		digest.update((byte) (value >>> 8));
		digest.update((byte) value);
	}

	public static void hashFloat(MessageDigest digest, float value) {
		hashInt(digest, Float.floatToRawIntBits(value));
	}

	public static void hashDouble(MessageDigest digest, double value) {
		hashLong(digest, Double.doubleToRawLongBits(value));
	}

	public static void hashBytes(MessageDigest digest, byte[] value) {
		for (byte b : value)
			hashByte(digest, b);
	}

	public static void hashString(MessageDigest digest, String value) {
		for (int i = 0; i < value.length(); i++)
			hashChar(digest, value.charAt(i));
	}

	private static void hashObject(MessageDigest digest, Object value) {
		if (value instanceof Byte)
			hashByte(digest, (Byte) value);
		else if (value instanceof Short)
			hashShort(digest, (Short) value);
		else if (value instanceof Integer)
			hashInt(digest, (Integer) value);
		else if (value instanceof Long)
			hashLong(digest, (Long) value);
		else if (value instanceof Character)
			hashChar(digest, (Character) value);
		else if (value instanceof Float)
			hashFloat(digest, (Float) value);
		else if (value instanceof Double)
			hashDouble(digest, (Double) value);
		else if (value instanceof byte[])
			hashBytes(digest, (byte[]) value);
		else if (value instanceof String)
			hashString(digest, (String) value);
		else if (value instanceof SecureRandom)
			hashString(digest, ((SecureRandom) value).getAlgorithm());
		else if (value instanceof MessageDigest)
			hashString(digest, ((MessageDigest) value).getAlgorithm());
		else if (value instanceof Cipher)
			hashString(digest, ((Cipher) value).getAlgorithm());
		else
			throw new IllegalArgumentException("Hashing " + value.getClass() + " not yet supported");
	}

	public static void hashLoggingCount(MessageDigest digest, LoggingCount value) {
		hashInt(digest, value.get());
		hashLong(digest, value.getCountDate());
	}

	public static void hashLoggingVariable(MessageDigest digest, LoggingVariable<?> value) {
		hashObject(digest, value.get());
		hashInt(digest, value.getModifyCount());
		hashLong(digest, value.getModifyDate());
	}

	public static void hashBlockFromBuffer(MessageDigest digest, byte[] buffer, int byteOffset) {
		int digestSize = digest.getDigestLength();

		int lengthBeforeWrap = getLengthBeforeWrap(buffer.length, byteOffset, digestSize);
		int lengthAfterWrap  = getLengthAfterWrap(buffer.length, byteOffset, digestSize);

		digest.update(buffer, byteOffset, lengthBeforeWrap);
		digest.update(buffer, 0, lengthAfterWrap);
	}

	public static void getDigestToBuffer(MessageDigest digest, byte[] buffer, int byteOffset, byte[] digestBuffer) {
		int digestSize = digest.getDigestLength();

		try {
			digest.digest(digestBuffer, 0, digestSize);

			int lengthBeforeWrap = getLengthBeforeWrap(buffer.length, byteOffset, digestSize);
			int lengthAfterWrap  = getLengthAfterWrap(buffer.length, byteOffset, digestSize);

			System.arraycopy(digestBuffer, 0, buffer, byteOffset, lengthBeforeWrap);
			System.arraycopy(digestBuffer, lengthBeforeWrap, buffer, 0, lengthAfterWrap);

			Arrays.fill(digestBuffer, (byte) 0);
		} catch (DigestException ex) {
			throw new InternalError("This shouldn't happen", ex);
		}
	}
}
