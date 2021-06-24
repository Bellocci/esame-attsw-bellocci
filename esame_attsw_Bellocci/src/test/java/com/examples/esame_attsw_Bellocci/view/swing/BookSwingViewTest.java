package com.examples.esame_attsw_Bellocci.view.swing;

import static org.junit.Assert.*;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.examples.esame_attsw_Bellocci.model.Book;

@RunWith(GUITestRunner.class)
public class BookSwingViewTest extends AssertJSwingJUnitTestCase {
	
	private FrameFixture window;
	
	private BookSwingView bookSwingView;

	@Override
	protected void onSetUp() throws Exception {
		GuiActionRunner.execute(() -> {
			bookSwingView = new BookSwingView();
			return bookSwingView;
		});
		window = new FrameFixture(robot(), bookSwingView);
		window.show(); // shows the frame to test
	}

	@Test @GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("id"));
		window.textBox("idTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("name"));
		window.textBox("nameTextBox").requireEnabled();
		window.button(JButtonMatcher.withText("Add book")).requireDisabled();
		window.list("bookList");
		window.button(JButtonMatcher.withText("Back to libraries")).requireEnabled();
		window.button(JButtonMatcher.withText("Delete book")).requireDisabled();
		window.label(JLabelMatcher.withText(" "));
	}
	
	@Test
	public void testWhenIdAndNameAreNotEmptyAddBookButtonShouldBeEnabled() {
		// exercise
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("book1");
		
		// verify
		window.button(JButtonMatcher.withText("Add book")).requireEnabled();
	}
	
	@Test
	public void testWhenIdIsEmptyOrNameIsEmptyOrBothAreEmptyAddBookButtonShouldBeDisabled() {
		// exercise
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText(" ");
		
		// verify
		window.button(JButtonMatcher.withText("Add book")).requireDisabled();
		
		// setup
		window.textBox("idTextBox").setText("");
		window.textBox("nameTextBox").setText("");
		
		// exercise
		window.textBox("idTextBox").enterText(" ");
		window.textBox("nameTextBox").enterText("test");
		
		// verify
		window.button(JButtonMatcher.withText("Add book")).requireDisabled();
	}
	
	@Test
	public void testDeleteButtonIsEnableOnlyIfABookIsSelectedFromList() {
		Book book = new Book("1", "book1");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Book> listBooksModel = bookSwingView.getListBooksModel();
			listBooksModel.addElement(book);
		});
		window.list("bookList").selectItem(0);
		window.button(JButtonMatcher.withText("Delete book")).requireEnabled();
		window.list("bookList").clearSelection();
		window.button(JButtonMatcher.withText("Delete book")).requireDisabled();
	}
}
