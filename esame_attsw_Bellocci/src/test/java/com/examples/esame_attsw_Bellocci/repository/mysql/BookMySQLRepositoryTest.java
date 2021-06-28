package com.examples.esame_attsw_Bellocci.repository.mysql;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
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

import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;

public class BookMySQLRepositoryTest {
	
	private BookMySQLRepository bookRepository;
	
	private static Properties settings;

	@BeforeClass
	public static void setupHibernateWithH2() {		
		settings = new Properties();
		
		settings.put(AvailableSettings.DRIVER, "org.h2.Driver");
		settings.put(AvailableSettings.URL, "jdbc:h2:mem:test");
		settings.put(AvailableSettings.USER, "user");
		settings.put(AvailableSettings.PASS, "password");
		settings.put(AvailableSettings.POOL_SIZE, "1");
		settings.put(AvailableSettings.DIALECT, "org.hibernate.dialect.H2Dialect");
		settings.put(AvailableSettings.SHOW_SQL, "true");
		settings.put(AvailableSettings.FORMAT_SQL, "true");
		settings.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
	}
	
	@AfterClass
	public static void clearHibernateUtil() {
		HibernateUtil.resetSessionFactory();
	}
	
	@Before
	public void setup() {
		bookRepository = new BookMySQLRepository(settings);
	}
	
	@After
	public void cleanTables() {
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
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}
	
	@Test
	public void testConstructorWhenSettingsNotNullSetPropertiesOfHibernateUtilWithTheir() {
		// setup
		Properties settings = new Properties();
		
		// exercise
		bookRepository = new BookMySQLRepository(settings);
		
		// verify
		assertThat(HibernateUtil.getProperties()).isNotNull();
		assertThat(HibernateUtil.getProperties()).isEqualTo(settings);
	}
	
	@Test
	public void testConstructorWhenSettingsIsNullSetPropertiesOfHibernateUtilNull() {
		// exercise
		bookRepository = new BookMySQLRepository(null);
		
		// verify
		assertThat(HibernateUtil.getProperties()).isNull();
	}

	@Test
	public void testGetAllBooksOfLibraryWhenListIsEmptyShouldReturnAnEmptyList() {
		// exercise & verify
		assertThat(bookRepository.getAllBooksOfLibrary("1")).isEmpty();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testGetAllBooksOfLibraryWhenListIsNotEmptyShouldReturnListOfBooks() {
		// setup
		Library library1 = new Library("1", "library1");
		Library library2 = new Library("2", "library2");
		addLibraryToDatabase(library1);
		addLibraryToDatabase(library2);
		
		Book book1 = new Book("1", "book1");
		Book book2 = new Book("2", "book2");
		Book book3 = new Book("3", "book3");
		addBookOfLibraryToDatabase(book1, library1);
		addBookOfLibraryToDatabase(book2, library1);
		addBookOfLibraryToDatabase(book3, library2);
		
		// exercise
		List<Book> books = bookRepository.getAllBooksOfLibrary("1");
		
		// verify
		assertThat(books)
			.hasSize(2)
			.anyMatch(e -> e.getId().equals("1"))
			.anyMatch(e -> e.getId().equals("2"))
			.anyMatch(e -> e.getLibrary().getId().equals("1"))
			.noneMatch(e -> e.getId().equals("3"))
			.noneMatch(e -> e.getLibrary().getId().equals("2"));
		
		assertThat(bookRepository.getSession().isOpen()).isFalse();
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
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}
	
	private void addBookOfLibraryToDatabase(Book book, Library library) {
		book.setLibrary(library);
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        session.save(book);
	        transaction.commit();
		} catch(Exception e) {
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}
	
	@Test
	public void testFindBookByIdWhenDatabaseContainsTheBookShouldReturnIt() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		
		Book book1 = new Book("1", "book1");
		Book book2 = new Book("2", "book2");
		addBookOfLibraryToDatabase(book1, library);
		addBookOfLibraryToDatabase(book2, library);
		
		// exercise
		Book bookFound = bookRepository.findBookById("2");
		
		// verify
		assertThat(bookFound.getId()).isEqualTo("2");
		assertThat(bookFound.getName()).isEqualTo("book2");
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testFindBookByIdWhenDatabaseDoesntContainTheBookShouldReturnNull() {
		// exercise & verify
		assertThat(bookRepository.findBookById("1")).isNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testSaveBookInTheLibraryWhenDatabaseDoesntContainNewBookShouldAddItToDatabase() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		Book book = new Book("1", "book1");
		addBookOfLibraryToDatabase(book, library);
		Book newBook = new Book("2", "new_book");
		
		// exercise
		bookRepository.saveBookInTheLibrary(library, newBook);
		
		// verify
		List<Book> books = getAllBooksFromDatabase();
		assertThat(books)
			.hasSize(2)
			.anyMatch(e -> e.getId().equals("2"));
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	private List<Book> getAllBooksFromDatabase() {
		List<Book> books = new ArrayList<>();
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			books = session.createQuery("FROM Book", Book.class).list();	
			transaction.commit();
		} catch(Exception e) {
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
		return books;
	}
	
	@Test
	public void testDeleteBookFromLibraryWhenDatabaseContainsBookInTheLibraryShouldRemoveIt() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		Book book1 = new Book("1", "book1");
		Book bookDeleted = new Book("2", "book2");
		addBookOfLibraryToDatabase(book1, library);
		addBookOfLibraryToDatabase(bookDeleted, library);
		
		assertThat(getAllBooksFromDatabase()).hasSize(2);
		
		// exercise
		bookRepository.deleteBookFromLibrary("1", "2");
		
		// verify
		List<Book> books = getAllBooksFromDatabase();
		assertThat(books)
			.hasSize(1)
			.noneMatch(e -> e.getId().equals("2"));
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}

}
