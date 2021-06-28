package com.examplesesame_attsw_Bellocci.view.swing;

import static org.assertj.swing.launcher.ApplicationLauncher.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.WindowFinder;
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

import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.mysql.BookMySQLRepository;
import com.examples.esame_attsw_Bellocci.repository.mysql.LibraryMySQLRepository;

@RunWith(GUITestRunner.class)
public class LibrarySwingAppE2E extends AssertJSwingJUnitTestCase {

	private static final String LIBRARY_FIXTURE_1_ID = "1";
	private static final String LIBRARY_FIXTURE_1_NAME = "library1";
	private static final String LIBRARY_FIXTURE_2_ID = "2";
	private static final String LIBRARY_FIXTURE_2_NAME = "library2";
	
	private static final String BOOK_FIXTURE_1_ID = "1";
	private static final String BOOK_FIXTURE_1_NAME = "book1";
	private static final String BOOK_FIXTURE_2_ID = "2";
	private static final String BOOK_FIXTURE_2_NAME = "book2";

	private static final String DB_NAME = "test";
	
	private static final String DB_USER = "user";
	
	private static final String DB_PASSWORD = "password";
	
	private FrameFixture windowLibrary;
	private FrameFixture windowBook;
	
	private static Properties settings;
	
	private static LibraryMySQLRepository libraryRepository;
	private static BookMySQLRepository bookRepository;

	private static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8");
	
	@BeforeClass
	public static void setupDatabase() {
		mySQLContainer
			.withDatabaseName(DB_NAME)
			.withUsername(DB_USER)
			.withPassword(DB_PASSWORD);
		mySQLContainer.start();
		
		settings = new Properties();
		
		settings.put(AvailableSettings.DRIVER, "com.mysql.cj.jdbc.Driver");
		settings.put(AvailableSettings.URL, "jdbc:mysql://"+mySQLContainer.getHost()+":"+
				mySQLContainer.getFirstMappedPort()+"/"+DB_NAME+"?useSSL=false");
		settings.put(AvailableSettings.USER, DB_USER);
		settings.put(AvailableSettings.PASS, DB_PASSWORD);
		settings.put(AvailableSettings.POOL_SIZE, "1");
		settings.put(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
		settings.put(AvailableSettings.SHOW_SQL, "true");
		settings.put(AvailableSettings.FORMAT_SQL, "true");
		settings.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
		settings.put(AvailableSettings.HBM2DDL_HALT_ON_ERROR, "true");
		settings.put(AvailableSettings.HBM2DDL_CREATE_SCHEMAS, "true");
		settings.put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, "true");
		
		libraryRepository = new LibraryMySQLRepository(settings);
		bookRepository = new BookMySQLRepository(settings);
	}
	
	@AfterClass
	public static void shutdownServerAndClearHibernateUtil() {
		HibernateUtil.resetSessionFactory();
		mySQLContainer.stop();
	}
	
	@Override
	protected void onSetUp() throws Exception {
		cleanDatabaseTables();
		addTestLibraryToDatabase(LIBRARY_FIXTURE_1_ID, LIBRARY_FIXTURE_1_NAME);
		addTestLibraryToDatabase(LIBRARY_FIXTURE_2_ID, LIBRARY_FIXTURE_2_NAME);
		
		addTestBookToDatabase(BOOK_FIXTURE_1_ID, BOOK_FIXTURE_1_NAME, LIBRARY_FIXTURE_1_ID, LIBRARY_FIXTURE_1_NAME);
		addTestBookToDatabase(BOOK_FIXTURE_2_ID, BOOK_FIXTURE_2_NAME, LIBRARY_FIXTURE_1_ID, LIBRARY_FIXTURE_1_NAME);
		
		// start the Swing application
		
		application("com.examples.esame_attsw_Bellocci.app.swing.LibrarySwingApp")
			.withArgs(
					"--useHibernateCfgXML=" + "false",
					"--mysql-host=" + mySQLContainer.getHost(),
					"--mysql-port=" + mySQLContainer.getFirstMappedPort(),
					"--db-name=" + DB_NAME,
					"--db-user=" + DB_USER,
					"--db-password=" + DB_PASSWORD
					)
			.start();
		windowLibrary = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Library View".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());
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
			if(transaction != null & transaction.isActive())
				transaction.rollback();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}
		
	private void addTestLibraryToDatabase(String id, String name) {
		libraryRepository.saveLibrary(new Library(id, name));
	}
	
	private void addTestBookToDatabase(String idBook, String nameBook, String idLibrary, String nameLibrary) {
		Book book = new Book(idBook, nameBook);
		Library library = new Library(idLibrary, nameLibrary);
		book.setLibrary(library);
		bookRepository.saveBookInTheLibrary(library, book);
	}
	
	@Test @GUITest
	public void testOnStartAllDatabaseElementsAreShown() {
		// verify
		assertThat(windowLibrary.list("libraryList").contents())
			.anyMatch(e -> e.contains(LIBRARY_FIXTURE_1_ID))
			.anyMatch(e -> e.contains(LIBRARY_FIXTURE_2_ID));
	}
	
	@Test @GUITest
	public void testAddLibraryButtonSuccess() {
		// setup
		windowLibrary.textBox("idTextBox").enterText("5");
		windowLibrary.textBox("nameTextBox").enterText("new_library");
		
		// exercise
		windowLibrary.button(JButtonMatcher.withText("Add library")).click();
		
		// verify
		assertThat(windowLibrary.list("libraryList").contents()).anyMatch(e -> e.contains("5"));
	}
	
	@Test @GUITest
	public void testAddLibraryButtonError() {
		// setup
		windowLibrary.textBox("idTextBox").enterText(LIBRARY_FIXTURE_1_ID);
		windowLibrary.textBox("nameTextBox").enterText("existing_library");
		
		// exercise
		windowLibrary.button(JButtonMatcher.withText("Add library")).click();
		
		// verify
		assertThat(windowLibrary.label("errorLabelMessage").text()).
			contains(LIBRARY_FIXTURE_1_ID, LIBRARY_FIXTURE_1_NAME);
		assertThat(windowLibrary.list("libraryList").contents())
			.noneMatch(e -> e.contains("existing_library"));
	}
	
	@Test @GUITest
	public void testDeleteLibraryButtonSuccess() {
		// setup
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		
		// exercise
		windowLibrary.button(JButtonMatcher.withText("Delete library")).click();
		
		// verify
		assertThat(windowLibrary.list("libraryList").contents())
			.noneMatch(e -> e.contains(LIBRARY_FIXTURE_1_NAME));
	}
	
	@Test @GUITest
	public void testDeleteLibraryButtonError() {
		// setup
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		libraryRepository.deleteLibrary(LIBRARY_FIXTURE_1_ID);
		
		// exercise
		windowLibrary.button(JButtonMatcher.withText("Delete library")).click();
		
		// verify
		assertThat(windowLibrary.label("errorLabelMessage").text())
			.contains(LIBRARY_FIXTURE_1_ID, LIBRARY_FIXTURE_1_NAME);
		assertThat(windowLibrary.list("libraryList").contents())
			.noneMatch(e -> e.contains(LIBRARY_FIXTURE_1_ID));
	}
	
	@Test @GUITest
	public void testOpenLibraryButtonSuccess() {
		// setup
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		
		// exercise
		windowLibrary.button(JButtonMatcher.withText("Open library")).click();
		
		// verify
		createFrameFixtureWindowBook();
		assertThat(windowBook.list("bookList").contents())
			.anySatisfy(e -> assertThat(e).contains(BOOK_FIXTURE_1_ID + " - " + BOOK_FIXTURE_1_NAME))
			.anySatisfy(e -> assertThat(e).contains(BOOK_FIXTURE_2_ID + " - " + BOOK_FIXTURE_2_NAME));
		windowLibrary.show();
		windowLibrary.label("errorLabelMessage").requireText(" ");
	}
	
	private void createFrameFixtureWindowBook() {
		windowBook = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Book View".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());
	}
	
	@Test @GUITest
	public void testOpenLibraryButtonError() {
		// setup
		GuiActionRunner.execute(() -> libraryRepository.deleteLibrary(LIBRARY_FIXTURE_1_ID));
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		
		// exercise
		windowLibrary.button(JButtonMatcher.withText("Open library")).click();
		
		// verify
		windowLibrary.label("errorLabelMessage")
			.requireText("Doesn't exist library with id 1 : 1 - library1");
		assertThat(windowLibrary.list("libraryList").contents())
			.noneMatch(e -> e.contains("1 - library1"));
	}
	
	@Test @GUITest
	public void testAddBookButtonSuccess() {
		// setup
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		windowLibrary.button(JButtonMatcher.withText("Open library")).click();
		createFrameFixtureWindowBook();
		windowBook.textBox("idTextBox").enterText("10");
		windowBook.textBox("nameTextBox").enterText("new_book");
		
		// exercise
		windowBook.button(JButtonMatcher.withText("Add book")).click();
		
		// verify
		assertThat(windowBook.list("bookList").contents())
			.anyMatch(e -> e.contains("10"));
	}
	
	@Test @GUITest
	public void testAddBookButtonError() {
		// setup
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		windowLibrary.button(JButtonMatcher.withText("Open library")).click();
		createFrameFixtureWindowBook();
		windowBook.textBox("idTextBox").enterText(BOOK_FIXTURE_1_ID);
		windowBook.textBox("nameTextBox").enterText("existing_book");
		
		// exercise
		windowBook.button(JButtonMatcher.withText("Add book")).click();
		
		// verify
		assertThat(windowBook.label("errorLabelMessage").text()).contains(BOOK_FIXTURE_1_ID, BOOK_FIXTURE_1_NAME);
		assertThat(windowBook.list("bookList").contents()).noneMatch(e -> e.contains("existing_book"));
	}
	
	@Test @GUITest
	public void testAddBookButtonErrorWhenLibraryDoesntExistIntoDatabase() {
		// setup
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		windowLibrary.button(JButtonMatcher.withText("Open library")).click();
		createFrameFixtureWindowBook();
		GuiActionRunner.execute(() -> libraryRepository.deleteLibrary(LIBRARY_FIXTURE_1_ID));
		windowBook.textBox("idTextBox").enterText("10");
		windowBook.textBox("nameTextBox").enterText("new_book");
		
		// exercise
		windowBook.button(JButtonMatcher.withText("Add book")).click();
		
		// verify
		windowBook.requireNotVisible();
		windowLibrary.requireVisible();
		windowLibrary.label("errorLabelMessage")
			.requireText("Doesnt exist library with id 1 : 1 - library1");
		windowBook.show();
		assertThat(windowBook.list("bookList").contents()).isEmpty();
		windowBook.label("errorLabelMessage").requireText(" ");
	}
	
	@Test @GUITest
	public void testDeleteBookButtonSuccess() {
		// setup
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		windowLibrary.button(JButtonMatcher.withText("Open library")).click();
		createFrameFixtureWindowBook();
		windowBook.list("bookList").selectItem(Pattern.compile(".*" + BOOK_FIXTURE_1_NAME + ".*"));
		
		// exercise
		windowBook.button(JButtonMatcher.withText("Delete book")).click();
		
		// verify
		assertThat(windowBook.list("bookList").contents()).noneMatch(e -> e.contains(BOOK_FIXTURE_1_NAME));
	}
	
	@Test @GUITest
	public void testDeleteBookButtonError() {
		// setup
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		windowLibrary.button(JButtonMatcher.withText("Open library")).click();
		createFrameFixtureWindowBook();
		windowBook.list("bookList").selectItem(Pattern.compile(".*" + BOOK_FIXTURE_1_NAME + ".*"));
		bookRepository.deleteBookFromLibrary(LIBRARY_FIXTURE_1_ID, BOOK_FIXTURE_1_ID);
		
		// exercise
		windowBook.button(JButtonMatcher.withText("Delete book")).click();
		
		// verify
		assertThat(windowBook.list("bookList").contents()).noneMatch(e -> e.contains(BOOK_FIXTURE_1_NAME));
		assertThat(windowBook.label("errorLabelMessage").text()).contains(BOOK_FIXTURE_1_ID, BOOK_FIXTURE_1_NAME);
	}
	
	@Test @GUITest
	public void testDeleteBookButtonErrorWhenLibraryDoesntExistIntoDatabase() {
		// setup
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		windowLibrary.button(JButtonMatcher.withText("Open library")).click();
		createFrameFixtureWindowBook();
		GuiActionRunner.execute(() -> libraryRepository.deleteLibrary(LIBRARY_FIXTURE_1_ID));
		windowBook.list("bookList").selectItem(Pattern.compile(".*" + BOOK_FIXTURE_1_NAME + ".*"));
		
		// exercise
		windowBook.button(JButtonMatcher.withText("Delete book")).click();
		
		// verify
		windowBook.requireNotVisible();
		windowLibrary.requireVisible();
		windowLibrary.label("errorLabelMessage")
			.requireText("Doesnt exist library with id 1 : 1 - library1");
		windowBook.show();
		assertThat(windowBook.list("bookList").contents()).isEmpty();
		windowBook.label("errorLabelMessage").requireText(" ");
	}
	
	@Test @GUITest
	public void testBackToLibrariesButton() {
		// setup
		windowLibrary.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		windowLibrary.button(JButtonMatcher.withText("Open library")).click();
		createFrameFixtureWindowBook();
		windowBook.textBox("idTextBox").enterText(BOOK_FIXTURE_1_ID);
		windowBook.textBox("nameTextBox").enterText("existing_book");
		windowBook.button(JButtonMatcher.withText("Add book")).click();
		assertThat(windowBook.list("bookList").contents()).hasSize(2);
		
		// exercise
		windowBook.button(JButtonMatcher.withText("Back to libraries")).click();
		
		// verify
		windowBook.requireNotVisible();
		windowLibrary.requireVisible();
		windowBook.show();
		assertThat(windowBook.list("bookList").contents()).isEmpty();
		windowBook.label("errorLabelMessage").requireText(" ");
	}
}
