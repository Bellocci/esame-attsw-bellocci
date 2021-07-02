package com.examples.esameattswbellocci.repository.mysql;

import java.util.List;

import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.examples.esameattswbellocci.hibernate.util.HibernateUtil;
import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.LibraryRepository;

public class LibraryMySQLRepository implements LibraryRepository {
	
	private Session session;
	private Transaction transaction;
	
	public LibraryMySQLRepository() {}
	
	protected Session getSession() {
		return this.session;
	}

	@Override
	public List<Library> takeAllLibraries() {
		session = HibernateUtil.getSessionFactory().openSession();
		List<Library> libraries = session.createQuery("FROM Library", Library.class).list();
		session.close();
		return libraries;
	}

	@Override
	public Library findLibraryById(String idLibrary) {
		session = HibernateUtil.getSessionFactory().openSession();
		Library library = session.get(Library.class, idLibrary);
		session.close();
		return library;
	}	

	@Override
	public void saveLibrary(Library library) {
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			session.save(library);
			transaction.commit();
		} catch(PersistenceException e) {
			throw new IllegalArgumentException("Database already contains library with id " + library.getId());
		} finally {
			session.close();  
		}
	}

	@Override
	public void deleteLibrary(String idLibrary) {
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			Library libraryFound = session.get(Library.class, idLibrary);
			session.delete(libraryFound);
			transaction.commit();
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Database doesn't contain library with id " + idLibrary);
		} finally {
			session.close();
		}
	}

}
