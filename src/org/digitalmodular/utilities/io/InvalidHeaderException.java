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

package org.digitalmodular.utilities.io;

import java.io.IOException;

/**
 * @author Mark Jeronimus
 */
// Created 2016-08-08
public class InvalidHeaderException extends IOException {
	public InvalidHeaderException()                                { }

	public InvalidHeaderException(String message)                  { super(message); }

	public InvalidHeaderException(String message, Throwable cause) { super(message, cause); }

	public InvalidHeaderException(Throwable cause)                 { super(cause); }
}
