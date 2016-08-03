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
import java.util.Arrays;
import static org.digitalmodular.utilities.ArrayUtilities.getLengthAfterWrap;
import static org.digitalmodular.utilities.ArrayUtilities.getLengthBeforeWrap;
import org.digitalmodular.utilities.container.LoggingCount;
import org.digitalmodular.utilities.container.LoggingLong;

/**
 * @author Mark Jeronimus
 */
// Created 2016-08-02
public enum MessageDigestUtilities {
	;

	public static void hashInt(MessageDigest digest, int value) {
		digest.update((byte) (value >> 24));
		digest.update((byte) (value >> 16));
		digest.update((byte) (value >> 8));
		digest.update((byte) value);
	}

	public static void hashLong(MessageDigest digest, long value) {
		digest.update((byte) (value >> 56));
		digest.update((byte) (value >> 48));
		digest.update((byte) (value >> 40));
		digest.update((byte) (value >> 32));
		digest.update((byte) (value >> 24));
		digest.update((byte) (value >> 16));
		digest.update((byte) (value >> 8));
		digest.update((byte) value);
	}

	public static void hashLoggingCount(MessageDigest digest, LoggingCount value) {
		hashLong(digest, value.getCount());
		hashLong(digest, value.getModifyDate());
	}

	public static void hashLoggingLong(MessageDigest digest, LoggingLong value) {
		hashLong(digest, value.getValue());
		hashLong(digest, value.getModifyDate());
		hashInt(digest, value.getModifyCount());
	}

	public static void hashBlockFromBuffer(MessageDigest digest, byte[] buffer, int byteOffset) {
		int digestSize = digest.getDigestLength();

		int lengthBeforeWrap = getLengthBeforeWrap(buffer.length, byteOffset, digestSize);
		int lengthAfterWrap  = getLengthAfterWrap(buffer.length, byteOffset, digestSize);

		digest.update(buffer, byteOffset, lengthBeforeWrap);
		digest.update(buffer, 0, lengthAfterWrap);
	}

	public static void getDigestToBuffer(MessageDigest digest, byte[] buffer, int byteOffset,	                                     byte[] digestBuffer) {
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
