package com.examples.esame_attsw_Bellocci.view.swing;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

@RunWith(GUITestRunner.class)
public class LibrarySwingViewTest extends AssertJSwingJUnitTestCase {
	
	private FrameFixture window;
	
	private LibrarySwingView librarySwingView;

	@Override
	protected void onSetUp() throws Exception {
		GuiActionRunner.execute(() -> {
			librarySwingView = new LibrarySwingView();
			return librarySwingView;
		});
		window = new FrameFixture(robot(), librarySwingView);
		window.show(); // shows the frame to test
	}
	
	@Test @GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("id"));
		window.textBox("idTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("name"));
		window.textBox("nameTextBox").requireEnabled();
		window.button(JButtonMatcher.withText("Add library")).requireDisabled();
		window.list("libraryList");
		window.button(JButtonMatcher.withText("Delete library")).requireDisabled();
		window.button(JButtonMatcher.withText("Open library")).requireDisabled();
		window.label(JLabelMatcher.withText(" "));
	}

	@Test
	public void testWhenIdAndNameAreNotEmptyAddLibraryButtonShouldBeEnabled() {
		// setup
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("library1");
		
		// exercise & verify
		window.button(JButtonMatcher.withText("Add library")).requireEnabled();
	}
	
	@Test
	public void testWhenIdIsBlankOrNameIsBlankOrBothAreBlankAddLibraryButtonShouldBeDisabled() {
		// setup
		JTextComponentFixture idTxtBox = window.textBox("idTextBox");
		JTextComponentFixture nameTxtBox = window.textBox("nameTextBox");
		
		// exercise
		idTxtBox.enterText("1");
		nameTxtBox.enterText(" ");
		
		// verify
		window.button(JButtonMatcher.withText("Add Library")).requireDisabled();
		
		// setup
		idTxtBox.setText("");
		nameTxtBox.setText("");
		
		// exercise
		idTxtBox.enterText(" ");
		nameTxtBox.enterText("library");
		
		// verify
		window.button(JButtonMatcher.withText("Add Library")).requireDisabled();
	}
}
