package com.examples.esame_attsw_Bellocci.repository.mysql;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
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

import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;

public class LibraryMySQLRepositoryTest {
	
	private LibraryMySQLRepository libraryRepository;
	
	private static Properties settings;
	
	@BeforeClass
	public static void setupHibernateWithH2() {
		settings = new Properties();
		
		settings.put(Environment.DRIVER, "org.h2.Driver");
		settings.put(Environment.URL, "jdbc:h2:mem:test");
		settings.put(Environment.USER, "user");
		settings.put(Environment.PASS, "password");
		settings.put(Environment.POOL_SIZE, "1");
		settings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
		settings.put(Environment.SHOW_SQL, "true");
		settings.put(Environment.FORMAT_SQL, "true");
		settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(Environment.HBM2DDL_AUTO, "create-drop");

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
		} catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	if(session != null)
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
			.anyMatch(e -> e.getName().equals("library1"));
		
		assertThat(libraries)
			.anyMatch(e -> e.getId().equals("2"))
			.anyMatch(e -> e.getName().equals("library2"));
		
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
		} catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	if(session != null)
        		session.close();
        }
	}
	
	@Test
	public void testFoundLibraryByIdWhenLibraryIsContainedInTheDatabaseShouldReturnIt() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		
		// exercise
		Library library_found = libraryRepository.findLibraryById("1");
		
		// verify
		assertThat(library_found.getId()).isEqualTo("1");
		assertThat(library_found.getName()).isEqualTo("library1");
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testFoundLibraryByIdWhenLibraryDidntContainInTheDatabaseShouldReturnNull() {
		// exercise
		Library library_found = libraryRepository.findLibraryById("1");
		
		// verify
		assertThat(library_found).isNull();
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	@Test
	public void testSaveLibraryWhenDatabaseDoesntContainNewLibraryShouldAddToDatabase() {
		// setup
		Library library = new Library("1", "library1");
		addLibrariesToDatabase(library);
		List<Library> listLibraries = getAllLibrariesFromDatabase();
		assertThat(listLibraries).hasSize(1);
		Library new_library = new Library("2", "new_library");
		
		// exercise
		libraryRepository.saveLibrary(new_library);
		
		// verify
		listLibraries = getAllLibrariesFromDatabase();
		assertThat(listLibraries).hasSize(2);
		Library library_found = searchLibraryInTheDatabase(new_library);
		assertThat(library_found.getId()).isEqualTo("2");
		assertThat(library_found.getName()).isEqualTo("new_library");
		assertThat(libraryRepository.getSession().isOpen()).isFalse();
	}
	
	private Library searchLibraryInTheDatabase(Library library) {
		Library library_found = null;
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        library_found = session.get(Library.class, library.getId());
	        transaction.commit();
		} catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	if(session != null)
        		session.close();
        }
        return library_found;
	}
	
	private List<Library> getAllLibrariesFromDatabase() {
		List<Library> libraries = new ArrayList<>();
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        libraries = session.createQuery("FROM Library", Library.class).list();
		} catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	if(session != null)
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
		Library library_found = searchLibraryInTheDatabase(library2);
		assertThat(library_found).isNull();
		listLibraries = getAllLibrariesFromDatabase();
		assertThat(listLibraries).hasSize(1);
		assertThat(listLibraries).noneMatch(e -> e.getId().equals("2"));
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
		Library library_found = searchLibraryInTheDatabase(library1);
		assertThat(library_found).isNull();
		listBooks = getAllBooksFromDatabase();
		assertThat(listBooks).hasSize(1);
		assertThat(listBooks).noneMatch(e -> e.getId().equals("1"));
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
		} catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	if(session != null)
        		session.close();
        }
        return listBooks;
	}
	
	private void addBookOfLibraryToDatabase(Library library, String id_book, String name_book) {
		Session session = null;
		Transaction transaction = null;
		try {
			Book book = new Book(id_book, name_book);
			book.setLibrary(library);
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        session.save(book);
	        transaction.commit();
		} catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	if(session != null)
        		session.close();
        }
	}
}
