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
public class LoggingLong {
	private long value;

	private int  modifyCount;
	private long modifyDate;

	public LoggingLong(long value, int modifyCount, long modifyDate) {
		Verifier.requireThat(modifyCount >= 0,
		                     "modifyCount < 0: " +
		                     modifyCount);

		this.value = value;
		this.modifyCount = modifyCount;
		this.modifyDate = modifyDate;
	}

	public LoggingLong(long value) {
		this(value, 0, 0);
	}

	public void set(long newValue) {
		this.value = newValue;

		log();
	}

	public void increment() {
		value++;

		log();
	}

	public void add(long addend) {
		value += addend;

		log();
	}

	private void log() {
		modifyCount++;
		modifyDate = System.currentTimeMillis();
	}

	public long getValue()      { return value; }

	public int getModifyCount() { return modifyCount; }

	public long getModifyDate() { return modifyDate; }

	public String toString()    { return Long.toString(value); }
}
