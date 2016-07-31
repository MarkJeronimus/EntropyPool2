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
package org.digitalmodular.entropypool;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.digitalmodular.utilities.Verifyer;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-29
public interface EntropyPool {
	String DEFAULT_SECURERANDOM_STRING  = "SP800CTR/AES/256/256/16777216";
	String DEFAULT_MESSAGEDIGEST_STRING = "Keccak-512";
	String DEFAULT_CIPHER_STRING        = "Threefish-1024/EAX/NoPadding";

	// The numbers that are 'most coprime' to 64 are 19 and 45. (Hexacontatetragram{64/19})
	// Subtracting 64s until total file size < 64kiB.
	int DEFAULT_ENTROPY_POOL_BYTE_LENGTH = 65536 - 64 * 6 - 45;

	boolean isAlive();
	void terminate();

	void injectEntropy(byte[] bytes, int entropyBits);
	byte[] extractEntropy(int numBytes);

	void mix();

	default void checkAlive() {
		Verifyer.requireState(isAlive(), "Entropy pool has been terminated");
	}

	default void injectEntropyFromFileOrDirectory(File fileOrDirectory) throws IOException {
		Objects.requireNonNull(fileOrDirectory, "fileOrDirectory = null");
		Verifyer.requireThat(fileOrDirectory.exists(), "File doesn't exist");
		checkAlive();

		EntropyPoolInjector.injectEntropyFromFileOrDirectory(this, fileOrDirectory);
	}
}
