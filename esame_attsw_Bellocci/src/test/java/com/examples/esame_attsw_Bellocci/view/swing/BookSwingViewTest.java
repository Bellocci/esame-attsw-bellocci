package com.examples.esame_attsw_Bellocci.view.swing;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.examples.esame_attsw_Bellocci.controller.BookController;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;

@RunWith(GUITestRunner.class)
public class BookSwingViewTest extends AssertJSwingJUnitTestCase {
	
	private FrameFixture window;
	
	private BookSwingView bookSwingView;
	
	@Mock
	private BookController bookController;
	
	@Mock
	private LibrarySwingView librarySwingView;
	
	private AutoCloseable closeable;
	
	private Library library;

	@Override
	protected void onSetUp() throws Exception {
		library = new Library("1", "library1");
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			bookSwingView = new BookSwingView();
			bookSwingView.setBookController(bookController);
			bookSwingView.setLibrary(library);
			bookSwingView.setLibrarySwingView(librarySwingView);
			return bookSwingView;
		});
		window = new FrameFixture(robot(), bookSwingView);
		window.show(); // shows the frame to test
	}
	
	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
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
		// setup
		Book book = new Book("1", "book1");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Book> listBooksModel = bookSwingView.getListBooksModel();
			listBooksModel.addElement(book);
		});
		
		// exercise
		window.list("bookList").selectItem(0);
		
		// verify
		window.button(JButtonMatcher.withText("Delete book")).requireEnabled();
		window.list("bookList").clearSelection();
		window.button(JButtonMatcher.withText("Delete book")).requireDisabled();
	}
	
	@Test
	public void testShowAllBooksShouldAddBookToTheList() {
		// setup
		Book book1 = new Book("1", "book1");
		Book book2 = new Book("2", "book2");
		
		// exercise
		GuiActionRunner.execute(
			() -> bookSwingView.showAllBooks(Arrays.asList(book1, book2))
		);
		
		// verify
		String[] listContents = window.list("bookList").contents();
		assertThat(listContents)
			.anyMatch(e -> e.contains("1 - book1"))
			.anyMatch(e -> e.contains("2 - book2"));
	}
	
	@Test
	public void testBookAddedShouldAddBookToTheListAndClearTheErrorLabel() {
		// setup
		Book book = new Book("1", "book1");
		GuiActionRunner.execute(() -> {
			bookSwingView.getLblErrorMessage().setText("Error");
		});
		
		// exercise
		GuiActionRunner.execute(() -> bookSwingView.bookAdded(book));
		
		// verify
		assertThat(window.list("bookList").contents())
			.anyMatch(e -> e.contains("1 - book1"));
		window.label("errorLabelMessage").requireText(" ");
	}
	
	@Test
	public void testBookRemovedShouldRemoveBookFromListAndClearTheErrorLabel() {
		// setup
		Book book1 = new Book("1", "book1");
		Book book2 = new Book("2", "book2");
		GuiActionRunner.execute(() -> {
			bookSwingView.getLblErrorMessage().setText("Error");
			DefaultListModel<Book> listBooksModel = bookSwingView.getListBooksModel();
			listBooksModel.addElement(book1);
			listBooksModel.addElement(book2);
		});
		
		// exercise
		GuiActionRunner.execute(() -> bookSwingView.bookRemoved(book2));
		
		// verify
		assertThat(window.list("bookList").contents())
			.noneMatch(e -> e.contains("2 - book2"));
		window.label("errorLabelMessage").requireText(" ");
	}
	
	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		// setup
		Book book = new Book("1", "book1");
		
		// exercise
		GuiActionRunner.execute(() -> bookSwingView.showError("Error message", book));
		
		// verify
		window.label("errorLabelMessage").requireText("Error message : 1 - book1");
	}
	
	//Mocks
	
	@Test
	public void testAddBookButtonShouldDelegateToBookControllerNewBook() {
		// setup
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("book1");
		
		// exercise
		window.button(JButtonMatcher.withText("Add book")).click();
		
		// verify
		verify(bookController).newBook(library, new Book("1", "book1"));
	}
	
	@Test
	public void testDeleteBookButtonShouldDelegateToBookControllerDeleteBook() {
		// setup
		Book book1 = new Book("1", "book1");
		Book book2 = new Book("2", "book2");
		book1.setLibrary(library);
		book2.setLibrary(library);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Book> listBooksModel = bookSwingView.getListBooksModel();
			listBooksModel.addElement(book1);
			listBooksModel.addElement(book2);
		});
		
		// exercise
		window.list("bookList").selectItem(1);
		window.button(JButtonMatcher.withText("Delete book")).click();
		
		// verify
		verify(bookController).deleteBook(library, book2);
	}
	
	@Test
	public void testBackToLibrariesButtonShouldCleanTableAndErrorLabelAndDisableBookViewAndSetVisibleLibraryView() {
		// exercise
		window.button(JButtonMatcher.withText("Back to libraries")).click();
		
		// verify
		verify(librarySwingView).setVisible(true);
		assertThat(bookSwingView.isVisible()).isFalse();
		window.show();
		assertThat(window.list("bookList").contents()).isEmpty();
		window.label("errorLabelMessage").requireText(" ");
	}
	
	@Test
	public void testCloseViewErrorShouldCleanTableAndDisableBookViewAndDelegateLibrarySwingViewShowError() {
		// setup
		Book book = new Book("1", "book1");
		book.setLibrary(library);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Book> listBooksModel = bookSwingView.getListBooksModel();
			listBooksModel.addElement(book);
			bookSwingView.getLblErrorMessage().setText("Error");
		});
		assertThat(window.list("bookList").contents()).hasSize(1);
		
		// exercise
		GuiActionRunner.execute(() -> bookSwingView.closeViewError("Doesnt exist library with id 1 ", library));
		
		// verify
		verify(librarySwingView).setVisible(true);
		verify(librarySwingView).libraryRemoved(library);
		verify(librarySwingView).showError("Doesnt exist library with id 1 ", library);
		assertThat(bookSwingView.isVisible()).isFalse();
		window.show();
		window.label("errorLabelMessage").requireText(" ");
		assertThat(window.list("bookList").contents()).isEmpty();
	}
}
