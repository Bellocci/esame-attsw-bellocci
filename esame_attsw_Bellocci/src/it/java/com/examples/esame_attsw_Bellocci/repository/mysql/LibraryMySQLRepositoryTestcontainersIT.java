package com.examples.esame_attsw_Bellocci.repository.mysql;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
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
	public void testGetAllLibraries() {
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
	
	@Test
	public void testFoundLibraryById() {
		// setup
		Library library = new Library("1", "library1");
		Library library2 = new Library("2", "library2");
		addLibrariesToDatabase(library);
		addLibrariesToDatabase(library2);
		
		// exercise
		Library library_found = libraryRepository.findLibraryById("2");
		
		// verify
		assertThat(library_found.getId()).isEqualTo("2");
		assertThat(library_found.getName()).isEqualTo("library2");
	}
	
	@Test
	public void testSaveLibrary() {
		// setup
		Library library = new Library("1", "library");
		addLibrariesToDatabase(library);
		Library new_library = new Library("2", "new_library");
		
		// exercise
		libraryRepository.saveLibrary(new_library);
		
		// verify
		Library library_found = searchLibraryInTheDatabase(new_library);
		assertThat(library_found.getId()).isEqualTo("2");
		assertThat(library_found.getName()).isEqualTo("new_library");
	}
	
	private Library searchLibraryInTheDatabase(Library library) {
		Library library_found = null;
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			// start a transaction
	        transaction = session.beginTransaction();
	        // save the student objects
	        library_found = session.get(Library.class, library.getId());
	        transaction.commit();
		} catch(Exception e) {
			if(transaction != null)
				transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
        return library_found;
	}
	
	@Test
	public void testDeleteLibrary() {
		// setup
		Library library1 = new Library("1", "library");
		Library library2 = new Library("2", "library2");
		addLibrariesToDatabase(library1);
		addLibrariesToDatabase(library2);
		addBookOfLibraryToDatabase(library1, "1", "book1");
		addBookOfLibraryToDatabase(library2, "2", "book2");
		
		// exercise
		libraryRepository.deleteLibrary("1");
		
		// verify
		Library library_found = searchLibraryInTheDatabase(library1);
		assertThat(library_found).isNull();
		List<Book> books = getAllBooksFromDatabase();
		assertThat(books).hasSize(1);
		assertThat(books).noneMatch(e -> e.getId().equals("1"));
	}
	
	private List<Book> getAllBooksFromDatabase() {
		List<Book> books = null;
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			// start a transaction
	        transaction = session.beginTransaction();
	        // save the student objects
	        books = session.createQuery("FROM Book", Book.class).list();
	        //transaction.commit();
		} catch(Exception e) {
			if(transaction != null)
				transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
        return books;
	}
	
	private void addBookOfLibraryToDatabase(Library library, String id_book, String name_book) {
		Book book = new Book(id_book, name_book);
		book.setLibrary(library);
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			// start a transaction
	        transaction = session.beginTransaction();
	        // save the student objects
	        session.save(book);
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
