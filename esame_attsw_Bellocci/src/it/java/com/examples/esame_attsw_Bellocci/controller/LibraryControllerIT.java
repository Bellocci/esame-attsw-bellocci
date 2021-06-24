package com.examples.esame_attsw_Bellocci.controller;

import static org.mockito.Mockito.verify;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MySQLContainer;

import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.LibraryRepository;
import com.examples.esame_attsw_Bellocci.repository.mysql.LibraryMySQLRepository;
import com.examples.esame_attsw_Bellocci.view.LibraryView;

public class LibraryControllerIT {

	@SuppressWarnings("rawtypes")
	private static MySQLContainer mySQLContainer;
	
	@Mock
	private LibraryView libraryView;
	
	private LibraryRepository libraryRepository;
	
	private LibraryController libraryController;
	
	private static Properties settings;
	
	private AutoCloseable closeable;
	
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
		
		settings.put(Environment.DRIVER, mySQLContainer.getDriverClassName());
		settings.put(Environment.URL, mySQLContainer.getJdbcUrl());
		settings.put(Environment.USER, mySQLContainer.getUsername());
		settings.put(Environment.PASS, mySQLContainer.getPassword());
		settings.put(Environment.POOL_SIZE, "1");
		settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
		settings.put(Environment.SHOW_SQL, "true");
		settings.put(Environment.FORMAT_SQL, "true");
		settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(Environment.HBM2DDL_AUTO, "create-drop");
		settings.put(Environment.HBM2DDL_HALT_ON_ERROR, "true");
		settings.put(Environment.HBM2DDL_CREATE_SCHEMAS, "true");
		settings.put(Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true");
		
	}
	
	@AfterClass
	public static void shutdownServerAndCloseSessionFactory() {
		HibernateUtil.resetSessionFactory();
		mySQLContainer.stop();
	}
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		libraryRepository = new LibraryMySQLRepository(settings);
		libraryController = new LibraryController(libraryView, libraryRepository);
	}
	
	@After
	public void releaseMocksAndCleanDatabase() throws Exception {
		closeable.close();
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
	        //transaction.commit();
		} catch(Exception e) {
			if(transaction != null)
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null)
				session.close();
		}
	}
	
	@Test
	public void testGetAllLibraries() {
		// setup
		Library library = new Library("1", "library1");
		libraryRepository.saveLibrary(library);
		
		// exercise
		libraryController.getAllLibraries();
		
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
		Library new_library = new Library("1", "library1");
		
		// exercise
		libraryController.newLibrary(new_library);
		
		// verify
		verify(libraryView).libraryAdded(new_library);
	}
}
