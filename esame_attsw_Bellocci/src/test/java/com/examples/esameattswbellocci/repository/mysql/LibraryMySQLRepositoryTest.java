package com.examples.esameattswbellocci.repository.mysql;

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
		libraryRepository = new LibraryMySQLRepository(settings);
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
	public void testGetAllLibrariesWhenListIsEmptyShouldReturnAnEmptyList() {
		// verify
		assertThat(libraryRepository.getAllLibraries()).isEmpty();
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
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
		
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	private void addLibrariesToDatabase(Library library) {
		Transaction transaction = null;
		Session session = null;
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
	public void testSaveLibraryWhenDatabaseDoesntContainNewLibraryShouldAddToDatabase() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		List<Library> listLibraries = getAllLibrariesFromDatabase();
		assertThat(listLibraries).hasSize(1);
		Library newLibrary = new Library("2", "new_library");
		
		// exercise
		libraryRepository.saveLibrary(newLibrary);
		
		// verify
		listLibraries = getAllLibrariesFromDatabase();
		assertThat(listLibraries).hasSize(2);
		Library libraryFound = searchLibraryInTheDatabase(newLibrary);
		assertThat(libraryFound.getId()).isEqualTo("2");
		assertThat(libraryFound.getName()).isEqualTo("new_library");
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	private Library searchLibraryInTheDatabase(Library library) {
		Library libraryFound = null;
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			libraryFound = session.get(Library.class, library.getId());
			transaction.commit();
		} catch(Exception e) {
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
        return libraryFound;
	}
	
	private List<Library> getAllLibrariesFromDatabase() {
		List<Library> libraries = new ArrayList<>();
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			libraries = session.createQuery("FROM Library", Library.class).list();
			transaction.commit();
		} catch(Exception e) {
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
        return libraries;
	}
	
	@Test
	public void testDeleteLibraryWhenDatabaseContainLibraryWithoutBooksShouldRemoveOnlyLibraryFromDatabase() {
		// setup
		Library library1 = new Library("1", "library");
		Library library2 = new Library("2", "library2");
		addLibrariesToDatabase(library1);
		addLibrariesToDatabase(library2);
		List<Library> listLibraries = getAllLibrariesFromDatabase();
		assertThat(listLibraries).hasSize(2);
		
		// exercise
		libraryRepository.deleteLibrary("2");
		
		// verify
		Library libraryFound = searchLibraryInTheDatabase(library2);
		assertThat(libraryFound).isNull();
		listLibraries = getAllLibrariesFromDatabase();
		assertThat(listLibraries)
			.hasSize(1)
			.noneMatch(e -> e.getId().equals("2"));
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testDeleteLibraryWhenDatabaseContainLibraryWithBooksShouldRemoveItAndAllItsBooksFromDatabase() {
		// setup
		Library library1 = new Library("1", "library");
		Library library2 = new Library("2", "library2");
		addLibrariesToDatabase(library1);
		addLibrariesToDatabase(library2);
		List<Library> listLibraries = getAllLibrariesFromDatabase();
		assertThat(listLibraries).hasSize(2);
		addBookOfLibraryToDatabase(library1, "1", "book1");
		addBookOfLibraryToDatabase(library2, "2", "book2");
		List<Book> listBooks = getAllBooksFromDatabase();
		assertThat(listBooks).hasSize(2);
		
		// exercise
		libraryRepository.deleteLibrary("1");
		
		// verify
		Library libraryFound = searchLibraryInTheDatabase(library1);
		assertThat(libraryFound).isNull();
		listBooks = getAllBooksFromDatabase();
		assertThat(listBooks)
			.hasSize(1)
			.noneMatch(e -> e.getId().equals("1"));
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	private List<Book> getAllBooksFromDatabase() {
		List<Book> listBooks = new ArrayList<>();
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			listBooks = session.createQuery("FROM Book", Book.class).list();
			transaction.commit();
		} catch(Exception e) {
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
        return listBooks;
	}
	
	private void addBookOfLibraryToDatabase(Library library, String idBook, String nameBook) {
		Session session = null;
		Transaction transaction = null;
		Book book = new Book(idBook, nameBook);
		book.setLibrary(library);
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
}
