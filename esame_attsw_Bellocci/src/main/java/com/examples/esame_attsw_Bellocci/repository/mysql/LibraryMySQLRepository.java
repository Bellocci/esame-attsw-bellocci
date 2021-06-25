package com.examples.esame_attsw_Bellocci.repository.mysql;

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
	
	public LibraryMySQLRepository(Properties settings) {
		HibernateUtil.setProperties(settings);
	}
	
	protected Session getSession() {
		return this.session;
	}

	@Override
	public List<Library> getAllLibraries() {
		session = null;
		transaction = null;
		session = HibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();
        List<Library> libraries = session.createQuery("FROM Library", Library.class).list();
        session.close();
		return libraries;
	}

	@Override
	public Library findLibraryById(String id_library) {
		transaction = null;
		session = null;
		session = HibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();
        Library library = session.get(Library.class, id_library);
        transaction.commit();
    	session.close();
		return library;
	}	

	@Override
	public void saveLibrary(Library library) {
		session = null;
		transaction = null;
		session = HibernateUtil.getSessionFactory().openSession();
		transaction = session.beginTransaction();
		session.save(library);
		transaction.commit();
    	session.close();  
	}

	@Override
	public void deleteLibrary(String id_library) {
		session = null;
		transaction = null;
		Library library_found = findLibraryById(id_library);
		session = HibernateUtil.getSessionFactory().openSession();
		transaction = session.beginTransaction();
		session.delete(library_found);
		transaction.commit();
        session.close();
	}

}
