/*
 * This file is part of Utilities.
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

package org.digitalmodular.utilities.container;

import org.digitalmodular.utilities.Verifier;

/**
 * @author Mark Jeronimus
 */
// Created 2016-08-02
public class LoggingCount {
	private int count;

	private long modifyDate;

	public LoggingCount(int count, long modifyDate) {
		Verifier.requireThat(count >= 0,
		                     "count < 0: " +
		                     count);

		this.count = count;
		this.modifyDate = modifyDate;
	}

	public LoggingCount() {
		this(0, 0);
	}

	public void increment() {
		count++;

		log();
	}

	private void log() {
		modifyDate = System.currentTimeMillis();
	}

	public int getCount()       { return count; }

	public long getModifyDate() { return modifyDate; }

	public String toString()    { return Integer.toString(count); }
}
