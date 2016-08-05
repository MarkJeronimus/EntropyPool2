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

package org.digitalmodular.utilities.container;

import java.util.function.UnaryOperator;
import static org.digitalmodular.utilities.Verifier.requireThat;

/**
 * @author Mark Jeronimus
 */
// Created 2016-08-04
public class LoggingVariable<T> {
	private T value;

	private int  modifyCount;
	private long modifyDate;

	public LoggingVariable(T value, int modifyCount, long modifyDate) {
		requireThat(modifyCount >= 0, "modifyCount < 0: " + modifyCount);

		this.value = value;
		this.modifyCount = modifyCount;
		this.modifyDate = modifyDate;
	}

	public LoggingVariable(T value) {
		this(value, 0, 0);
	}

	public void set(T value) {
		this.value = value;

		log();
	}

	public void modify(UnaryOperator<T> operator) {
		value = operator.apply(value);

		log();
	}

	public T get()              { return value; }

	public int getModifyCount() { return modifyCount; }

	public long getModifyDate() { return modifyDate; }

	public String toString()    { return value.toString(); }

	protected void log() {
		modifyCount = Math.incrementExact(modifyCount);
		modifyDate = System.currentTimeMillis();
	}
}
