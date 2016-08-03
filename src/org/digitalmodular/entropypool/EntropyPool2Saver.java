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
import java.util.Objects;
import java.util.logging.Logger;
import static org.digitalmodular.entropypool.EntropyPool.*;
import static org.digitalmodular.utilities.container.DataIO.*;
import org.digitalmodular.utilities.LogTimer;
import org.digitalmodular.utilities.Verifier;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public enum EntropyPool2Saver {
	;

	private static final Logger LOGGER = Logger.getLogger(EntropyPool2Saver.class.getName());

	public static void saveToFile(EntropyPool2 pool, File file) throws IOException {
		Objects.requireNonNull(pool,
		                       "pool == null");
		Objects.requireNonNull(file,
		                       "file == null");
		Verifier.requireThat(!file.exists() || file.isFile(),
		                     "file.isFile() == false: " +
		                     file);
		Verifier.requireThat(!file.exists() || file.canWrite(),
		                     "file.canWrite() == false: " +
		                     file);

		LOGGER.info("Saving Entropy Pool file " + file);
		LogTimer.start();

		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			writeHeader(out, pool);
			writePool(out, pool);
		}

		LogTimer.finishAndLog(LOGGER, "Saved the Entropy Pool in {0} seconds");
	}

	private static void writeHeader(DataOutputStream out, EntropyPool2 pool) throws IOException {
		out.writeBytes(MAGIC);
		out.writeUTF(PROGRAM_TITLE);

		writeVersion(out, CURRENT_VERSION);
	}

	private static void writePool(DataOutputStream out, EntropyPool2 pool) throws IOException {
		out.writeUTF(pool.getSecureRandom().getAlgorithm());
		out.writeUTF(pool.getMessageDigest().getAlgorithm());
		out.writeUTF(pool.getCipher().getAlgorithm());

		out.writeLong(pool.getCreatedDate());
		writeLoggingCount(out, pool.getMixCount());
		writeLoggingLong(out, pool.getInjectedEntropyLoggingLong());
		writeLoggingLong(out, pool.getExtractedEntropyLoggingLong());

		out.writeInt(pool.getHashX());
		out.writeInt(pool.getHashY());

		out.writeInt(pool.getBuffer().length);
		out.write(pool.getBuffer());
	}
}
