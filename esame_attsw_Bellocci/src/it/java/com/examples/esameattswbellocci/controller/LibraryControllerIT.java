package com.examples.esameattswbellocci.controller;

import static org.mockito.Mockito.verify;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MySQLContainer;

import com.examples.esameattswbellocci.hibernate.util.HibernateUtil;
import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.LibraryRepository;
import com.examples.esameattswbellocci.repository.mysql.LibraryMySQLRepository;
import com.examples.esameattswbellocci.view.LibraryView;

public class LibraryControllerIT {

	private static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8");
	
	@Mock
	private LibraryView libraryView;
	
	private LibraryRepository libraryRepository;
	
	private LibraryController libraryController;
	
	private static Properties settings;
	
	private AutoCloseable closeable;
	
	@BeforeClass
	public static void setupServerAndHibernate() {
		
		mySQLContainer
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
	}
	
	@AfterClass
	public static void shutdownServerAndCloseSessionFactory() {
		HibernateUtil.resetSessionFactory();
		mySQLContainer.stop();
	}
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		libraryRepository = new LibraryMySQLRepository();
		libraryController = new LibraryController(libraryView, libraryRepository);
	}
	
	@After
	public void releaseMocksAndCleanDatabase() throws Exception {
		closeable.close();
		cleanDatabaseTables();
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
	public void testAllLibraries() {
		// setup
		Library library = new Library("1", "library1");
		libraryRepository.saveLibrary(library);
		
		// exercise
		libraryController.allLibraries();
		
		// verify
		verify(libraryView).showAllLibraries(asList(library));
	}
	
	@Test
	public void testFindLibrary() {
		// setup
		Library library = new Library("1", "library1");
		libraryRepository.saveLibrary(library);
		
		// exercise
		libraryController.findLibrary(library);
		
		// verify
		verify(libraryView).showAllBooksOfLibrary(library);
	}
	
	@Test
	public void testNewLibrary() {
		// setup
		Library newLibrary = new Library("1", "library1");
		
		// exercise
		libraryController.newLibrary(newLibrary);
		
		// verify
		verify(libraryView).libraryAdded(newLibrary);
	}
	
	@Test
	public void testDeleteLibrary() {
		// setup
		Library libraryDeleted = new Library("1", "library1");
		libraryRepository.saveLibrary(libraryDeleted);
		
		// exercise
		libraryController.deleteLibrary(libraryDeleted);
		
		// verify
		verify(libraryView).libraryRemoved(libraryDeleted);
	}
}
