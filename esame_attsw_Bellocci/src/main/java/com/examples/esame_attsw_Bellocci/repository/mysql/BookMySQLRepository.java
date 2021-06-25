package com.examples.esame_attsw_Bellocci.repository.mysql;

import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.BookRepository;

public class BookMySQLRepository implements BookRepository {
	
	private Session session;
	private Transaction transaction;

	public BookMySQLRepository(Properties settings) {
		HibernateUtil.setProperties(settings);
	}
	
	protected Session getSession() {
		return this.session;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Book> getAllBooksOfLibrary(String id_library) {
		session = null;
		transaction = null;
		session = HibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();
        String hql = "FROM Book WHERE id_library = :library";
        Query query = session.createQuery(hql);
        query.setParameter("library", id_library);
        List<Book> books = query.getResultList();
        transaction.commit();
        session.close();
		return books;
	}

	@Override
	public Book findBookById(String id_book) {
		session = null;
		transaction = null;
		session = HibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();
        Book book = session.get(Book.class, id_book);
        transaction.commit();
        session.close();
		return book;
	}

	@Override
	public void saveBookInTheLibrary(Library library, Book new_book) {
		session = null;
		transaction = null;
		session = HibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();
        new_book.setLibrary(library);
        session.save(new_book);
        transaction.commit();            
    	session.close();
	}

	@Override
	public void deleteBookFromLibrary(String id_library, String id_book) {
		Book book_found = findBookOfLibraryById(id_library, id_book);
		session = null;
		transaction = null;
		session = HibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();
        session.delete(book_found);
        transaction.commit();
        session.close();
	}

	@SuppressWarnings("rawtypes")
	private Book findBookOfLibraryById(String id_library, String id_book) {
		session = null;
		transaction = null;
		session = HibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();
        String hql = "FROM Book WHERE id = :idBook AND id_library = :idLibrary";
        Query query = session.createQuery(hql);
        query.setParameter("idBook", id_book);
        query.setParameter("idLibrary", id_library);
        Book book = (Book) query.uniqueResult();
        transaction.commit();
    	session.close();   
		return book;
	}
}
