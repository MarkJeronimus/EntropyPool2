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

package org.digitalmodular.utilities;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mark Jeronimus
 */
// Created 2016-08-03
public class LogTimer {
	private static final ThreadLocal<Deque<Long>> THREAD_LOCAL = new ThreadLocal<Deque<Long>>() {
		@Override
		protected Deque<Long> initialValue() {
			return new ArrayDeque<>();
		}
	};

	public static void start() {
		Deque<Long> stack = THREAD_LOCAL.get();

		stack.push(System.nanoTime());
	}

	public static void start(Level level, String message) {
		Logger.getGlobal().log(level, message);

		start();
	}

	public static void finishAndLog(Level level, String template) {
		Deque<Long> stack = THREAD_LOCAL.get();

		if (stack.isEmpty()) throw new IllegalStateException("Not started");

		long elapsed = System.nanoTime() - stack.pop();
		Logger.getGlobal().log(level, template, elapsed / 1e9);
	}
}
