package com.examples.esameattswbellocci.repository.mysql;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.examples.esameattswbellocci.hibernate.util.HibernateUtil;
import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;
import com.examples.esameattswbellocci.repository.BookRepository;

public class BookMySQLRepository implements BookRepository {
	
	private Session session;
	private Transaction transaction;
	
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
			if(session != null && session.isConnected())
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
			throw new IllegalStateException("Cannot open the Session");
		} catch(IllegalStateException e) {
			throw new IllegalStateException("Transaction isn't active");
		} catch(RollbackException e) {
			transaction.rollback();
			throw new IllegalStateException("Error during commit. Transaction rollback");
		} catch(PersistenceException e) {
			throw new IllegalArgumentException("Database already contains the book");
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}

	@Override
	public void deleteBookFromLibrary(Book bookToRemove) {
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			session.delete(bookToRemove);
			transaction.commit();
		} catch(HibernateException e) {
			throw new IllegalStateException("Cannot open the Session");
		} catch(IllegalStateException e) {
			throw new IllegalStateException("Transaction isn't active");
		} catch(RollbackException e) {
			transaction.rollback();
			throw new IllegalStateException("Error during commit. Transaction rollback");
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
	}

}
