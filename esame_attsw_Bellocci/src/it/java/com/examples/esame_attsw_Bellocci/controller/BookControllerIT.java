package com.examples.esame_attsw_Bellocci.controller;

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

import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.BookRepository;
import com.examples.esame_attsw_Bellocci.repository.LibraryRepository;
import com.examples.esame_attsw_Bellocci.repository.mysql.BookMySQLRepository;
import com.examples.esame_attsw_Bellocci.view.BookView;

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
			if(transaction != null)
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null)
				session.close();
		}
	}

	@Test
	public void testNewBook() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		Book new_book = new Book("1", "book1");
		new_book.setLibrary(library);
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		
		// exercise
		bookController.newBook(library, new_book);
		
		// verify
		verify(bookView).bookAdded(new_book);
	}
	
	@Test
	public void testDeleteBook() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		Book book_to_delete = new Book("1", "book1");
		book_to_delete.setLibrary(library);
		when(libraryController.getLibraryRepository()).thenReturn(libraryRepository);
		when(libraryRepository.findLibraryById(library.getId())).thenReturn(library);
		bookRepository.saveBookInTheLibrary(library, book_to_delete);
		
		// exercise
		bookController.deleteBook(library, book_to_delete);
		
		// verify
		verify(bookView).bookRemoved(book_to_delete);
	}
}
