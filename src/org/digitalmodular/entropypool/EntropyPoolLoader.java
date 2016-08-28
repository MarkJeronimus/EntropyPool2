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
import java.util.logging.Logger;

import org.digitalmodular.utilities.LogTimer;
import org.digitalmodular.utilities.container.Version;
import org.digitalmodular.utilities.io.InvalidHeaderException;
import static org.digitalmodular.utilities.Verifier.requireThat;
import static org.digitalmodular.entropypool.EntropyPool.MAGIC;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public enum EntropyPoolLoader {
	;

	public static EntropyPool loadFromFile(File file) throws IOException {
		requireThat(file.exists(), "file.exists() == false: " + file);
		requireThat(file.isFile(), "file.isFile() == false: " + file);
		requireThat(file.canRead(), "file.canRead() == false: " + file);

		LogTimer.start(Level.INFO, "Loading Entropy Pool file " + file);

		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			Version version = readHeader(in);

			EntropyPool pool = readPool(in, version);

			if (Logger.getGlobal().isLoggable(Level.FINE))
				Logger.getGlobal().finer("Loaded pool: " + pool);
			LogTimer.finishAndLog(Level.FINE, "Loaded the Entropy Pool in {0} seconds");

			return pool;
		} catch (InvalidHeaderException ignored) {
			throw new InvalidHeaderException("File is not an EntropyPool file: " + file);
		}
	}

	static Version readHeader(DataInput in) throws IOException {
		byte[] magic = new byte[MAGIC.length()];
		in.readFully(magic);

		if (Logger.getGlobal().isLoggable(Level.FINE)) Logger.getGlobal().fine("magic: " + new String(magic, "UTF-8"));

		for (int i = 0; i < magic.length; i++)
			if (magic[i] != (byte)MAGIC.charAt(i))
				throw new InvalidHeaderException("Is not an EntropyPool");

		String title = in.readUTF();
		if (Logger.getGlobal().isLoggable(Level.FINE)) Logger.getGlobal().fine("title: " + title);

		Version version = Version.readFrom(in);
		if (Logger.getGlobal().isLoggable(Level.FINE)) Logger.getGlobal().fine("version: " + version);
		return version;
	}

	private static EntropyPool readPool(DataInputStream in, Version version) throws IOException {
		EntropyPool pool;

		if (version.getMajor() == 2) {
			pool = EntropyPool2Loader.readFrom(in);
		} else {
			if (version.getMajor() < 2)
				throw new IOException("Versions below 2 not supported: " + version);
			else
				throw new IOException("Versions above 2 not supported: " + version);
		}

		if (in.available() > 0)
			Logger.getGlobal().warning(in.available() + " extraneous byte(s) detected");

		return pool;
	}
}
