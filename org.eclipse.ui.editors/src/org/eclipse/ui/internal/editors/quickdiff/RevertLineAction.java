/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.quickdiff;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action that will revert a line in the currently displayed document to the state in the
 * reference document.
 *
 * @since 3.1
 */
public class RevertLineAction extends QuickDiffRestoreAction {
	/** Resource key prefix. */
	private static final String PREFIX= "RevertLineAction."; //$NON-NLS-1$
	/** Resource key for added lines - they will be deleted. */
	private static final String DELETE_KEY= PREFIX + "delete.label"; //$NON-NLS-1$
	/** Resource key for changed lines - they will be reverted. */
	private static final String REVERT_KEY= PREFIX + "label"; //$NON-NLS-1$

	/** The line to be restored. Set in <code>update()</code>. */
	private int fLine;

	/**
	 * Creates a new instance.
	 *
	 * @param editor the editor this action belongs to
	 * @param isRulerAction <code>true</code> if this is a ruler action
	 */
	public RevertLineAction(ITextEditor editor, boolean isRulerAction) {
		super(QuickDiffMessages.getResourceBundle(), PREFIX, editor, isRulerAction);
	}

	/*
	 * @see org.eclipse.ui.internal.texteditor.quickdiff.QuickDiffRestoreAction#computeEnablement()
	 */
	public boolean computeEnablement() {
		if (!super.computeEnablement())
			return false;

		fLine= getLastLine();
		if (fLine == -1)
			return false;
		ILineDiffer differ= getDiffer();
		if (differ == null)
			return false;
		ILineDiffInfo info= differ.getLineInfo(fLine);
		if (info == null || info.getChangeType() == ILineDiffInfo.UNCHANGED)
			return false;

		if (info.getChangeType() == ILineDiffInfo.ADDED)
			setText(QuickDiffMessages.getString(DELETE_KEY));
		else
			setText(QuickDiffMessages.getString(REVERT_KEY));
		return true;
	}

	/*
	 * @see org.eclipse.ui.internal.editors.quickdiff.QuickDiffRestoreAction#runCompoundChange()
	 */
	public void runCompoundChange() {
		if (!isEnabled())
			return;
		ILineDiffer differ= getDiffer();
		if (differ != null) {
			try {
				differ.revertLine(fLine);
			} catch (BadLocationException e) {
				setStatus(e.getMessage());
			}
		}
	}
}