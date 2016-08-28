/*
 * This file is part of EntropyPool2.
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

package org.digitalmodular.entropypool;

import java.io.DataOutput;
import java.io.IOException;

import static org.digitalmodular.utilities.io.DataIO.*;
import static org.digitalmodular.entropypool.EntropyPool.*;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
enum EntropyPool2Saver {
	;

	static void writeTo(EntropyPool2 pool, DataOutput out) throws IOException {
		writeHeader(out);
		writePool(pool, out);
	}

	private static void writeHeader(DataOutput out) throws IOException {
		out.writeBytes(MAGIC);
		out.writeUTF(PROGRAM_TITLE);

		CURRENT_VERSION.writeTo(out);
	}

	private static void writePool(EntropyPool2 pool, DataOutput out) throws IOException {
		out.writeLong(pool.getCreateDate());
		writeLoggingCount(out, pool.accessCount());

		writeLoggingReference(out, pool.secureRandom());
		writeLoggingReference(out, pool.messageDigest());
		writeLoggingReference(out, pool.cipher());

		writeLoggingReference(out, pool.injectedEntropy());
		writeLoggingReference(out, pool.extractedEntropy());
		writeLoggingCount(out, pool.mixCount());

		out.writeInt(pool.hashX());
		out.writeInt(pool.hashY());

		writeByteArray(out, pool.buffer());
	}
}
