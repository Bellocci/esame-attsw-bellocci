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

		HibernateUtil.setProperties(settings);
	}
	
	@AfterClass
	public static void clearHibernateUtil() {
		HibernateUtil.resetSessionFactory();
	}

	@Before
	public void setupDatabase() {
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
	public void testTakeAllLibrariesWhenListIsEmptyShouldReturnAnEmptyListAndCloseTheSession() {
		// verify
		assertThat(libraryRepository.takeAllLibraries()).isEmpty();
		assertThat(libraryRepository.getSession()).isNotNull();
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testTakeAllLibrariesWhenListIsNotEmptyShouldReturnAListWithAllLibrariesAndCloseTheSession() {
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
	public void testFindLibraryByIdWhenLibraryIsContainedInTheDatabaseShouldReturnIt() {
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
	public void testFindLibraryByIdWhenLibraryDidntContainInTheDatabaseShouldReturnNull() {
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
	public void testSaveLibraryWhenDatabaseAlreadyContainsLibraryWithSameIdShouldThrowAndCloseTheSession() {
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
		List<Book> listBooks = session.createQuery("FROM Book", Book.class).list();
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
