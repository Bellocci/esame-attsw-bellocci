package com.examples.esameattswbellocci.repository.mysql;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.RollbackException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.examples.esameattswbellocci.hibernate.util.HibernateUtil;
import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;

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
		HibernateUtil.setProperties(settings);
		bookRepository = new BookMySQLRepository();
		cleanDatabaseTables();
	}

	@After
	public void resetSessionFactory(){
		HibernateUtil.resetSessionFactory();
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
		} catch(HibernateException e) {
			e.printStackTrace();
		} catch(IllegalStateException e) {
			e.printStackTrace();
		} catch(RollbackException e) {
			transaction.rollback();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}

	@Test
	public void testGetAllBooksOfLibraryWhenListIsEmptyShouldReturnAnEmptyListAndCloseTheSession() {
		// setup
		addLibraryToDatabase("1", "library1");
		
		// exercise & verify
		assertThat(bookRepository.getAllBooksOfLibrary("1")).isEmpty();
		assertThat(bookRepository.getSession()).isNotNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	private void addLibraryToDatabase(String id, String name) {
		Library library = new Library(id, name);
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        session.save(library);
	        transaction.commit();
		} catch(HibernateException e) {
			e.printStackTrace();
		} catch(IllegalStateException e) {
			e.printStackTrace();
		} catch(RollbackException e) {
			transaction.rollback();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}
	
	@Test
	public void testGetAllBooksOfLibraryWhenListIsNotEmptyShouldReturnListOfBooksAndCloseTheSession() {
		// setup
		addLibraryToDatabase("1", "library1");
		addLibraryToDatabase("2", "library2");
		
		addBookOfLibraryToDatabase("1", "book1", "1", "library1");
		addBookOfLibraryToDatabase("2", "book2", "1", "library1");
		addBookOfLibraryToDatabase("3", "book3", "2", "library2");
		
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
		
		assertThat(bookRepository.getSession()).isNotNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	private void addBookOfLibraryToDatabase(String id, String name, String idLibrary, String nameLibrary) {
		Book book = new Book(id, name);
		book.setLibrary(new Library(idLibrary, nameLibrary));
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        session.save(book);
	        transaction.commit();
		} catch(HibernateException e) {
			e.printStackTrace();
		} catch(IllegalStateException e) {
			e.printStackTrace();
		} catch(RollbackException e) {
			transaction.rollback();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}
	
	@Test
	public void testGetAllBooksOfLibraryWhenOpenSessionThrowHibernateExceptionShouldThrowAndVerifySessionIsNull() {
		// setup
		addLibraryToDatabase("1", "library1");
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory().openSession())
                   //.thenReturn(sessionFactory)
                   .thenThrow(new HibernateException("Cannot connect to session"));
            
            // exercise & verify
            assertThatThrownBy(() -> bookRepository.getAllBooksOfLibrary("1"))
            	.isInstanceOf(IllegalStateException.class)
            	.hasMessage("Cannot connect to session");
            
            assertThat(bookRepository.getSession()).isNull();
        }
	}
	
	@Test
	public void testFindBookByIdWhenDatabaseContainsTheBookShouldReturnItAndCloseTheSession() {
		// setup
		addLibraryToDatabase("1", "library1");
		addBookOfLibraryToDatabase("1", "book1", "1", "library1");
		addBookOfLibraryToDatabase("2", "book2", "1", "library1");
		
		// exercise
		Book bookFound = bookRepository.findBookById("2");
		
		// verify
		assertThat(bookFound.getId()).isEqualTo("2");
		assertThat(bookFound.getName()).isEqualTo("book2");
		assertThat(bookRepository.getSession()).isNotNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testFindBookByIdWhenDatabaseDoesntContainTheBookShouldReturnNullAndCloseTheSession() {
		// setup
		addLibraryToDatabase("1", "library1");
		
		// exercise & verify
		assertThat(bookRepository.findBookById("1")).isNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testFindBookByIdWhenSessionFactoryThrowHibernateExceptionShouldThrowAndVerifySessionIsNull() {
		// setup
		addLibraryToDatabase("1", "library1");
		addBookOfLibraryToDatabase("1", "book1", "1", "library1");
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
			hibernateMock.when(() -> HibernateUtil.getSessionFactory().openSession())
            	.thenThrow(new HibernateException("Cannot open session"));
			
			// exercise & verify
			assertThatThrownBy(() -> bookRepository.findBookById("1"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Cannot open session");
			
			assertThat(bookRepository.getSession()).isNull();
		}	
	}
	
	@Test
	public void testSaveBookInTheLibraryWhenDatabaseDoesntContainNewBookShouldAddItToDatabaseAndCloseTheSession() {
		// setup
		addLibraryToDatabase("1", "library1");
		addBookOfLibraryToDatabase("1", "book1", "1", "library1");
		
		// exercise
		bookRepository.saveBookInTheLibrary(new Library("1", "library1"), new Book("2", "new_book"));
		
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
	public void testSaveBookInTheLibraryWhenSessionFactoryThrowHibernateExceptionShouldThrowAndVerifySessionIsNull() {
		// setup
		addLibraryToDatabase("1", "library1");
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
			hibernateMock.when(() -> HibernateUtil.getSessionFactory().openSession())
            	.thenThrow(new HibernateException("Cannot open session"));
			
			// exercise & verify
			assertThatThrownBy(() -> bookRepository.saveBookInTheLibrary(
						new Library("1", "library1"),
						new Book("1", "book1"))
					)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Cannot open session");
			
			assertThat(bookRepository.getSession()).isNull();
		}
		
	}
	
	@Test
	public void testSaveBookInTheLibraryWhenDatabaseAlreadyContainsNewBookShouldNotAddBookAndThrowAndCloseSession() {
		// setup
		addLibraryToDatabase("1", "library1");
		addBookOfLibraryToDatabase("1", "book1", "1", "library1");
		
		// exercise
		assertThatThrownBy(() -> bookRepository.saveBookInTheLibrary(
					new Library("1", "library1"),
					new Book("1", "existing_book"))
				)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Database already contains the book");
		
		// verify
		
		List<Book> books = getAllBooksFromDatabase();
		assertThat(books)
			.hasSize(1)
			.noneMatch(e -> e.getName().equals("existing_book"));
		
		assertThat(bookRepository.getSession().isConnected()).isFalse();
	}
	
	@Test
	public void testSaveBookInTheLibraryWhenTransactionIsNullShouldThrowAndCloseTheSession() {
		// setup
		addLibraryToDatabase("1", "library1");
		
		Transaction transaction = mock(Transaction.class);
		Session session = mock(Session.class);
		SessionFactory sessionFactory = mock(SessionFactory.class);
		
		bookRepository.setTransaction(transaction);
		bookRepository.setSession(session);
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
			hibernateMock.when(() -> HibernateUtil.getSessionFactory()).thenReturn(sessionFactory);
			
			when(sessionFactory.openSession()).thenReturn(session);
			when(session.beginTransaction()).thenReturn(transaction);
			doThrow(new IllegalStateException("Transaction isn't active")).when(transaction).commit();
			
			// exercise & verify
			assertThatThrownBy(() -> bookRepository.saveBookInTheLibrary(
						new Library("1", "library1"), 
						new Book("1", "book1"))
					)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Transaction isn't active");
			
			assertThat(bookRepository.getSession().isConnected()).isFalse();
		}
		
	}
	
	@Test
	public void testSaveBookInTheLibraryWhenTransactionCommitFailShouldThrowAndCloseTheSession() {
		// setup
		addLibraryToDatabase("1", "library1");
		
		Transaction transaction = mock(Transaction.class);
		Session session = mock(Session.class);
		SessionFactory sessionFactory = mock(SessionFactory.class);
		
		bookRepository.setTransaction(transaction);
		bookRepository.setSession(session);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
			hibernateMock.when(() -> HibernateUtil.getSessionFactory())
				.thenReturn(sessionFactory);
			
			when(sessionFactory.openSession()).thenReturn(session);
			when(session.beginTransaction()).thenReturn(transaction);
			doThrow(new RollbackException("Error commit. Transaction rollback"))
				.when(transaction).commit();
			
			// exercise & verify
			assertThatThrownBy(() -> bookRepository.saveBookInTheLibrary(
						new Library("1", "library1"), 
						new Book("1", "book1"))
					)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Error commit. Transaction rollback");
			
			assertThat(bookRepository.getSession().isConnected()).isFalse();
		}
	}
	
	@Test
	public void testDeleteBookFromLibraryWhenDatabaseContainsBookInTheLibraryShouldRemoveIt() {
		// setup
		addLibraryToDatabase("1", "library1");
		addBookOfLibraryToDatabase("1", "book1", "1", "library1");
		addBookOfLibraryToDatabase("2", "book2", "1", "library1");
		
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
