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
