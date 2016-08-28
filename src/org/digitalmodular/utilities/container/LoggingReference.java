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

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import static java.util.Objects.requireNonNull;

import static org.digitalmodular.utilities.Verifier.requireThat;

/**
 * @author Mark Jeronimus
 */
// Created 2016-08-04
public class LoggingReference<V> {
	private V value;

	private int  modifyCount;
	private long modifyDate;

	public LoggingReference(V value) {
		this(value, 0, 0);
	}

	public LoggingReference(V value, int modifyCount, long modifyDate) {
		requireNonNull(value, "value == null");
		requireThat(modifyCount >= 0, "modifyCount < 0: " + modifyCount);

		this.value = value;
		this.modifyCount = modifyCount;
		this.modifyDate = modifyDate;
	}

	public LoggingReference(LoggingReference<V> other) {
		value = other.value;
		modifyCount = other.modifyCount;
		modifyDate = other.modifyDate;
	}

	public V get() {
		return value;
	}

	public void set(V value) {
		requireNonNull(value, "value == null");

		this.value = value;

		log();
	}

	/**
	 * Sets the value to the given updated value if the current value {@code ==} the expected value.
	 *
	 * @param expect the expected value
	 * @param update the new value
	 * @return {@code true} if successful. False return indicates that the actual value was not equal to the expected
	 * value.
	 */
	public final boolean compareAndSet(V expect, V update) {
		if (value != expect) return false;
		set(update);
		return true;

	}

	/**
	 * Sets to the given value and returns the old value.
	 *
	 * @param newValue the new value
	 * @return the previous value
	 */
	@SuppressWarnings("unchecked")
	public final V getAndSet(V newValue) {
		V old = value;
		set(newValue);
		return old;
	}

	/**
	 * Updates the current value with the results of
	 * applying the given function.
	 *
	 * @param updateFunction a side-effect-free function
	 * @return the previous value
	 */
	public final void update(UnaryOperator<V> updateFunction) {
		set(updateFunction.apply(value));
	}

	/**
	 * Updates the current value with the results of
	 * applying the given function, returning the updated value.
	 *
	 * @param updateFunction a side-effect-free function
	 * @return the updated value
	 */
	public final V updateAndGet(UnaryOperator<V> updateFunction) {
		V next = updateFunction.apply(value);
		set(next);
		return next;
	}

	/**
	 * Updates the current value with the results of
	 * applying the given function to the current and given values,
	 * returning the updated value. The function
	 * is applied with the current value as its first argument,
	 * and the given update as the second argument.
	 *
	 * @param x                   the update value
	 * @param accumulatorFunction a side-effect-free function of two arguments
	 * @return the updated value
	 */
	public final V accumulateAndGet(V x, BinaryOperator<V> accumulatorFunction) {
		V next = accumulatorFunction.apply(value, x);
		set(next);
		return next;
	}

	public int getModifyCount() { return modifyCount; }

	public long getModifyDate() { return modifyDate; }

	protected void log() {
		modifyCount = Math.incrementExact(modifyCount);
		modifyDate = System.currentTimeMillis();
	}

	public String toString() { return value.toString(); }
}
