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

package org.digitalmodular.utilities.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import org.digitalmodular.utilities.container.LoggingCount;
import org.digitalmodular.utilities.container.LoggingVariable;

/**
 * @author Mark Jeronimus
 */
// Created 2016-08-03
public enum DataIO {
	;

	public static void writeObject(DataOutput out, Object value) throws IOException {
		if (value instanceof Byte)
			out.writeByte((Byte) value);
		else if (value instanceof Short)
			out.writeShort((Short) value);
		else if (value instanceof Integer)
			out.writeInt((Integer) value);
		else if (value instanceof Long)
			out.writeLong((Long) value);
		else if (value instanceof Float)
			out.writeFloat((Float) value);
		else if (value instanceof Double)
			out.writeDouble((Double) value);
		else if (value instanceof Character)
			out.writeChar((Character) value);
		else if (value instanceof byte[])
			out.write((byte[]) value);
		else if (value instanceof String)
			out.writeUTF((String) value);
		else if (value instanceof SecureRandom)
			out.writeUTF(((SecureRandom) value).getAlgorithm());
		else if (value instanceof MessageDigest)
			out.writeUTF(((MessageDigest) value).getAlgorithm());
		else if (value instanceof Cipher)
			out.writeUTF(((Cipher) value).getAlgorithm());
		else
			throw new IllegalArgumentException("Serializing " + value.getClass().getName() + " not yet supported");
	}

	public static void writeByteArray(DataOutput out, byte[] value) throws IOException {
		out.writeInt(value.length);
		out.write(value);
	}

	public static byte[] readByteArray(DataInput in) throws IOException {
		int    length = in.readInt();
		byte[] value  = new byte[length];
		in.readFully(value);
		return value;
	}

	public static byte[] readByteArray(DataInput in, int length) throws IOException {
		byte[] value = new byte[length];
		in.readFully(value);
		return value;
	}

	public static void writeLoggingCount(DataOutput out, LoggingCount value) throws IOException {
		out.writeInt(value.get());
		out.writeLong(value.getCountDate());
	}

	public static LoggingCount readLoggingCount(DataInput in) throws IOException {
		int  value     = in.readInt();
		long countDate = in.readLong();

		LoggingCount loggingCount = new LoggingCount(value, countDate);
		return loggingCount;
	}

	public static void writeLoggingVariable(DataOutput out, LoggingVariable<?> value) throws IOException {
		writeObject(out, value.get());
		out.writeInt(value.getModifyCount());
		out.writeLong(value.getModifyDate());
	}

	public static <T> LoggingVariable<T> readLoggingVariable(T object, DataInput in) throws IOException {
		int  accessCount = in.readInt();
		long accessDate  = in.readLong();

		LoggingVariable<T> value = new LoggingVariable<>(object, accessCount, accessDate);
		return value;
	}
}
