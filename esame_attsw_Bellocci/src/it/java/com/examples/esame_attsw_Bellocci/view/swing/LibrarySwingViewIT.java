package com.examples.esame_attsw_Bellocci.view.swing;

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
public class LibrarySwingViewIT extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	
	private LibrarySwingView librarySwingView;
	private LibraryController libraryController;
	private LibraryRepository libraryRepository;
	
	private BookSwingView bookSwingView;
	private BookController bookController;
	private BookRepository bookRepository;
	
	private static Properties settings;
	
	@BeforeClass
	public static void setupServer() {
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
	}
	
	@AfterClass
	public static void closeSessionFactory() {
		HibernateUtil.resetSessionFactory();
	}

	@Override
	protected void onSetUp() throws Exception {
		GuiActionRunner.execute(() -> {
			libraryRepository = new LibraryMySQLRepository(settings);
			librarySwingView = new LibrarySwingView();
			libraryController = new LibraryController(librarySwingView, libraryRepository);
			librarySwingView.setLibraryController(libraryController);
		});
		window = new FrameFixture(robot(), librarySwingView);
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
			// start a transaction
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
	public void testAllLibraries() {
		// setup
		libraryRepository.saveLibrary(new Library("1", "library1"));
		libraryRepository.saveLibrary(new Library("2", "library2"));
		
		// exercise
		GuiActionRunner.execute(() -> libraryController.getAllLibraries());
		
		// verify
		assertThat(window.list("libraryList").contents())
			.containsExactly(
					new Library("1", "library1").toString(),
					new Library("2", "library2").toString()
			);
	}
	
	@Test @GUITest
	public void testAddLibraryButtonSuccess() {
		// setup
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("test");
		
		// exercise
		window.button(JButtonMatcher.withText("Add library")).click();
		
		// verify
		assertThat(window.list("libraryList").contents()).anyMatch(e -> e.contains("1"));
	}
	
	@Test @GUITest
	public void testAddLibraryButtonError() {
		// setup
		libraryRepository.saveLibrary(new Library("1", "library1"));		
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("test");
		
		// exercise
		window.button(JButtonMatcher.withText("Add library")).click();
		
		// verify
		assertThat(window.list().contents()).isEmpty();
		window.label("errorLabelMessage").requireText("Already existing library with id 1: 1 - library1");
	}
	
	@Test @GUITest
	public void testDeleteLibraryButtonSuccess() {
		// setup
		GuiActionRunner.execute(() -> libraryController.newLibrary(new Library("1", "test")));
		window.list("libraryList").selectItem(0);
		
		// exercise
		window.button(JButtonMatcher.withText("Delete library")).click();
		
		// verify
		assertThat(window.list("libraryList").contents()).isEmpty();
	}
	
	@Test @GUITest
	public void testDeleteLibraryButtonError() {
		// setup
		Library library = new Library("1", "library1");
		GuiActionRunner.execute(() -> librarySwingView.libraryAdded(library));
		window.list("libraryList").selectItem(0);
		
		// exercise
		window.button(JButtonMatcher.withText("Delete library")).click();
		
		// verify
		assertThat(window.list("libraryList").contents()).noneMatch(e -> e.contains("1"));
		window.label("errorLabelMessage").requireText("Doesn't exist library with id 1: 1 - library1");
	}
	
	@Test @GUITest
	public void testOpenLibraryButtonSucess() {
		// setup
		initBookMVC();
		Library library = new Library("1", "library1");
		Book book1 = new Book("1", "book1");
		book1.setLibrary(library);
		Book book2 = new Book("2", "book2");
		book2.setLibrary(library);
		GuiActionRunner.execute(() -> {
			libraryController.newLibrary(library);
			bookRepository.saveBookInTheLibrary(library, book1);
			bookRepository.saveBookInTheLibrary(library, book2);
			librarySwingView.getLblErrorMessage().setText("Error");
		});
		window.list("libraryList").selectItem(0);
		
		// exercise
		window.button(JButtonMatcher.withText("Open library")).click();
		
		// verify
		assertThat(bookSwingView.isVisible()).isTrue();
		assertThat(bookSwingView.getListBooksModel().toArray())
			.containsExactly(book1, book2);
		assertThat(librarySwingView.isVisible()).isFalse();
		assertThat(librarySwingView.getLblErrorMessage().getText()).isEqualTo(" ");
	}
	
	private void initBookMVC() {
		GuiActionRunner.execute(() -> {
			bookSwingView = new BookSwingView();
			bookRepository = new BookMySQLRepository(settings);
			bookController = new BookController(bookRepository, bookSwingView, libraryController);
			bookSwingView.setBookController(bookController);
			bookSwingView.setLibrarySwingView(librarySwingView);
			librarySwingView.setBookSwingView(bookSwingView);
		});
	}
	
	@Test @GUITest
	public void testOpenLibraryButtonError() {
		// setup
		Library library = new Library("1", "library1");
		GuiActionRunner.execute(() -> {
			librarySwingView.libraryAdded(library);
		});
		window.list("libraryList").selectItem(0);
		
		// exercise
		window.button(JButtonMatcher.withText("Open library")).click();
		
		// verify
		assertThat(window.list("libraryList").contents()).noneMatch(e -> e.contains("library1"));
		window.label("errorLabelMessage").requireText("Doesn't exist library with id 1: " + library.toString());
	}
}
