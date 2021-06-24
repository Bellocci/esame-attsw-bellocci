package com.examples.esame_attsw_Bellocci.repository.mysql;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.examples.esame_attsw_Bellocci.hibernate.util.HibernateUtil;
import com.examples.esame_attsw_Bellocci.model.Library;
import com.examples.esame_attsw_Bellocci.repository.LibraryRepository;

public class LibraryMySQLRepository implements LibraryRepository {
	
	private Session session;
	private Transaction transaction;

	public LibraryMySQLRepository() { }
	
	public LibraryMySQLRepository(Properties settings) {
		HibernateUtil.setProperties(settings);
	}
	
	protected Session getSession() {
		return this.session;
	}

	@Override
	public List<Library> getAllLibraries() {
		List<Library> libraries = new ArrayList<Library>();
		session = null;
		transaction = null;
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

	@Override
	public Library findLibraryById(String id_library) {
		Library library = new Library();
		transaction = null;
		session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            library = session.get(Library.class, id_library);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	if(session != null)
        		session.close();
        }
		return library;
	}
	
	

	@Override
	public void saveLibrary(Library library) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deleteLibrary(String id_library) {
		// TODO Auto-generated method stub
	}

}
