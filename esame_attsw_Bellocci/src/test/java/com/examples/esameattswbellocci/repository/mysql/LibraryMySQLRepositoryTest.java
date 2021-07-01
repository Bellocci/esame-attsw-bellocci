package com.examples.esameattswbellocci.repository.mysql;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import javax.persistence.RollbackException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.examples.esameattswbellocci.hibernate.util.HibernateUtil;
import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;

public class LibraryMySQLRepositoryTest {
	
	private LibraryMySQLRepository libraryRepository;
	
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
	public void setupDatabase() {
		HibernateUtil.setProperties(settings);
		libraryRepository = new LibraryMySQLRepository();
		cleanDatabaseTables();
	}
	
	private void cleanDatabaseTables() {
		Transaction transaction = null;
		Session session = null;
		session = HibernateUtil.getSessionFactory().openSession();
	    transaction = session.beginTransaction();
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
	public void testGetAllLibrariesWhenListIsEmptyShouldReturnAnEmptyListAndCloseTheSession() {
		// verify
		assertThat(libraryRepository.getAllLibraries()).isEmpty();
		assertThat(libraryRepository.getSession()).isNotNull();
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testGetAllLibrariesWhenListIsNotEmptyShouldReturnAListWithAllLibrariesAndCloseTheSession() {
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
		
		assertThat(libraryRepository.getSession()).isNotNull();
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	private void addLibrariesToDatabase(Library library) {
		Session session = HibernateUtil.getSessionFactory().openSession();
	    Transaction transaction = session.beginTransaction();
	    session.save(library);
	    transaction.commit();
		session.close();
	}
	
	@Test
	public void testGetAllLibrariesWhenSessionIsNullAndOpenSessionThrowShouldThrowAndSessionRemainsNull() {
		// setup
		Library library1 = new Library("1", "library1");
		addLibrariesToDatabase(library1);
		libraryRepository.setSession(null);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory().openSession())
                   .thenThrow(new HibernateException("Error to open session"));
            
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.getAllLibraries())
            	.isInstanceOf(IllegalStateException.class)
            	.hasMessage("Cannot open the session");
            
            assertThat(libraryRepository.getSession()).isNull();
		}
	}
	
	@Test
	public void testGetAllLibrariesWhenSessionIsClosedAndOpenSessionThrowShouldThrowAndNotClosedSessionAgain() {
		// setup
		Library library1 = new Library("1", "library1");
		addLibrariesToDatabase(library1);
		Session session = mock(Session.class);
		libraryRepository.setSession(session);
		when(session.isOpen()).thenReturn(false);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory().openSession())
                   .thenThrow(new HibernateException("Error to open session"));
            
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.getAllLibraries())
            	.isInstanceOf(IllegalStateException.class)
            	.hasMessage("Cannot open the session");
            
            
            verify(session).isOpen();
            verify(session, never()).close();
            verifyNoMoreInteractions(session);
		}
	}

	
	@Test
	public void testFoundLibraryByIdWhenLibraryIsContainedInTheDatabaseShouldReturnIt() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		
		// exercise
		Library libraryFound = libraryRepository.findLibraryById("1");
		
		// verify
		assertThat(libraryFound.getId()).isEqualTo("1");
		assertThat(libraryFound.getName()).isEqualTo("library1");
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testFoundLibraryByIdWhenLibraryDidntContainInTheDatabaseShouldReturnNull() {
		// exercise
		Library libraryFound = libraryRepository.findLibraryById("1");
		
		// verify
		assertThat(libraryFound).isNull();
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testFoundLibraryByIdWhenSessionIsNullAndOpenSessionThrowShouldThrowAndSessionRemainsNull() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		libraryRepository.setSession(null);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory().openSession())
                   .thenThrow(new HibernateException("Error to open session"));
         
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.findLibraryById("1"))
            	.isInstanceOf(IllegalStateException.class)
            	.hasMessage("Cannot open the session");
            
            assertThat(libraryRepository.getSession()).isNull();
            
		}
	}
	
	@Test
	public void testFoundLibraryByIdWhenSessionIsClosedAndOpenSessionThrowShouldThrowAndDontCloseSessionAgain() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		Session session = mock(Session.class);
		libraryRepository.setSession(session);
		when(session.isOpen()).thenReturn(false);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory().openSession())
                   .thenThrow(new HibernateException("Error to open session"));
         
         // exercise & verify
            assertThatThrownBy(() -> libraryRepository.findLibraryById("1"))
            	.isInstanceOf(IllegalStateException.class)
            	.hasMessage("Cannot open the session");
            
            verify(session).isOpen();
            verify(session, never()).close();
            verifyNoMoreInteractions(session);
		}
	}
	
	@Test
	public void testSaveLibraryWhenDatabaseDoesntContainNewLibraryShouldAddToDatabase() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		Library newLibrary = new Library("2", "new_library");
		
		// exercise
		libraryRepository.saveLibrary(newLibrary);
		
		// verify
		List<Library> listLibraries = getAllLibrariesFromDatabase();
		assertThat(listLibraries)
			.hasSize(2)
			.anyMatch(e -> e.getId().equals("2") && e.getName().equals("new_library"));
		
		assertThat(libraryRepository.getSession()).isNotNull();
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	private List<Library> getAllLibrariesFromDatabase() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<Library> libraries = session.createQuery("FROM Library", Library.class).list();
		session.close();
		return libraries;
	}
	
	@Test
	public void testSaveLibraryWhenSessionIsNullAndOpenSessionThrowShouldThrowAndSessionRemainsNull() {
		// setup
		Library library = new Library("1", "library1");
		libraryRepository.setSession(null);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory().openSession())
            	.thenThrow(new HibernateException("Error to open session"));
            
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.saveLibrary(library))
            	.isInstanceOf(IllegalStateException.class)
            	.hasMessage("Cannot open the session");
            
            assertThat(libraryRepository.getSession()).isNull();
		}
	}
	
	@Test
	public void testSaveLibraryWhenSessionIsClosedAndOpenSessionThrowShouldThrowAndDontCloseSessionAgain() {
		// setup
		Library library = new Library("1", "library1");
		Session session = mock(Session.class);
		libraryRepository.setSession(session);
		when(session.isOpen()).thenReturn(false);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory().openSession())
            	.thenThrow(new HibernateException("Error to open session"));
         
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.saveLibrary(library))
        		.isInstanceOf(IllegalStateException.class)
        		.hasMessage("Cannot open the session");
            
            verify(session).isOpen();
            verify(session, never()).close();
            verifyNoMoreInteractions(session);
		}	
	}
	
	@Test
	public void testSaveLibraryWhenTransactionCommitFailureBecuseDoesntActiveShouldThrowAndCloseTheSession() {
		// setup
		Library library = new Library("1", "library1");
		
		SessionFactory sessionFactory = mock(SessionFactory.class);
		Session session = mock(Session.class);
		Transaction transaction = mock(Transaction.class);
		
		libraryRepository.setSession(session);
		libraryRepository.setTransaction(transaction);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory()).thenReturn(sessionFactory);
            
            when(sessionFactory.openSession()).thenReturn(session);
            when(session.beginTransaction()).thenReturn(transaction);
            doThrow(new IllegalStateException("Transaction error")).when(transaction).commit();
            when(session.isOpen()).thenReturn(true);
            
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.saveLibrary(library))
    			.isInstanceOf(IllegalStateException.class)
    			.hasMessage("Transaction isn't active");
            
            InOrder inOrder = Mockito.inOrder(session);
            inOrder.verify(session).isOpen();
            inOrder.verify(session).close();
            inOrder.verifyNoMoreInteractions();
		}
	}
	
	@Test
	public void testSaveLibraryWhenTransactionCommitFailsShouldThrowRollbackTransactionAndCloseTheSession() {
		// setup
		Library library = new Library("1", "library1");
		
		SessionFactory sessionFactory = mock(SessionFactory.class);
		Session session = mock(Session.class);
		Transaction transaction = mock(Transaction.class);
		
		libraryRepository.setSession(session);
		libraryRepository.setTransaction(transaction);
		
		libraryRepository.setSession(session);
		libraryRepository.setTransaction(transaction);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory()).thenReturn(sessionFactory);
            
            when(sessionFactory.openSession()).thenReturn(session);
            when(session.beginTransaction()).thenReturn(transaction);
            doThrow(new RollbackException("Transaction error")).when(transaction).commit();
            when(session.isOpen()).thenReturn(true);
            
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.saveLibrary(library))
    			.isInstanceOf(IllegalStateException.class)
    			.hasMessage("Commit fails. Transaction rollback");
            
            InOrder inOrder = Mockito.inOrder(transaction, session);
            inOrder.verify(transaction).rollback();
            inOrder.verify(session).isOpen();
            inOrder.verify(session).close();
            inOrder.verifyNoMoreInteractions();
		}
	}
	
	@Test
	public void testSaveLibraryWhenDatabaseAlreadyContainsLibraryWithSameIdShouldThrowNotAddTheLibraryAndCloseTheSession() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		Library library2 = new Library("1", "existing_library");
		
		// exercise & verify
		assertThatThrownBy(() -> libraryRepository.saveLibrary(library2))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Database already contains library with id 1");
		
		assertThat(getAllLibrariesFromDatabase())
			.hasSize(1)
			.noneMatch(e -> e.getId().equals("1") && e.getName().equals("existing_library"));
		
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	
	@Test
	public void testDeleteLibraryWhenDatabaseContainLibraryShouldRemoveItFromDatabase() {
		// setup
		Library library1 = new Library("1", "library");
		Library library2 = new Library("2", "library2");
		addLibrariesToDatabase(library1);
		addLibrariesToDatabase(library2);
		
		// exercise
		libraryRepository.deleteLibrary("2");
		
		// verify
		List<Library> listLibraries = getAllLibrariesFromDatabase();
		assertThat(listLibraries)
			.hasSize(1)
			.noneMatch(e -> e.getId().equals("2") && e.getName().equals("library2"));
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testDeleteLibraryWhenDatabaseContainLibraryWithBooksShouldRemoveItAndAllItsBooksFromDatabase() {
		// setup
		Library library1 = new Library("1", "library1");
		Library library2 = new Library("2", "library2");
		addLibrariesToDatabase(library1);
		addLibrariesToDatabase(library2);
		addBookOfLibraryToDatabase(library1, "1", "book1");
		addBookOfLibraryToDatabase(library2, "2", "book2");
		
		// exercise
		libraryRepository.deleteLibrary("1");
		
		// verify
		assertThat(getAllLibrariesFromDatabase())
			.hasSize(1)
			.noneMatch(e -> e.getId().equals("1") && e.getName().equals("library1"));
		
		assertThat(getAllBooksFromDatabase())
			.hasSize(1)
			.noneMatch(e -> e.getId().equals("1") && e.getName().equals("book1"));
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	private List<Book> getAllBooksFromDatabase() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		List<Book> listBooks = session.createQuery("FROM Book", Book.class).list();
		transaction.commit();
		session.close();
        return listBooks;
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
	
	@Test
	public void testDeleteLibraryWhenOpenSessionFailsAndSessionIsNullShouldThrowAndSessionRemainsNull() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		SessionFactory sessionFactory = mock(SessionFactory.class);
		libraryRepository.setSession(null);
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory()).thenReturn(sessionFactory);
            
            when(sessionFactory.openSession()).thenThrow(new HibernateException("Error openSession method"));
            
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.deleteLibrary("1"))
            	.isInstanceOf(IllegalStateException.class)
            	.hasMessage("Cannot open the session");
            
            assertThat(libraryRepository.getSession()).isNull();
		}
		
        assertThat(getAllLibrariesFromDatabase())
    		.hasSize(1)
    		.anyMatch(e -> e.getId().equals("1") && e.getName().equals("library1"));
	}
	
	@Test
	public void testDeleteLibraryWhenOpenSessionFailsAndSessionIsClosedShouldThrowAndDontCloseSessionAgain() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		SessionFactory sessionFactory = mock(SessionFactory.class);
		Session session = mock(Session.class);
		libraryRepository.setSession(session);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory()).thenReturn(sessionFactory);
            
            when(session.isOpen()).thenReturn(false);
            when(sessionFactory.openSession()).thenThrow(new HibernateException("Error openSession method"));
            
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.deleteLibrary("1"))
            	.isInstanceOf(IllegalStateException.class)
            	.hasMessage("Cannot open the session");
            
            verify(session).isOpen();
            verify(session, never()).close();
            verifyNoMoreInteractions(session);
		}
		
		assertThat(getAllLibrariesFromDatabase())
			.hasSize(1)
			.anyMatch(e -> e.getId().equals("1") && e.getName().equals("library1"));
	}
	
	@Test
	public void testDeleteLibraryWhenTransitionCommitFailsBecauseItIsNotActiveShouldThrowAndCloseTheSession() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		SessionFactory sessionFactory = mock(SessionFactory.class);
		Session session = mock(Session.class);
		Transaction transaction = mock(Transaction.class);
		
		libraryRepository.setSession(session);
		libraryRepository.setTransaction(transaction);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory()).thenReturn(sessionFactory);
            
            when(sessionFactory.openSession()).thenReturn(session);
            when(session.beginTransaction()).thenReturn(transaction);
            doThrow(new IllegalStateException("Error transaction")).when(transaction).commit();
            when(session.isOpen()).thenReturn(true);
            
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.deleteLibrary("1"))
            	.isInstanceOf(IllegalStateException.class)
            	.hasMessage("Transaction commit fails because it isn't active");
            
            verify(session).isOpen();
            verify(session).close();
		}
		
		assertThat(getAllLibrariesFromDatabase())
			.hasSize(1)
			.anyMatch(e -> e.getId().equals("1") && e.getName().equals("library1"));
	}
	
	@Test
	public void testDeleteLibraryWhenTransactionCommitFailsShouldThrowRollbackAndCloseTheSession() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		SessionFactory sessionFactory = mock(SessionFactory.class);
		Session session = mock(Session.class);
		Transaction transaction = mock(Transaction.class);
		
		libraryRepository.setSession(session);
		libraryRepository.setTransaction(transaction);
		
		try (MockedStatic<HibernateUtil> hibernateMock = Mockito.mockStatic(HibernateUtil.class)) {
            hibernateMock.when(() -> HibernateUtil.getSessionFactory()).thenReturn(sessionFactory);
            
            when(sessionFactory.openSession()).thenReturn(session);
            when(session.beginTransaction()).thenReturn(transaction);
            doThrow(new RollbackException("Error transaction")).when(transaction).commit();
            when(session.isOpen()).thenReturn(true);
            
            // exercise & verify
            assertThatThrownBy(() -> libraryRepository.deleteLibrary("1"))
            	.isInstanceOf(IllegalStateException.class)
            	.hasMessage("Transaction commit fails. Transaction rollback");
            
            InOrder inOrder = Mockito.inOrder(transaction, session);
            inOrder.verify(transaction).rollback();
            inOrder.verify(session).isOpen();
            inOrder.verify(session).close();
            inOrder.verifyNoMoreInteractions();
		}
		
		assertThat(getAllLibrariesFromDatabase())
			.hasSize(1)
			.anyMatch(e -> e.getId().equals("1") && e.getName().equals("library1"));
	}
	
	@Test
	public void testDeleteLibraryWhenDatabaseDoesntContainLibraryShouldThrowAndCloseSession() {
		// setup
		Library library = new Library("1","library1");
		addLibrariesToDatabase(library);
		
		// exercise & verify
		assertThatThrownBy(() -> libraryRepository.deleteLibrary("2"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Database doesn't contain library with id 2");
		
		assertThat(libraryRepository.getSession()).isNotNull();
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
		
		assertThat(getAllLibrariesFromDatabase())
			.hasSize(1)
			.anyMatch(e -> e.getId().equals("1") && e.getName().equals("library1"));
	}
}
