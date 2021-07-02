package com.examples.esameattswbellocci.view.swing;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Properties;

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
import org.testcontainers.containers.MySQLContainer;

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
public class ModelViewControllerBookIT extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	
	private LibrarySwingView librarySwingView;
	
	private LibraryController libraryController;
	private BookController bookController;
	
	private static LibraryRepository libraryRepository;
	private BookRepository bookRepository;
	
	@SuppressWarnings("rawtypes")
	private static MySQLContainer mySQLContainer;
	
	private static Properties settings;
	
	private Library library = new Library("1", "library1");;
	
	@SuppressWarnings({ "rawtypes", "resource" })
	@BeforeClass
	public static void setupServerAndHibernate() {
		mySQLContainer = new MySQLContainer("mysql:8")
				.withDatabaseName("test")
				.withUsername("user")
				.withPassword("password");
		mySQLContainer.start();
		
		settings = new Properties();
		
		settings.put(AvailableSettings.DRIVER, mySQLContainer.getDriverClassName());
		settings.put(AvailableSettings.URL, mySQLContainer.getJdbcUrl());
		settings.put(AvailableSettings.USER, mySQLContainer.getUsername());
		settings.put(AvailableSettings.PASS, mySQLContainer.getPassword());
		settings.put(AvailableSettings.POOL_SIZE, "1");
		settings.put(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
		settings.put(AvailableSettings.SHOW_SQL, "true");
		settings.put(AvailableSettings.FORMAT_SQL, "true");
		settings.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
		settings.put(AvailableSettings.HBM2DDL_HALT_ON_ERROR, "true");
		settings.put(AvailableSettings.HBM2DDL_CREATE_SCHEMAS, "true");
		settings.put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, "true");
		
		HibernateUtil.setProperties(settings);
		libraryRepository = new LibraryMySQLRepository();
	}
	
	@AfterClass
	public static void shutdownServerAndHibernate() {
		HibernateUtil.resetSessionFactory();
		mySQLContainer.stop();
	}

	@Override
	protected void onSetUp() throws Exception {
		cleanDatabaseTables();
		
		bookRepository = new BookMySQLRepository();
		libraryRepository.saveLibrary(library);
		window = new FrameFixture(robot(), GuiActionRunner.execute(() -> {
			librarySwingView = new LibrarySwingView();
			libraryController = new LibraryController(librarySwingView, libraryRepository);
			
			BookSwingView bookSwingView = new BookSwingView();
			bookSwingView.setLibrarySwingView(librarySwingView);
			bookSwingView.setLibrary(library);
			bookController = new BookController(bookRepository, bookSwingView, libraryController);
			bookSwingView.setBookController(bookController);
			return bookSwingView;
		}));
		window.show();
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
	
	@Test
	public void testAddBook() {
		// setup
		Book book = new Book("1", "book1");
		book.setLibrary(library);
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("book1");
		
		// exercise
		window.button(JButtonMatcher.withText("Add book")).click();
		
		// verify
		assertThat(bookRepository.findBookById("1")).isEqualTo(book);
	}
	
	@Test
	public void testDeleteBook() {
		// setup
		Book book = new Book("1", "book1");
		book.setLibrary(library);
		bookRepository.saveBookInTheLibrary(library, book);
		GuiActionRunner.execute(() -> bookController.allBooks(library));
		window.list("bookList").selectItem(0);
		
		// exercise
		window.button(JButtonMatcher.withText("Delete book")).click();
		
		// verify
		assertThat(bookRepository.findBookById(book.getId())).isNull();
	}

}
