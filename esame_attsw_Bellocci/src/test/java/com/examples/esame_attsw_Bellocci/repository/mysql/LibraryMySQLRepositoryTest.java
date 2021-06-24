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

import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.LibraryRepository;

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
}
