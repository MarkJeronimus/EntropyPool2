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

import java.io.*;
import java.util.logging.Level;

import org.digitalmodular.utilities.LogTimer;

import static java.util.Objects.requireNonNull;
import static org.digitalmodular.utilities.Verifier.requireThat;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public enum EntropyPoolInjector {
	;

	private static final int MAX_READ_ARRAY_LENGTH = 1024 * 1024;

	public static void injectEntropyFromFileOrDirectory(EntropyPool pool, File fileOrDirectory) throws IOException {
		requireNonNull(pool, "pool == null");
		requireThat(fileOrDirectory.exists(), "fileOrDirectory.exists() == false: " + fileOrDirectory);

		if (fileOrDirectory.isDirectory())
			injectDirectory(pool, fileOrDirectory);
		else if (fileOrDirectory.isFile())
			injectFile(pool, fileOrDirectory);
		else
			throw new IllegalArgumentException(fileOrDirectory.toString());
	}

	public static void injectDirectory(EntropyPool pool, File directory) throws IOException {
		requireNonNull(pool, "pool == null");
		requireThat(directory.exists(), "directory.exists() == false: " + directory);

		File[] files = directory.listFiles();
		if (files == null)
			throw new IOException("Directory unreadable:" + directory);

		for (File file : files)
			injectEntropyFromFileOrDirectory(pool, file);
	}

	public static void injectFile(EntropyPool pool, File file) throws IOException {
		requireNonNull(pool, "pool == null");
		requireThat(file.exists(), "file.exists() == false: " + file);
		requireThat(file.canRead(), "file.canRead() == false: " + file);

		LogTimer.start(Level.INFO, "Injecting entropy into the Entropy Pool from file " + file);

		long remaining = file.length();

		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			byte[] bytesForPool = null;
			while (remaining > 0) {

				int size = remaining > MAX_READ_ARRAY_LENGTH ? MAX_READ_ARRAY_LENGTH : (int)remaining;

				if (bytesForPool == null || bytesForPool.length != size) bytesForPool = new byte[size];

				in.readFully(bytesForPool);

				pool.injectEntropy(bytesForPool, bytesForPool.length);

				remaining -= size;
			}
		}

		pool.mix();

		LogTimer.finishAndLog(Level.FINE, "Injected file into the Entropy Pool in {0} seconds");
	}
}
