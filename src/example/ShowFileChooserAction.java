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
package example;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * @author Mark Jeronimus
 */
// Created 2016-07-25
public class ShowFileChooserAction extends AbstractAction {
	private final Consumer<File[]> callback;

	private final JFileChooser chooser;

	public ShowFileChooserAction(Consumer<File[]> callback) {
		super("Select files and/or directories");

		Objects.requireNonNull(callback,
		                       "callback == null");

		this.callback = callback;

		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		chooser.setCurrentDirectory(new File("r:\\"));
		int returnValue = chooser.showOpenDialog(null);

		if (returnValue != JFileChooser.APPROVE_OPTION)
			return;

		File[] filesOrDirectories = chooser.getSelectedFiles();

		callback.accept(filesOrDirectories);
	}
}
