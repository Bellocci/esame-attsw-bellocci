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
import org.hibernate.cfg.Environment;
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
	
	private FrameFixture window_library;
	private FrameFixture window_book;
	
	private static Properties settings;
	
	private static LibraryMySQLRepository libraryRepository;
	private static BookMySQLRepository bookRepository;

	@SuppressWarnings("rawtypes")
	private static MySQLContainer mySQLContainer;
	
	@SuppressWarnings({ "rawtypes", "resource" })
	@BeforeClass
	public static void setupDatabase() {
		mySQLContainer = new MySQLContainer("mysql:8")
				.withDatabaseName(DB_NAME)
				.withUsername(DB_USER)
				.withPassword(DB_PASSWORD);
		mySQLContainer.start();
		
		settings = new Properties();
		
		settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
		settings.put(Environment.URL, "jdbc:mysql://"+mySQLContainer.getHost()+":"+
				mySQLContainer.getFirstMappedPort()+"/"+DB_NAME+"?useSSL=false");
		settings.put(Environment.USER, DB_USER);
		settings.put(Environment.PASS, DB_PASSWORD);
		settings.put(Environment.POOL_SIZE, "1");
		settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
		settings.put(Environment.SHOW_SQL, "true");
		settings.put(Environment.FORMAT_SQL, "true");
		settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(Environment.HBM2DDL_AUTO, "create-drop");
		settings.put(Environment.HBM2DDL_HALT_ON_ERROR, "true");
		settings.put(Environment.HBM2DDL_CREATE_SCHEMAS, "true");
		settings.put(Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true");
		
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
		window_library = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Library View".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());
	}
		
	private void addTestLibraryToDatabase(String id, String name) {
		libraryRepository.saveLibrary(new Library(id, name));
	}
	
	private void addTestBookToDatabase(String id_book, String name_book, String id_library, String name_library) {
		Book book = new Book(id_book, name_book);
		Library library = new Library(id_library, name_library);
		book.setLibrary(library);
		bookRepository.saveBookInTheLibrary(library, book);
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
	        //transaction.commit();
		} catch(Exception e) {
			if(transaction != null)
				transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
	
	@Test @GUITest
	public void testOnStartAllDatabaseElementsAreShown() {
		// verify
		assertThat(window_library.list("libraryList").contents())
			.anyMatch(e -> e.contains(LIBRARY_FIXTURE_1_ID))
			.anyMatch(e -> e.contains(LIBRARY_FIXTURE_2_ID));
	}
	
	@Test @GUITest
	public void testAddLibraryButtonSuccess() {
		// setup
		window_library.textBox("idTextBox").enterText("5");
		window_library.textBox("nameTextBox").enterText("new_library");
		
		// exercise
		window_library.button(JButtonMatcher.withText("Add library")).click();
		
		// verify
		assertThat(window_library.list("libraryList").contents()).anyMatch(e -> e.contains("5"));
	}
	
	@Test @GUITest
	public void testAddLibraryButtonError() {
		// setup
		window_library.textBox("idTextBox").enterText(LIBRARY_FIXTURE_1_ID);
		window_library.textBox("nameTextBox").enterText("existing_library");
		
		// exercise
		window_library.button(JButtonMatcher.withText("Add library")).click();
		
		// verify
		assertThat(window_library.label("errorLabelMessage").text()).contains(LIBRARY_FIXTURE_1_ID, LIBRARY_FIXTURE_1_NAME);
		assertThat(window_library.list("libraryList").contents()).noneMatch(e -> e.contains("existing_library"));
	}
	
	@Test @GUITest
	public void testDeleteLibraryButtonSuccess() {
		// setup
		window_library.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		
		// exercise
		window_library.button(JButtonMatcher.withText("Delete library")).click();
		
		// verify
		assertThat(window_library.list("libraryList").contents()).noneMatch(e -> e.contains(LIBRARY_FIXTURE_1_NAME));
	}
	
	@Test @GUITest
	public void testDeleteLibraryButtonError() {
		// setup
		window_library.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		libraryRepository.deleteLibrary(LIBRARY_FIXTURE_1_ID);
		
		// exercise
		window_library.button(JButtonMatcher.withText("Delete library")).click();
		
		// verify
		assertThat(window_library.label("errorLabelMessage").text()).contains(LIBRARY_FIXTURE_1_ID, LIBRARY_FIXTURE_1_NAME);
		assertThat(window_library.list("libraryList").contents()).noneMatch(e -> e.contains(LIBRARY_FIXTURE_1_ID));
	}
	
	@Test @GUITest
	public void testOpenLibraryButtonSuccess() {
		// setup
		window_library.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		
		// exercise
		window_library.button(JButtonMatcher.withText("Open library")).click();
		
		// verify
		createFrameFixtureWindowBook();
		assertThat(window_book.list("bookList").contents())
			.anySatisfy(e -> assertThat(e).contains(BOOK_FIXTURE_1_ID, BOOK_FIXTURE_1_NAME))
			.anySatisfy(e -> assertThat(e).contains(BOOK_FIXTURE_2_ID, BOOK_FIXTURE_2_NAME));
		window_library.show();
		window_library.label("errorLabelMessage").requireText(" ");
	}
	
	private void createFrameFixtureWindowBook() {
		window_book = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
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
		window_library.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		
		// exercise
		window_library.button(JButtonMatcher.withText("Open library")).click();
		
		// verify
		window_library.label("errorLabelMessage").requireText("Doesn't exist library with id " + LIBRARY_FIXTURE_1_ID
				+ ": " + LIBRARY_FIXTURE_1_ID + " - " + LIBRARY_FIXTURE_1_NAME);
		assertThat(window_library.list("libraryList").contents()).noneMatch(e -> e.contains(LIBRARY_FIXTURE_1_ID));
	}
	
	@Test @GUITest
	public void testAddBookButtonSuccess() {
		// setup
		window_library.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		window_library.button(JButtonMatcher.withText("Open library")).click();
		createFrameFixtureWindowBook();
		window_book.textBox("idTextBox").enterText("10");
		window_book.textBox("nameTextBox").enterText("new_book");
		
		// exercise
		window_book.button(JButtonMatcher.withText("Add book")).click();
		
		// verify
		assertThat(window_book.list("bookList").contents())
			.anyMatch(e -> e.contains("10"));
	}
	
	@Test @GUITest
	public void testAddBookButtonError() {
		// setup
		window_library.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		window_library.button(JButtonMatcher.withText("Open library")).click();
		createFrameFixtureWindowBook();
		window_book.textBox("idTextBox").enterText(BOOK_FIXTURE_1_ID);
		window_book.textBox("nameTextBox").enterText("existing_book");
		
		// exercise
		window_book.button(JButtonMatcher.withText("Add book")).click();
		
		// verify
		assertThat(window_book.label("errorLabelMessage").text()).contains(BOOK_FIXTURE_1_ID, BOOK_FIXTURE_1_NAME);
		assertThat(window_book.list("bookList").contents()).noneMatch(e -> e.contains("existing_book"));
	}
	
	@Test @GUITest
	public void testAddBookButtonErrorWhenLibraryDoesntExistIntoDatabase() {
		// setup
		window_library.list("libraryList").selectItem(Pattern.compile(".*" + LIBRARY_FIXTURE_1_NAME + ".*"));
		window_library.button(JButtonMatcher.withText("Open library")).click();
		createFrameFixtureWindowBook();
		GuiActionRunner.execute(() -> libraryRepository.deleteLibrary(LIBRARY_FIXTURE_1_ID));
		window_book.textBox("idTextBox").enterText("10");
		window_book.textBox("nameTextBox").enterText("new_book");
		
		// exercise
		window_book.button(JButtonMatcher.withText("Add book")).click();
		
		// verify
		window_book.requireNotVisible();
		window_library.requireVisible();
		window_library.label("errorLabelMessage").requireText("Doesnt exist library with id " + LIBRARY_FIXTURE_1_ID
				+ " : " + LIBRARY_FIXTURE_1_ID + " - " + LIBRARY_FIXTURE_1_NAME);
		window_book.show();
		assertThat(window_book.list("bookList").contents()).isEmpty();
		window_book.label("errorLabelMessage").requireText(" ");
	}
}
