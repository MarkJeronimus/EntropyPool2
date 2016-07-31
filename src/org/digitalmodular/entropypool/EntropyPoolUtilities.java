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

import org.digitalmodular.utilities.Version;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-27
public enum EntropyPoolUtilities {
	;

	public static final Version CURRENT_VERSION = new Version(2, 0, 1, Version.Release.ALPHA);

	public static final int    TAG_LENGTH        = 16;
	public static final String MAGIC_TAG         = "ENTROPYPOOL";
	public static final String COMMENT_TAG       = "COMMENT";
	public static final String VERSION_TAG       = "VERSION";
	public static final String POOL_TAG          = "POOL";
	public static final String SECURERANDOM_TAG  = "SECURERANDOM";
	public static final String MESSAGEDIGEST_TAG = "MESSAGEDIGEST";
	public static final String CIPHER_TAG        = "CIPHER";
	public static final String ENTROPY_TAG       = "ENTROPY";
	public static final String HASH_TAG          = "HASH";
	public static final String COUNT_TAG         = "COUNT";
	public static final String DATE_TAG          = "DATE";

	public static int calculatePadLength(int size, int padModulo) {
		int paddedLength = (size + padModulo - 1) & -padModulo;
		int padLength    = paddedLength - size;
		return padLength;
	}

	public static int getLengthBeforeWrap(int arrayLength, int offset, int length) {
		return Math.min(length, arrayLength - offset);
	}

	public static int getLengthAfterWrap(int arrayLength, int offset, int length) {
		return Math.max(0, offset + length - arrayLength);
	}
}
