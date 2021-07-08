package com.examples.esameattswbellocci.repository.mysql;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;

import com.examples.esameattswbellocci.hibernate.util.HibernateUtil;
import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.LibraryRepository;

public class LibraryMySQLRepositoryTestcontainersIT {

	private static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8");
	
	private LibraryRepository libraryRepository;
	
	private static Properties settings;
	
	@BeforeClass
	public static void setupServerAndHibernateWithMySQL() {
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
		
		HibernateUtil.setProperties(settings);
	}
	
	@AfterClass
	public static void shutdownServerAndCloseSessionFactory() {
		HibernateUtil.closeSessionFactory();
		mySQLContainer.stop();
	}
	
	@Before
	public void setup() {
		cleanDatabaseTables();
		libraryRepository = new LibraryMySQLRepository();
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
	public void testTakeAllLibraries() {
		// setup
		Library library1 = new Library("1", "library1");
		Library library2 = new Library("2", "library2");
		addLibrariesToDatabase(library1);
		addLibrariesToDatabase(library2);
		
		// exercise
		List<Library> libraries = libraryRepository.takeAllLibraries();
		
		// verify
		assertThat(libraries)
			.anyMatch(e -> e.getId().equals("1"))
			.anyMatch(e -> e.getId().equals("2"));
	}
	
	private void addLibrariesToDatabase(Library library) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.save(library);
		transaction.commit();
		session.close();
	}
	
	@Test
	public void testFindLibraryById() {
		// setup
		Library library = new Library("1", "library1");
		Library library2 = new Library("2", "library2");
		addLibrariesToDatabase(library);
		addLibrariesToDatabase(library2);
		
		// exercise
		Library libraryFound = libraryRepository.findLibraryById("2");
		
		// verify
		assertThat(libraryFound.getId()).isEqualTo("2");
		assertThat(libraryFound.getName()).isEqualTo("library2");
	}
	
	@Test
	public void testSaveLibrary() {
		// setup
		Library library = new Library("1", "library");
		addLibrariesToDatabase(library);
		Library newLibrary = new Library("2", "new_library");
		
		// exercise
		libraryRepository.saveLibrary(newLibrary);
		
		// verify
		Library libraryFound = searchLibraryInTheDatabase(newLibrary);
		assertThat(libraryFound.getId()).isEqualTo("2");
		assertThat(libraryFound.getName()).isEqualTo("new_library");
	}
	
	private Library searchLibraryInTheDatabase(Library library) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Library libraryFound = session.get(Library.class, library.getId());
		session.close();
        return libraryFound;
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
		Library libraryFound = searchLibraryInTheDatabase(library1);
		assertThat(libraryFound).isNull();
		List<Book> books = getAllBooksFromDatabase();
		assertThat(books)
			.hasSize(1)
			.noneMatch(e -> e.getId().equals("1"));
	}
	
	private List<Book> getAllBooksFromDatabase() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<Book> books = session.createQuery("FROM Book", Book.class).list();
		session.close();
        return books;
	}
	
	private void addBookOfLibraryToDatabase(Library library, String idBook, String nameBook) {
		Book book = new Book(idBook, nameBook);
		book.setLibrary(library);
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.save(book);
		transaction.commit();
		session.close();
	}
}
