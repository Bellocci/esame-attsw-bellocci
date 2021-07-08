package com.examples.esameattswbellocci.view.swing;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Properties;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.examples.esameattswbellocci.controller.BookController;
import com.examples.esameattswbellocci.controller.LibraryController;
import com.examples.esameattswbellocci.hibernate.util.HibernateUtil;
import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.BookRepository;
import com.examples.esameattswbellocci.repository.LibraryRepository;
import com.examples.esameattswbellocci.repository.mysql.BookMySQLRepository;
import com.examples.esameattswbellocci.repository.mysql.LibraryMySQLRepository;

@RunWith(GUITestRunner.class)
public class BookSwingViewIT extends AssertJSwingJUnitTestCase {
	
	private FrameFixture window;
	
	private LibrarySwingView librarySwingView;
	private BookSwingView bookSwingView;
	
	private LibraryController libraryController;
	private BookController bookController;
	
	private static LibraryRepository libraryRepository;
	private BookRepository bookRepository;
	
	private static Properties settings;
	
	private static Library library;
	
	@BeforeClass
	public static void setupHibernateWithH2() {
		settings = new Properties();
		
		settings.put(AvailableSettings.DRIVER, "org.h2.Driver");
		settings.put(AvailableSettings.URL, "jdbc:h2:mem:test");
		settings.put(AvailableSettings.USER, "user");
		settings.put(AvailableSettings.PASS, "password");
		settings.put(AvailableSettings.POOL_SIZE, "1");
		settings.put(AvailableSettings.DIALECT, "org.hibernate.dialect.H2Dialect");
		settings.put(AvailableSettings.SHOW_SQL, "true");
		settings.put(AvailableSettings.FORMAT_SQL, "true");
		settings.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
		settings.put(AvailableSettings.HBM2DDL_HALT_ON_ERROR, "true");
		settings.put(AvailableSettings.HBM2DDL_CREATE_SCHEMAS, "true");
		settings.put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, "true");
		
		HibernateUtil.setProperties(settings);
		
		library = new Library("1", "library1");
		libraryRepository = new LibraryMySQLRepository();
	}
	
	@AfterClass
	public static void closeSessionFactory() {
		HibernateUtil.closeSessionFactory();
	}
	
	@Override
	protected void onSetUp() throws Exception {
		cleanDatabaseTables();
		
		GuiActionRunner.execute(() -> {
			librarySwingView = new LibrarySwingView();
			libraryController = new LibraryController(librarySwingView, libraryRepository);
			librarySwingView.setLibraryController(libraryController);
			
			bookRepository = new BookMySQLRepository();
			bookSwingView = new BookSwingView();
			bookSwingView.setLibrary(library);
			bookSwingView.setLibrarySwingView(librarySwingView);
			bookController = new BookController(bookRepository, bookSwingView, libraryController);
			bookSwingView.setBookController(bookController);
		});
		libraryRepository.saveLibrary(library);
		window = new FrameFixture(robot(), bookSwingView);
		window.show(); // shows the frame to test
	}
	
	private void cleanDatabaseTables() {
		Session session = HibernateUtil.getSessionFactory().openSession();
	    Transaction transaction = session.beginTransaction();
	    List<Library> libraries = session.createQuery("FROM Library", Library.class).list();
	    for(Library library: libraries)
	       	session.delete(library);
	    List<Book> books = session.createQuery("FROM Book", Book.class).list();
	    for(Book book: books)
	       	session.delete(book);
	    transaction.commit();
		session.close();
	}
	
	@Test @GUITest
	public void testAllBooksWhenLibraryIsContainedIntoDatabase() {
		// setup
		Book book1 = new Book("1", "book1");
		book1.setLibrary(library);
		Book book2 = new Book("2", "book2");
		book2.setLibrary(library);
		bookRepository.saveBookInTheLibrary(library, book1);
		bookRepository.saveBookInTheLibrary(library, book2);
		
		// exercise
		GuiActionRunner.execute(() -> bookController.allBooks(library));
		
		// verify
		assertThat(window.list("bookList").contents())
			.anyMatch(e -> e.contains("1 - book1"))
			.anyMatch(e -> e.contains("2 - book2"));
	}
	
	@Test @GUITest
	public void testAllBooksWhenLibraryDoesntContainedIntoDatabase() {
		// setup
		libraryRepository.deleteLibrary(library.getId());
		GuiActionRunner.execute(() -> librarySwingView.libraryAdded(library));
		
		// exercise
		GuiActionRunner.execute(() -> bookController.allBooks(library));
		
		// verify
		assertThat(bookSwingView.getListBooksModel().toArray()).isEmpty();
		FrameFixture windowLibrary = new FrameFixture(robot(), librarySwingView);
		windowLibrary.label("errorLabelMessage")
			.requireText("Doesnt exist library with id 1 : " + library.getId() + " - " + library.getName());
		assertThat(windowLibrary.list("libraryList").contents())
			.noneMatch(e -> e.contains("1 - library1"));
	}
	
	@Test @GUITest
	public void testAddBookButtonSuccess() {
		// setup
		window.textBox("idTextBox").enterText("10");
		window.textBox("nameTextBox").enterText("new_book");
		
		// exercise
		window.button(JButtonMatcher.withText("Add book")).click();
		
		// verify
		assertThat(window.list("bookList").contents())
			.anyMatch(e -> e.contains("10 - new_book"));
	}
	
	@Test @GUITest
	public void testAddBookButtonErrorWhenBookAlreadyExistIntoDatabase() {
		// setup
		Book book1 = new Book("1", "existing");
		book1.setLibrary(library);
		bookRepository.saveBookInTheLibrary(library, book1);
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("book1");
		
		// exercise
		window.button(JButtonMatcher.withText("Add book")).click();
		
		// verify
		window.label("errorLabelMessage").requireText("Already existing book with id 1 : 1 - existing");
	}
	
	@Test @GUITest
	public void testAddBookButtonErrorWhenLibraryDoesntExistIntoDatabase() {
		// setup
		libraryRepository.deleteLibrary(library.getId());
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("book1");
		GuiActionRunner.execute(() -> bookSwingView.getLblErrorMessage().setText("Error"));
		
		// exercise
		window.button(JButtonMatcher.withText("Add book")).click();

		// verify
		assertThat(bookSwingView.getLblErrorMessage().getText()).isEqualTo(" ");
		assertThat(bookSwingView.getListBooksModel().toArray()).isEmpty();
		FrameFixture window_library = new FrameFixture(robot(), librarySwingView);
		window_library.label("errorLabelMessage")
			.requireText("Doesnt exist library with id 1 : " + library.getId() + " - " + library.getName());
		assertThat(window_library.list("libraryList").contents())
			.noneMatch(e -> e.contains(library.getId() + " - " + library.getName()));
	}
	
	@Test @GUITest
	public void testDeleteBookButtonSuccess() {
		// setup
		Book book1 = new Book("1", "book1");
		book1.setLibrary(library);
		GuiActionRunner.execute(() -> bookController.newBook(library, book1));
		window.list("bookList").selectItem(0);
		
		// exercise
		window.button(JButtonMatcher.withText("Delete book")).click();
		
		// verify
		assertThat(window.list("bookList").contents()).isEmpty();
	}

	@Test @GUITest
	public void testDeleteBookButtonErrorWhenBookDoesntExistIntoDatabase() {
		// setup
		Book book1 = new Book("1", "book1");
		GuiActionRunner.execute(() -> bookSwingView.bookAdded(book1));
		window.list("bookList").selectItem(0);
		
		// exercise
		window.button(JButtonMatcher.withText("Delete book")).click();
		
		// verify
		assertThat(window.list("bookList").contents()).noneMatch(e -> e.contains("1 - book1"));
		window.label("errorLabelMessage").requireText("No existing book with id 1 : 1 - book1");
	}
	
	@Test @GUITest
	public void testDeleteBookButtonErrorWhenLibraryDoesntExistIntoDatabase() {
		// setup
		Book book1 = new Book("1", "book1");
		GuiActionRunner.execute(() -> {
			bookSwingView.bookAdded(book1);
			libraryRepository.deleteLibrary(library.getId());
		});
		window.list("bookList").selectItem(0);
		
		// exercise
		window.button(JButtonMatcher.withText("Delete book")).click();
		
		// verify
		assertThat(bookSwingView.getLblErrorMessage().getText()).isEqualTo(" ");
		assertThat(bookSwingView.getListBooksModel().toArray()).isEmpty();
		FrameFixture window_library = new FrameFixture(robot(), librarySwingView);
		window_library.label("errorLabelMessage")
			.requireText("Doesnt exist library with id 1 : " + library.getId() + " - " + library.getName());
		assertThat(window_library.list("libraryList").contents())
			.noneMatch(e -> e.contains(library.getId() + " - " + library.getName()));
	}
	
	@Test @GUITest
	public void testBackToLibrariesButton() {
		// setup
		Book book = new Book("1", "book1");
		book.setLibrary(library);
		GuiActionRunner.execute(() -> {
			bookController.newBook(library, book);
			librarySwingView.getListLibraryModel().addElement(library);
		});
		assertThat(bookSwingView.getListBooksModel().toArray()).hasSize(1);
		
		// exercise
		window.button(JButtonMatcher.withText("Back to libraries")).click();
		
		// verify
		assertThat(bookSwingView.getListBooksModel().toArray()).isEmpty();
		assertThat(bookSwingView.isVisible()).isFalse();
		assertThat(librarySwingView.isVisible()).isTrue();
		window.show();
		window.label("errorLabelMessage").requireText(" ");
	}
}
