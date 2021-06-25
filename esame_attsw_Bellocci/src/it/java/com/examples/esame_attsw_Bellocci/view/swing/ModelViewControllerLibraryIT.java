package com.examples.esame_attsw_Bellocci.view.swing;

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

import com.examples.esame_attsw_Bellocci.controller.LibraryController;
import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.LibraryRepository;
import com.examples.esame_attsw_Bellocci.repository.mysql.LibraryMySQLRepository;

@RunWith(GUITestRunner.class)
public class ModelViewControllerLibraryIT extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	
	private LibraryController libraryController;
	
	private static LibraryRepository libraryRepository;
	
	@SuppressWarnings("rawtypes")
	private static MySQLContainer mySQLContainer;
	
	private static Properties settings;
	
	@SuppressWarnings({ "rawtypes", "resource" })
	@BeforeClass
	public static void setupServerAndHibernate() {
		mySQLContainer = new MySQLContainer("mysql:8")
				.withDatabaseName("test")
				.withUsername("user")
				.withPassword("password");
		mySQLContainer.start();
		
		//Map<String, String> settings = new HashMap<>();
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
		
		libraryRepository = new LibraryMySQLRepository(settings);
	}
	
	@AfterClass
	public static void shutdownServerAndHibernate() {
		HibernateUtil.resetSessionFactory();
		mySQLContainer.stop();
	}

	@Override
	protected void onSetUp() throws Exception {
		libraryRepository = new LibraryMySQLRepository(settings);
		window = new FrameFixture(robot(), GuiActionRunner.execute(() -> {
			LibrarySwingView librarySwingView = new LibrarySwingView();
			libraryController = new LibraryController(librarySwingView, libraryRepository);
			librarySwingView.setLibraryController(libraryController);
			return librarySwingView;
		}));
		window.show();
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

	@Test
	public void testAddLibrary() {
		// setup
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("library1");
		
		// exercise
		window.button(JButtonMatcher.withText("Add library")).click();
		
		// verify
		assertThat(libraryRepository.findLibraryById("1")).isEqualTo(new Library("1", "library1"));
	}
	
	@Test
	public void testDeleteLibrary() {
		// setup
		libraryRepository.saveLibrary(new Library("1", "library1"));
		GuiActionRunner.execute(() -> libraryController.getAllLibraries());
		window.list("libraryList").selectItem(0);
		
		// exercise
		window.button(JButtonMatcher.withText("Delete library")).click();
		
		// verify
		assertThat(libraryRepository.findLibraryById("1")).isNull();
	}
}
