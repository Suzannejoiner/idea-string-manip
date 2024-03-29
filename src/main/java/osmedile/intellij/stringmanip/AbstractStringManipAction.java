package osmedile.intellij.stringmanip;

import osmedile.intellij.stringmanip.utils.StringUtils;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;

/**
 * @author Olivier Smedile
 * @version $Id: AbstractStringManipAction.java 62 2008-04-20 11:11:54Z osmedile $
 */
public abstract class AbstractStringManipAction extends EditorAction {

	protected AbstractStringManipAction() {
		this(true);
	}

	protected AbstractStringManipAction(boolean setupHandler) {
		super(null);
		if (setupHandler) {
			this.setupHandler(new EditorWriteActionHandler() {
				public void executeWriteAction(Editor editor, DataContext dataContext) {
					final SelectionModel selectionModel = editor.getSelectionModel();
					String selectedText = selectionModel.getSelectedText();

					boolean allLinSelected = false;
					if (selectedText == null) {
						selectionModel.selectLineAtCaret();
						selectedText = selectionModel.getSelectedText();
						allLinSelected = true;

						if (selectedText == null) {
							return;
						}
					} else if (selectedText.endsWith("\n")) {
						allLinSelected = true;

					}
					String[] textParts = selectedText.split("\n");
					if (selectionModel.hasBlockSelection()) {
						int[] blockStarts = selectionModel.getBlockSelectionStarts();
						int[] blockEnds = selectionModel.getBlockSelectionEnds();

						int plusOffset = 0;

						for (int i = 0; i < textParts.length; i++) {

							String newTextPart = transform(textParts[i]);
							if (allLinSelected) {
								newTextPart += "\n";
							}

							editor.getDocument().replaceString(blockStarts[i] + plusOffset, blockEnds[i] + plusOffset,
									newTextPart);

							int realOldTextLength = blockEnds[i] - blockStarts[i];
							plusOffset += newTextPart.length() - realOldTextLength;
						}
					} else {
						for (int i = 0; i < textParts.length; i++) {
							textParts[i] = transform(textParts[i]);
						}

						final String s = StringUtils.join(textParts, '\n');
						editor.getDocument().replaceString(selectionModel.getSelectionStart(),
								selectionModel.getSelectionEnd(), s);
						if (allLinSelected) {
							editor.getDocument().insertString(selectionModel.getSelectionEnd(), "\n");
						}
					}
				}
			});
		}

	}

	public abstract String transform(String s);
}
