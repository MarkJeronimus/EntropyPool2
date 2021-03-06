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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.*;

/**
 * @author Mark Jeronimus
 */
// Created 2016-04-23
@SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "UseOfSystemOutOrSystemErr"})
public enum LoggerUtilities {
	;

	public static void configure(Level level) {
		Logger.getGlobal().setLevel(level);
		Logger.getGlobal().getParent().removeHandler(Logger.getGlobal().getParent().getHandlers()[0]);
		Logger.getGlobal().getParent().addHandler(
				new StreamHandler(System.out, new OneLineFormatter()) {
					@Override
					public synchronized void publish(LogRecord record) {
						super.publish(record);
						flush();
					}
				}
		);
		Logger.getGlobal().getParent().getHandlers()[0].setLevel(Level.FINEST);
	}

	private static class OneLineFormatter extends Formatter {
		@Override
		public synchronized String format(LogRecord record) {
			StringBuilder sb = new StringBuilder(64);

			String    levelName = record.getLevel().getLocalizedName();
			String    message   = formatMessage(record);
			Throwable exception = record.getThrown();

			sb.append('[').append(levelName).append("] ");
			sb.append(message);
			sb.append('\n');

			if (exception != null) {
				try (StringWriter sw = new StringWriter();
				     PrintWriter pw = new PrintWriter(sw)) {
					exception.printStackTrace(pw);
					sb.append(sw);
				} catch (IOException ignored) {
				}
			}

			return sb.toString();
		}
	}
}
