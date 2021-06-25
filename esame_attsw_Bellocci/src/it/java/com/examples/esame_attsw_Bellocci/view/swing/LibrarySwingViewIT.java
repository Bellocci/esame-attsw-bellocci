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

import com.examples.esame_attsw_Bellocci.controller.LibraryController;
import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.LibraryRepository;
import com.examples.esame_attsw_Bellocci.repository.mysql.LibraryMySQLRepository;

@RunWith(GUITestRunner.class)
public class LibrarySwingViewIT extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	
	private LibrarySwingView librarySwingView;
	
	private LibraryController libraryController;
	
	private LibraryRepository libraryRepository;
	
	private static Properties settings;
	
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
}
