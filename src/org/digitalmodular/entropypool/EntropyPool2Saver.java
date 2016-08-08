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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import static java.util.Objects.requireNonNull;
import static org.digitalmodular.entropypool.EntropyPool.*;
import static org.digitalmodular.utilities.Verifier.requireThat;
import static org.digitalmodular.utilities.DataIO.*;
import org.digitalmodular.utilities.LogTimer;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public enum EntropyPool2Saver {
	;

	public static void saveToFile(EntropyPool2 pool, File file) throws IOException {
		requireNonNull(pool, "pool == null");
		requireThat(!file.exists() || file.isFile(), "file.isFile() == false: " + file);
		requireThat(!file.exists() || file.canWrite(), "file.canWrite() == false: " + file);

		LogTimer.start(Level.INFO, "Saving Entropy Pool file " + file);

		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			writeHeader(out, pool);
			writePool(out, pool);
		}

		LogTimer.finishAndLog(Level.FINE, "Saved the Entropy Pool in {0} seconds");
	}

	private static void writeHeader(DataOutputStream out, EntropyPool2 pool) throws IOException {
		out.writeBytes(MAGIC);
		out.writeUTF(PROGRAM_TITLE);

		writeVersion(out, CURRENT_VERSION);
	}

	private static void writePool(DataOutputStream out, EntropyPool2 pool) throws IOException {
		out.writeLong(pool.getCreateDate());
		writeLoggingCount(out, pool.accessCount());

		writeLoggingObject(out, pool.secureRandom());
		writeLoggingObject(out, pool.messageDigest());
		writeLoggingObject(out, pool.cipher());

		writeLoggingObject(out, pool.injectedEntropy());
		writeLoggingObject(out, pool.extractedEntropy());
		writeLoggingCount(out, pool.mixCount());

		out.writeInt(pool.hashX());
		out.writeInt(pool.hashY());

		writeByteArray(out, pool.buffer());
	}
}
