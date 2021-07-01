package com.examples.esameattswbellocci.repository.mysql;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.examples.esameattswbellocci.hibernate.util.HibernateUtil;
import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.LibraryRepository;

public class LibraryMySQLRepository implements LibraryRepository {
	
	private static final Logger LOGGER = LogManager.getLogger(LibraryMySQLRepository.class);
	
	private Session session;
	private Transaction transaction;
	
	public LibraryMySQLRepository() {}
	
	protected void setSession(Session session) {
		this.session = session;
	}
	
	protected Session getSession() {
		return this.session;
	}
	
	protected void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	@Override
	public List<Library> getAllLibraries() {
		List<Library> libraries = new ArrayList<>();
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			libraries = session.createQuery("FROM Library", Library.class).list();
		} catch(HibernateException e) {
			throw new IllegalStateException("Cannot open the session");
		} finally {
			if(session != null && session.isOpen())
				session.close();
		}
		return libraries;
	}

	@Override
	public Library findLibraryById(String idLibrary) {
		Library library = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			library = session.get(Library.class, idLibrary);
		} catch(HibernateException e) {
			throw new IllegalStateException("Cannot open the session");
		} finally {
			if(session != null && session.isOpen())
				session.close();
		}
		return library;
	}	

	@Override
	public void saveLibrary(Library library) {
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			session.save(library);
			transaction.commit();
		} catch(HibernateException e) {
			throw new IllegalStateException("Cannot open the session");
		} catch(IllegalStateException e) {
			throw new IllegalStateException("Transaction isn't active");
		} catch(RollbackException e) {
			transaction.rollback();
			throw new IllegalStateException("Commit fails. Transaction rollback");
		} catch(PersistenceException e) {
			throw new IllegalArgumentException("Database already contains library with id " + library.getId());
		} finally {
			if(session != null && session.isOpen())
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
		} catch(HibernateException e) {
			throw new IllegalStateException("Cannot open the session");
		} catch(IllegalStateException e) {
			throw new IllegalStateException("Transaction commit fails because it isn't active");
		} catch(RollbackException e) {
			transaction.rollback();
			throw new IllegalStateException("Transaction commit fails. Transaction rollback");
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Database doesn't contain library with id " + idLibrary);
		} finally {
			if(session != null && session.isOpen())
				session.close();
		}
	}

}
