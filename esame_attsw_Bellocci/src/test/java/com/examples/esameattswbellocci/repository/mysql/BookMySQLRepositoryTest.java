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
		
		HibernateUtil.setProperties(settings);
	}
	
	@AfterClass
	public static void closeSessionFactory(){
		HibernateUtil.closeSessionFactory();
	}
	
	@Before
	public void setup() {
		bookRepository = new BookMySQLRepository();
		cleanDatabaseTables();
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
	public void testTakeAllBooksOfLibraryWhenListIsEmptyShouldReturnAnEmptyListAndCloseTheSession() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		
		// exercise & verify
		assertThat(bookRepository.takeAllBooksOfLibrary("1")).isEmpty();
		assertThat(bookRepository.getSession()).isNotNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	private void addLibraryToDatabase(Library library) {
		Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.save(library);
        transaction.commit();
		session.close();
	}
	
	@Test
	public void testTakeAllBooksOfLibraryWhenListIsNotEmptyShouldReturnListOfBooksAndCloseTheSession() {
		// setup
		Library library1 = new Library("1", "library1");
		addLibraryToDatabase(library1);
		Library library2 = new Library("2", "library2");
		addLibraryToDatabase(library2);
		
		Book book1 = new Book("1", "book1");
		addBookOfLibraryToDatabase(book1, library1);
		Book book2 = new Book("2", "book2");
		addBookOfLibraryToDatabase(book2, library1);
		Book book3 = new Book("3", "book3");
		addBookOfLibraryToDatabase(book3, library2);
		
		// exercise
		List<Book> books = bookRepository.takeAllBooksOfLibrary("1");
		
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
	
	private void addBookOfLibraryToDatabase(Book book, Library library) {
		book.setLibrary(library);
		Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.save(book);
        transaction.commit();
		session.close();
	}
	
	@Test
	public void testFindBookByIdWhenDatabaseContainsTheBookShouldReturnItAndCloseTheSession() {
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
		assertThat(bookRepository.getSession()).isNotNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testFindBookByIdWhenDatabaseDoesntContainTheBookShouldReturnNullAndCloseTheSession() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		
		// exercise & verify
		assertThat(bookRepository.findBookById("1")).isNull();
		assertThat(bookRepository.getSession()).isNotNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testSaveBookInTheLibraryWhenDatabaseDoesntContainNewBookShouldAddItToDatabaseAndCloseTheSession() {
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
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<Book> books = session.createQuery("FROM Book", Book.class).list();	
		session.close();
		return books;
	}
	
	@Test
	public void testSaveBookInTheLibraryWhenDatabaseAlreadyContainsNewBookShouldThrowAndCloseSession() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		Book book = new Book("1", "existing_book");
		addBookOfLibraryToDatabase(book, library);
		Book newBook = new Book("1", "new_book");
		
		// exercise
		assertThatThrownBy(() -> bookRepository.saveBookInTheLibrary(library, newBook))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Database already contains the book with id 1");
		
		// verify
		
		List<Book> books = getAllBooksFromDatabase();
		assertThat(books)
			.hasSize(1)
			.noneMatch(e -> e.getName().equals("new_book"));
		
		assertThat(bookRepository.getSession()).isNotNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testDeleteBookFromLibraryWhenDatabaseContainsBookInTheLibraryShouldRemoveIt() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		Book book = new Book("1", "book1");
		addBookOfLibraryToDatabase(book, library);
		Book bookToRemove = new Book("2", "to_remove");
		addBookOfLibraryToDatabase(bookToRemove, library);
		
		assertThat(getAllBooksFromDatabase()).hasSize(2);
		
		// exercise
		bookRepository.deleteBookFromLibrary("2");
		
		// verify
		List<Book> books = getAllBooksFromDatabase();
		assertThat(books)
			.hasSize(1)
			.noneMatch(e -> e.getId().equals("2") && e.getName().equals("to_remove"));
		assertThat(bookRepository.getSession()).isNotNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testDeleteBookFromLibraryWhenDatabaseDoesntContainsBookShouldThrow() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		Book book = new Book("1", "book1");
		addBookOfLibraryToDatabase(book, library);
		
		// exercise & verify
		assertThatThrownBy(() -> bookRepository.deleteBookFromLibrary("2"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Database doesn't contain book with id 2");
		
		// verify
		assertThat(getAllBooksFromDatabase())
			.hasSize(1)
			.anyMatch(e -> e.getId().equals("1"))
			.anyMatch(e -> e.getName().equals("book1"));
		
		assertThat(bookRepository.getSession()).isNotNull();
		assertThat(bookRepository.getSession().isOpen()).isFalse();
	}
}
