/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.editors.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class ChainedPreferenceStoreTest extends TestCase {

	private class PropertyChangeListener implements IPropertyChangeListener {

		public void propertyChange(PropertyChangeEvent event) {
			fEvents.add(event);
		}
	}

	private List fEvents= new ArrayList();
	private PropertyChangeListener fPropertyChangeListener= new PropertyChangeListener();

	private static final String PROPERTY= "some.property";
	private static final String VALUE= "8";
	private static final String DEFAULT_VALUE= "4";
	private static final String DEFAULT_DEFAULT_VALUE= "";

	public static Test suite() {
		return new TestSuite(ChainedPreferenceStoreTest.class);
	}

	/**
	 * [implementation] ChainedPreferenceStore
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=69419
	 */
	public void testChainedStore0() {
		IPreferenceStore store1= new PreferenceStore();
		IPreferenceStore store2= new PreferenceStore();
		IPreferenceStore chainedStore= new ChainedPreferenceStore(new IPreferenceStore[] { store1, store2 });
		store2.setDefault(PROPERTY, DEFAULT_VALUE);

		chainedStore.addPropertyChangeListener(fPropertyChangeListener);
		store1.firePropertyChangeEvent(PROPERTY, VALUE, DEFAULT_DEFAULT_VALUE); // simulated removal with newValue != null
		chainedStore.removePropertyChangeListener(fPropertyChangeListener);

		assertEquals(1, fEvents.size());
		PropertyChangeEvent event= (PropertyChangeEvent) fEvents.get(0);
		assertEquals(chainedStore, event.getSource());
		assertEquals(PROPERTY, event.getProperty());
		assertEquals(VALUE, event.getOldValue());
		assertEquals(DEFAULT_VALUE, event.getNewValue());
	}

	/**
	 * Assertion failed in ChainedPreferenceStore.handlePropertyChangeEvent(..)
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=52827
	 */
	public void testChainedStore1() {
		IPreferenceStore store1= new PreferenceStore();
		IPreferenceStore store2= new PreferenceStore();
		IPreferenceStore chainedStore= new ChainedPreferenceStore(new IPreferenceStore[] { store1, store2 });

		chainedStore.addPropertyChangeListener(fPropertyChangeListener);
		store1.firePropertyChangeEvent(PROPERTY, VALUE, DEFAULT_DEFAULT_VALUE); // simulated removal with newValue != null
		chainedStore.removePropertyChangeListener(fPropertyChangeListener);

		assertEquals(1, fEvents.size());
		PropertyChangeEvent event= (PropertyChangeEvent) fEvents.get(0);
		assertEquals(store1, event.getSource());
		assertEquals(PROPERTY, event.getProperty());
		assertEquals(VALUE, event.getOldValue());
		assertEquals(DEFAULT_DEFAULT_VALUE, event.getNewValue());
	}

	/**
	 * Third case where the initial implementation used to have an assertion which would fail in this case
	 */
	public void testChainedStore2() {
		IPreferenceStore store1= new PreferenceStore();
		IPreferenceStore store2= new PreferenceStore();
		IPreferenceStore chainedStore= new ChainedPreferenceStore(new IPreferenceStore[] { store1, store2 });
		store1.setValue(PROPERTY, VALUE);

		chainedStore.addPropertyChangeListener(fPropertyChangeListener);
		store1.firePropertyChangeEvent(PROPERTY, DEFAULT_VALUE, null); // simulated change with newValue == null
		chainedStore.removePropertyChangeListener(fPropertyChangeListener);

		assertEquals(1, fEvents.size());
		PropertyChangeEvent event= (PropertyChangeEvent) fEvents.get(0);
		assertEquals(store1, event.getSource());
		assertEquals(PROPERTY, event.getProperty());
		assertEquals(DEFAULT_VALUE, event.getOldValue());
		assertEquals(null, event.getNewValue());
	}

	/**
	 * Case where the initial implementation used to throw an IAE
	 */
	public void testChainedStore3() {
		IPreferenceStore store1= new PreferenceStore();
		IPreferenceStore store2= new PreferenceStore();
		IPreferenceStore chainedStore= new ChainedPreferenceStore(new IPreferenceStore[] { store1, store2 });
		store2.setDefault(PROPERTY, DEFAULT_VALUE);

		chainedStore.addPropertyChangeListener(fPropertyChangeListener);
		store1.firePropertyChangeEvent(PROPERTY, null, null); // simulated removal with oldValue == null
		chainedStore.removePropertyChangeListener(fPropertyChangeListener);

		assertEquals(1, fEvents.size());
		PropertyChangeEvent event= (PropertyChangeEvent) fEvents.get(0);
		assertEquals(chainedStore, event.getSource());
		assertEquals(PROPERTY, event.getProperty());
		assertEquals(null, event.getOldValue());
		assertEquals(DEFAULT_VALUE, event.getNewValue());
	}

//	/**
//	 * Case where the old value cannot be determined. (Not handled by the current implementation.)
//	 */
//	public void testChainedStore4() {
//		IPreferenceStore store1= EditorsUI.getPreferenceStore();
//		IPreferenceStore store2= new PreferenceStore();
//		IPreferenceStore chainedStore= new ChainedPreferenceStore(new IPreferenceStore[] { store1, store2 });
//		store2.setDefault(PROPERTY, DEFAULT_VALUE);
//
//		chainedStore.addPropertyChangeListener(fPropertyChangeListener);
//		store1.setValue(PROPERTY, VALUE);
//		chainedStore.removePropertyChangeListener(fPropertyChangeListener);
//
//		assertEquals(1, fEvents.size());
//		PropertyChangeEvent event= (PropertyChangeEvent) fEvents.get(0);
//		assertEquals(PROPERTY, event.getProperty());
//		assertNull(event.getOldValue());
//		assertEquals(VALUE, event.getNewValue());
//	}
}
