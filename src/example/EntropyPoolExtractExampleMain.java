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

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.digitalmodular.entropypool.EntropyPool2;

/**
 * @author Mark Jeronimus
 * @version 2.0
 * @since 2.0
 */
// Created 2016-07-29
public class EntropyPoolExtractExampleMain {
	private static final File ENTROPY_POOL_FILE      = new File("r:\\entropypool.bin");
	private static final File ENTROPY_POOL_FILE_BAK  = new File("r:\\entropypool.bak");
	private static final File ENTROPY_POOL_FILE_TEMP = new File("r:\\entropypool.tmp");

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(EntropyPoolExtractExampleMain::new);
	}

	private EntropyPool2 pool;

	public EntropyPoolExtractExampleMain() {
		try {
			pool = EntropyPool2.loadFromFile(ENTROPY_POOL_FILE);

			byte[] entropy = pool.extractEntropy(16);
			System.out.println("Extracted entropy: " + Arrays.toString(entropy));
			System.out.println("Entropy remaining: " + pool.getEntropyRemaining());

			pool.saveToFile(ENTROPY_POOL_FILE, ENTROPY_POOL_FILE_BAK, ENTROPY_POOL_FILE_TEMP);
		} catch (IOException ex) {
			Logger.getLogger(EntropyPoolExtractExampleMain.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
}
