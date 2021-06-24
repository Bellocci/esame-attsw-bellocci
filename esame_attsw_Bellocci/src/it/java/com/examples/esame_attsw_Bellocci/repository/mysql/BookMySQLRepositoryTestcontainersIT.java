package com.examples.esame_attsw_Bellocci.repository.mysql;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
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
import com.examples.esame_attsw_Bellocci.repository.BookRepository;

public class BookMySQLRepositoryTestcontainersIT {

	@SuppressWarnings("rawtypes")
	private static MySQLContainer mySQLContainer;
	
	private BookRepository bookRepository;
	
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
		
		settings.put(Environment.DRIVER, mySQLContainer.getDriverClassName());
		settings.put(Environment.URL, mySQLContainer.getJdbcUrl());
		settings.put(Environment.USER, mySQLContainer.getUsername());
		settings.put(Environment.PASS, mySQLContainer.getPassword());
		settings.put(Environment.POOL_SIZE, "1");
		settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
		settings.put(Environment.SHOW_SQL, "true");
		settings.put(Environment.FORMAT_SQL, "true");
		settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(Environment.HBM2DDL_AUTO, "create-drop");
		settings.put(Environment.HBM2DDL_HALT_ON_ERROR, "true");
		settings.put(Environment.HBM2DDL_CREATE_SCHEMAS, "true");
	}
	
	@AfterClass
	public static void shutdownServerAndHibernate() {
		HibernateUtil.resetSessionFactory();
		mySQLContainer.stop();
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
			if(session != null)
				session.close();
		}
	}
	
	@Test
	public void testGetAllBooksOfLibrary() {
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
		assertThat(books).hasSize(2);
		assertThat(books)
			.anyMatch(e -> e.getId().equals("1"))
			.anyMatch(e -> e.getId().equals("2"));
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
	
	private void addBookOfLibraryToDatabase(Book book, Library library) {
		book.setLibrary(library);
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        book.setLibrary(library);
	        session.save(book);
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
	public void testFindBookById() {
		// setup
		Library library = new Library("1", "library1");
		addLibraryToDatabase(library);
		
		Book book1 = new Book("1", "book1");
		Book book2 = new Book("2", "book2");
		addBookOfLibraryToDatabase(book1, library);
		addBookOfLibraryToDatabase(book2, library);
		
		// exercise
		Book book_found = bookRepository.findBookById("2");
		
		// verify
		assertThat(book_found.getId()).isEqualTo("2");
		assertThat(book_found.getName()).isEqualTo("book2");
	}
	
	
}
