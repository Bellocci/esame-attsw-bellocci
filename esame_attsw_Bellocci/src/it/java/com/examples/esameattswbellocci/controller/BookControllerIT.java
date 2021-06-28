package com.examples.esameattswbellocci.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import com.examples.esameattswbellocci.repository.BookRepository;
import com.examples.esameattswbellocci.repository.LibraryRepository;
import com.examples.esameattswbellocci.repository.mysql.BookMySQLRepository;
import com.examples.esameattswbellocci.view.BookView;

public class BookControllerIT {

	@Mock
	private BookView bookView;
	
	@Mock
	private LibraryController libraryController;
	
	@Mock
	private LibraryRepository libraryRepository;
	
	private BookRepository bookRepository;
	
	private BookController bookController;
	
	private AutoCloseable closeable;
	
	private static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8");
	
	private static Properties settings;
	
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
	}
	
	@AfterClass
	public static void shutdownServerAndHibernate() {
		HibernateUtil.resetSessionFactory();
		mySQLContainer.stop();
	}
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		bookRepository = new BookMySQLRepository(settings);
		bookController = new BookController(bookRepository, bookView, libraryController);
	}
	
	@After
	public void releaseMocksAndCleanTables() throws Exception {
		closeable.close();
		cleanDatabaseTables();
	}
	
	private void cleanDatabaseTables() {
		Transaction transaction = null;
		Session session = null;
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
	
	@Test
	public void testGetAllBooks() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		Book book = new Book("1", "book1");
		book.setLibrary(library);
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		bookRepository.saveBookInTheLibrary(library, book);
		
		// exercise
		bookController.allBooks(library);
		
		// verify
		verify(bookView).showAllBooks(asList(book));
	}
	
	private void addLibraryToDatabase(Library library) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        session.save(library);
	        transaction.commit();
		} catch(Exception e) {
			if(transaction != null & transaction.isActive())
				transaction.rollback();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}

	@Test
	public void testNewBook() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		Book newBook = new Book("1", "book1");
		newBook.setLibrary(library);
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		
		// exercise
		bookController.newBook(library, newBook);
		
		// verify
		verify(bookView).bookAdded(newBook);
	}
	
	@Test
	public void testDeleteBook() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		Book bookDeleted = new Book("1", "book1");
		bookDeleted.setLibrary(library);
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		bookRepository.saveBookInTheLibrary(library, bookDeleted);
		
		// exercise
		bookController.deleteBook(library, bookDeleted);
		
		// verify
		verify(bookView).bookRemoved(bookDeleted);
	}
}
