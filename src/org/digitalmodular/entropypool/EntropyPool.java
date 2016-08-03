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

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import org.digitalmodular.utilities.Verifier;
import org.digitalmodular.utilities.container.Version;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-29
public interface EntropyPool {
	Version CURRENT_VERSION = new Version(2, 0, Version.Release.ALPHA, 14);

	String PROGRAM_TITLE = String.format("EntropyPool v%s © %d DigitalModular",
	                                     CURRENT_VERSION.toShortString(), LocalDate.now().getYear());

	String MAGIC = "ENTROPYPOOL";

	void injectEntropy(byte[] bytes, int entropyBits);
	byte[] extractEntropy(int numBytes);

	long getInjectedEntropy();
	long getExtractedEntropy();

	void mix();

	default void injectEntropyFromFileOrDirectory(File fileOrDirectory) throws IOException {
		Objects.requireNonNull(fileOrDirectory,
		                       "fileOrDirectory == null");
		Verifier.requireThat(fileOrDirectory.exists(), "File doesn't exist");

		EntropyPoolInjector.injectEntropyFromFileOrDirectory(this, fileOrDirectory);
	}

	default long getAvailableEntropy() {
		return getInjectedEntropy() - getExtractedEntropy();
	}
}
