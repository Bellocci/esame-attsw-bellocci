package com.examples.esameattswbellocci.repository.mysql;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.RollbackException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.examples.esameattswbellocci.hibernate.util.HibernateUtil;
import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.BookRepository;

public class BookMySQLRepository implements BookRepository {
	
	private static final Logger LOGGER = LogManager.getLogger(BookMySQLRepository.class);
	
	private Session session;
	private Transaction transaction;

	public BookMySQLRepository(Properties settings) {
		HibernateUtil.setProperties(settings);
	}
	
	public BookMySQLRepository() {}
	
	protected void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	
	protected void setSession(Session session) {
		this.session = session;
	}

	protected Session getSession() {
		return this.session;
	}

	@Override
	public List<Book> getAllBooksOfLibrary(String idLibrary) {
		List<Book> books = new ArrayList<>();
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			String hql = "FROM Book WHERE id_library = :library";
			Query<Book> query = session.createQuery(hql, Book.class);
			query.setParameter("library", idLibrary);
			books = query.getResultList();
		} catch(HibernateException e) {
			throw new IllegalStateException(e.getMessage());
		} finally {
			if(session != null && session.isConnected() != false)
				session.close();
		}
		return books;
	}

	@Override
	public Book findBookById(String idBook) {
		Book book = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			book = session.get(Book.class, idBook);
		} catch(HibernateException e) {
			throw new IllegalStateException(e.getMessage());
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
		return book;
	}

	@Override
	public void saveBookInTheLibrary(Library library, Book newBook) {
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			newBook.setLibrary(library);
			session.save(newBook);
			transaction.commit();
		} catch(HibernateException e) {
			throw new IllegalStateException(e.getMessage());
		} catch(IllegalStateException e) {
			throw new IllegalStateException(e.getMessage());
		} catch(RollbackException e) {
			transaction.rollback();
			throw new IllegalStateException(e.getMessage());
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}

	@Override
	public void deleteBookFromLibrary(String idLibrary, String idBook) {
		Book bookFound = findBookOfLibraryById(idLibrary, idBook);
		session = null;
		transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        session.delete(bookFound);
	        transaction.commit();
		} catch(Exception e) {
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			LOGGER.error(e.getMessage(), e);
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}


	private Book findBookOfLibraryById(String idLibrary, String idBook) {
		Book book = null;
		session = null;
		transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        String hql = "FROM Book WHERE id = :idBook AND id_library = :idLibrary";
	        Query<Book> query = session.createQuery(hql, Book.class);
	        query.setParameter("idBook", idBook);
	        query.setParameter("idLibrary", idLibrary);
	        book = query.uniqueResult();
	        transaction.commit();
		} catch(Exception e) {
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			LOGGER.error(e.getMessage(), e);
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
		return book;
	}

}
