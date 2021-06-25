package com.examples.esame_attsw_Bellocci.repository.mysql;

import java.util.ArrayList;
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
	
	public BookMySQLRepository() { }

	public BookMySQLRepository(Properties settings) {
		HibernateUtil.setProperties(settings);
	}
	
	protected Session getSession() {
		return this.session;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Book> getAllBooksOfLibrary(String id_library) {
		List<Book> books = new ArrayList<Book>();
		session = null;
		transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            String hql = "FROM Book WHERE id_library = :library";
            Query query = session.createQuery(hql);
            query.setParameter("library", id_library);
            books = query.getResultList();
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
        
		return books;
	}

	@Override
	public Book findBookById(String id_book) {
		Book book = new Book();
		session = null;
		transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            book = session.get(Book.class, id_book);
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
		return book;
	}

	@Override
	public void saveBookInTheLibrary(Library library, Book new_book) {
		session = null;
		transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            new_book.setLibrary(library);
            session.save(new_book);
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

	@Override
	public void deleteBookFromLibrary(String id_library, String id_book) {
		Book book_found = findBookOfLibraryById(id_library, id_book);
		session = null;
		transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.delete(book_found);
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

	@SuppressWarnings("rawtypes")
	private Book findBookOfLibraryById(String id_library, String id_book) {
		Book book = new Book();
		session = null;
		transaction = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            String hql = "FROM Book WHERE id = :idBook AND id_library = :idLibrary";
            Query query = session.createQuery(hql);
            query.setParameter("idBook", id_book);
            query.setParameter("idLibrary", id_library);
            book = (Book) query.uniqueResult();
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
        
		return book;
	}
}
