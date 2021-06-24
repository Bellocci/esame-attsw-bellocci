package com.examples.esame_attsw_Bellocci.view.swing;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.examples.esame_attsw_Bellocci.model.Library;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
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
		window.button(JButtonMatcher.withText("Add library")).requireDisabled();
		
		// setup
		idTxtBox.setText("");
		nameTxtBox.setText("");
		
		// exercise
		idTxtBox.enterText(" ");
		nameTxtBox.enterText("library");
		
		// verify
		window.button(JButtonMatcher.withText("Add library")).requireDisabled();
	}
	
	@Test
	public void testWhenLibraryIsSelectedFromListDeleteLibraryButtonShouldBeEnabled() {
		// setup
		GuiActionRunner.execute(() -> 
			librarySwingView.getListLibraryModel().addElement(new Library("1", "library1")));
		
		// execute
		window.list("libraryList").selectItem(0);
		
		// verify
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete library"));
		deleteButton.requireEnabled();
		window.list("libraryList").clearSelection();
		deleteButton.requireDisabled();
	}
	
	@Test
	public void testWhenLibraryIsSelectedFromListOpenLibraryButtonShouldBeEnabled() {
		// setup
		GuiActionRunner.execute(() -> 
			librarySwingView.getListLibraryModel().addElement(new Library("1", "library1")));
		
		// exercise
		window.list("libraryList").selectItem(0);
	
		// verify
		JButtonFixture openButton = window.button(JButtonMatcher.withText("Open library"));
		openButton.requireEnabled();
		window.list("libraryList").clearSelection();
		openButton.requireDisabled();
	}
	
	@Test
	public void testShowAllLibrariesShouldAddLibrariesToTheList() {
		// setup
		Library library1 = new Library("1", "library1");
		Library library2 = new Library("2", "library2");
		
		// exercise
		GuiActionRunner.execute(() -> {
			librarySwingView.showAllLibraries(Arrays.asList(library1, library2));
		});
		
		// verify
		String[] listLibraries = window.list().contents();
		assertThat(listLibraries)
			.containsExactly(library1.toString(), library2.toString());
	}
	
	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		// setup
		Library library = new Library("1", "library1");
		
		// exercise
		GuiActionRunner.execute(() ->
			librarySwingView.showError("Error ", library)
		);
		
		// verify
		window.label("errorLabelMessage").requireText("Error : " + library);
	}
	
	@Test
	public void testLibraryAddedShouldAddLibraryToTheListAndClearErrorLabel() {
		// setup
		Library library = new Library("1", "library1");
		GuiActionRunner.execute(() -> {
			librarySwingView.getLblErrorMessage().setText("Error");
		});
		
		// exercise
		GuiActionRunner.execute(() -> librarySwingView.libraryAdded(library));
		
		// verify
		String[] listLibraries = window.list().contents();
		assertThat(listLibraries).containsExactly("1 - library1");
		window.label("errorLabelMessage").requireText(" ");
	}
}
