/*
 * This file is part of EntropyPool2.
 *
 * Copyleft 2014 Mark Jeronimus. All Rights Reversed.
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
 * along with AllUtilities. If not, see <http://www.gnu.org/licenses/>.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.digitalmodular.entropypool;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.digitalmodular.utilities.Verifyer;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-25
public enum EntropyPoolInjector {
	;

	private static final int MAX_READ_ARRAY_LENGTH = 16 * 1024 * 1024;

	public static void injectEntropyFromFileOrDirectory(EntropyPool pool, File fileOrDirectory) throws IOException {
		Objects.requireNonNull(fileOrDirectory);
		Objects.requireNonNull(pool, "pool = null");
		Verifyer.requireThat(fileOrDirectory.exists(), "fileOrDirectory.exists() = false: " + fileOrDirectory);

		if (fileOrDirectory.isDirectory())
			injectDirectory(pool, fileOrDirectory);
		else if (fileOrDirectory.isFile())
			injectFile(pool, fileOrDirectory);
		else
			throw new IllegalArgumentException(fileOrDirectory.toString());
	}

	public static void injectDirectory(EntropyPool pool, File directory) throws IOException {
		Objects.requireNonNull(directory, "directory = null");
		Objects.requireNonNull(pool, "pool = null");
		Verifyer.requireThat(directory.exists(), "directory.exists() = false: " + directory);

		for (File file : directory.listFiles()) {
			injectEntropyFromFileOrDirectory(pool, file);

			pool.mix();
		}
	}

	public static void injectFile(EntropyPool pool, File file) throws IOException {
		Objects.requireNonNull(file, "file = null");
		Objects.requireNonNull(pool, "pool = null");
		Verifyer.requireThat(file.exists(), "file.exists() = false: " + file);
		Verifyer.requireThat(file.canRead(), "file.canRead() = false: " + file);

		long remaining = file.length();

		if (remaining > MAX_READ_ARRAY_LENGTH) {
			long numChunks = (remaining + MAX_READ_ARRAY_LENGTH - 1) / MAX_READ_ARRAY_LENGTH;
			Logger.getLogger(EntropyPoolInjector.class.getName())
			      .log(Level.WARNING,
			           "File is larger than max array length: {0} > {1}. The file will be read in {1}" +
			           " pieces and each piece added to the pool as if it were a separate file.",
			           new Object[]{remaining, MAX_READ_ARRAY_LENGTH, numChunks});
		}

		byte[] bytesForPool = null;

		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			while (remaining > 0) {
				int size = remaining > MAX_READ_ARRAY_LENGTH ? MAX_READ_ARRAY_LENGTH : (int) remaining;

				if (bytesForPool == null || bytesForPool.length != size) bytesForPool = new byte[size];

				in.readFully(bytesForPool);

				pool.injectEntropy(bytesForPool, bytesForPool.length);

				remaining -= size;
			}
		}

		pool.mix();
	}
}
