package com.examples.esame_attsw_Bellocci.view.swing;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Properties;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.examples.esame_attsw_Bellocci.controller.BookController;
import com.examples.esame_attsw_Bellocci.controller.LibraryController;
import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.BookRepository;
import com.examples.esame_attsw_Bellocci.repository.LibraryRepository;
import com.examples.esame_attsw_Bellocci.repository.mysql.BookMySQLRepository;
import com.examples.esame_attsw_Bellocci.repository.mysql.LibraryMySQLRepository;

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
	public static void setupServer() {
		settings = new Properties();
		
		settings.put(Environment.DRIVER, "org.h2.Driver");
		settings.put(Environment.URL, "jdbc:h2:mem:test");
		settings.put(Environment.USER, "user");
		settings.put(Environment.PASS, "password");
		settings.put(Environment.POOL_SIZE, "1");
		settings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
		settings.put(Environment.SHOW_SQL, "true");
		settings.put(Environment.FORMAT_SQL, "true");
		settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(Environment.HBM2DDL_AUTO, "create-drop");
		settings.put(Environment.HBM2DDL_HALT_ON_ERROR, "true");
		settings.put(Environment.HBM2DDL_CREATE_SCHEMAS, "true");
		settings.put(Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true");
		
		library = new Library("1", "library1");
		libraryRepository = new LibraryMySQLRepository(settings);
	}
	
	@AfterClass
	public static void closeSessionFactory() {
		HibernateUtil.resetSessionFactory();
	}
	
	@Override
	protected void onSetUp() throws Exception {
		GuiActionRunner.execute(() -> {
			librarySwingView = new LibrarySwingView();
			libraryController = new LibraryController(librarySwingView, libraryRepository);
			librarySwingView.setLibraryController(libraryController);
			
			bookRepository = new BookMySQLRepository(settings);
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

	@Override
	protected void onTearDown() {
		cleanDatabaseTables();
	}
	
	private void cleanDatabaseTables() {
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        List<Library> libraries = session.createQuery("FROM Library", Library.class).list();
	        for(Library library: libraries)
	        	session.delete(library);
	        List<Book> books = session.createQuery("FROM Book", Book.class).list();
	        for(Book book: books)
	        	session.delete(book);
	        transaction.commit();
		} catch(Exception e) {
			e.printStackTrace();
			if(transaction != null)
				transaction.rollback();
		} finally {
			if(session != null)
				session.close();
		}
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
			.containsExactly(book1.toString(), book2.toString());
	}
	
	@Test @GUITest
	public void testAllBooksWhenLibraryDoesntContainedIntoDatabase() {
		// setup
		libraryRepository.deleteLibrary(library.getId());
		
		// exercise
		GuiActionRunner.execute(() -> bookController.allBooks(library));
		
		// verify
		assertThat(bookSwingView.getListBooksModel().toArray()).isEmpty();
		FrameFixture window_library = new FrameFixture(robot(), librarySwingView);
		window_library.label("errorLabelMessage").requireText("Doesnt exist library with id 1 : " + library);
	}
}
