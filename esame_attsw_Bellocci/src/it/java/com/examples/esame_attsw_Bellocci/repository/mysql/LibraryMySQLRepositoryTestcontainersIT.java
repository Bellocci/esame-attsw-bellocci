package com.examples.esame_attsw_Bellocci.repository.mysql;

import static org.assertj.core.api.Assertions.*;

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
import org.testcontainers.containers.MySQLContainer;

import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.LibraryRepository;

public class LibraryMySQLRepositoryTestcontainersIT {

	@SuppressWarnings("rawtypes")
	private static MySQLContainer mySQLContainer;
	
	private LibraryRepository libraryRepository;
	
	private static Properties settings;
	
	@SuppressWarnings({ "rawtypes", "resource" })
	@BeforeClass
	public static void setupServerAndHibernate() {
		mySQLContainer = new MySQLContainer("mysql:8")
				.withDatabaseName("test")
				.withUsername("user")
				.withPassword("password");
		mySQLContainer.start();
		
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
	}
	
	@AfterClass
	public static void shutdownServerAndHibernate() {
		HibernateUtil.resetSessionFactory();
		mySQLContainer.stop();
	}
	
	@Before
	public void setup() {
		libraryRepository = new LibraryMySQLRepository(settings);
	}
	
	@After
	public void cleanTables() {
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
			if(transaction != null)
				transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	@Test
	public void testGetAllLibrariesWhenListIsNotEmptyShouldReturnAListWithAllLibraries() {
		// setup
		Library library1 = new Library("1", "library1");
		Library library2 = new Library("2", "library2");
		addLibrariesToDatabase(library1);
		addLibrariesToDatabase(library2);
		
		// exercise
		List<Library> libraries = libraryRepository.getAllLibraries();
		
		// verify
		assertThat(libraries)
			.anyMatch(e -> e.getId().equals("1"))
			.anyMatch(e -> e.getId().equals("2"));
	}
	
	private void addLibrariesToDatabase(Library library) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			// start a transaction
	        transaction = session.beginTransaction();
	        // save the student objects
	        session.save(library);
	        transaction.commit();
		} catch(Exception e) {
			if(transaction != null)
				transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
}
