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

import java.util.Arrays;
import java.util.List;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-08-02
public class MultipleMixer implements EntropyPoolMixer {
	private final List<EntropyPoolMixer> mixers;

	public MultipleMixer(EntropyPoolMixer... mixers) {
		this.mixers = Arrays.asList(mixers);
	}

	@Override
	public void mix(EntropyPool2 pool) {
		for (EntropyPoolMixer mixer : mixers)
			mixer.mix(pool);
	}
}
