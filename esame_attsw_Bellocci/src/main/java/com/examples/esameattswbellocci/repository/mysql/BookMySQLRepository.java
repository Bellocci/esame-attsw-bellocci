package com.examples.esameattswbellocci.repository.mysql;

import java.util.List;

import javax.persistence.PersistenceException;

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

	protected Session getSession() {
		return this.session;
	}

	@Override
	public List<Book> takeAllBooksOfLibrary(String idLibrary) {
		session = HibernateUtil.getSessionFactory().openSession();
		String hql = "FROM Book WHERE id_library = :library";
		Query<Book> query = session.createQuery(hql, Book.class);
		query.setParameter("library", idLibrary);
		List<Book> books = query.getResultList();
		session.close();
		return books;
	}

	@Override
	public Book findBookById(String idBook) {
		session = HibernateUtil.getSessionFactory().openSession();
		Book book = session.get(Book.class, idBook);
		session.close();
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
		} catch(PersistenceException e) {
			throw new IllegalArgumentException("Database already contains the book with id " + newBook.getId());
		} finally {
			session.close();
		}
	}

	@Override
	public void deleteBookFromLibrary(String idBook, String idLibrary) {
		Book bookFound = findBookById(idBook);
		if(bookFound == null)
			throw new IllegalArgumentException("Database doesn't contain book with id " + idBook);
		if(!bookFound.getLibrary().getId().equals(idLibrary))
			throw new IllegalArgumentException("Library with id " + idLibrary + 
					" doesn't contain book with id " + idBook);
		session = HibernateUtil.getSessionFactory().openSession();
		transaction = session.beginTransaction();
		session.delete(bookFound);
		transaction.commit();
		session.close();
	}

}
