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

import java.security.SecureRandom;
import java.util.logging.Level;
import static org.digitalmodular.utilities.ArrayUtilities.shuffle;
import org.digitalmodular.utilities.LogTimer;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-08-02
public class PermuteMixer implements EntropyPoolMixer {
	@Override
	public void mix(EntropyPool2 pool) {
		LogTimer.start();

		SecureRandom random = pool.getSecureRandom();
		byte[]       buffer = pool.buffer();

		shuffle(buffer, random);

		LogTimer.finishAndLog(Level.FINER, "Permuted the Entropy Pool in {0} seconds");
	}
}
