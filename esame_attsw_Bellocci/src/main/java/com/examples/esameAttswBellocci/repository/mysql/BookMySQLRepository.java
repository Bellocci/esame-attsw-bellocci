package com.examples.esameAttswBellocci.repository.mysql;

import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.examples.esameAttswBellocci.hibernate.util.HibernateUtil;
import com.examples.esameAttswBellocci.model.Book;
import com.examples.esameAttswBellocci.model.Library;
import com.examples.esameAttswBellocci.repository.BookRepository;

public class BookMySQLRepository implements BookRepository {
	
	private Session session;
	private Transaction transaction;

	public BookMySQLRepository(Properties settings) {
		HibernateUtil.setProperties(settings);
	}
	
	protected Session getSession() {
		return this.session;
	}

	@Override
	public List<Book> getAllBooksOfLibrary(String idLibrary) {
		List<Book> books = null;
		session = null;
		transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        String hql = "FROM Book WHERE id_library = :library";
	        Query<Book> query = session.createQuery(hql, Book.class);
	        query.setParameter("library", idLibrary);
	        books = query.getResultList();
	        transaction.commit();
		} catch(Exception e) {
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
		return books;
	}

	@Override
	public Book findBookById(String idBook) {
		Book book = null;
		session = null;
		transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        book = session.get(Book.class, idBook);
	        transaction.commit();
		} catch(Exception e) {
			if(transaction != null && transaction.isActive())
				transaction.rollback();
			e.printStackTrace();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
		return book;
	}

	@Override
	public void saveBookInTheLibrary(Library library, Book newBook) {
		session = null;
		transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
	        transaction = session.beginTransaction();
	        newBook.setLibrary(library);
	        session.save(newBook);
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
			e.printStackTrace();
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
			e.printStackTrace();
		} finally {
			if(session != null && session.isConnected())
				session.close();
		}
		return book;
	}
}