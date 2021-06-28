package com.examples.esameAttswBellocci.hibernate.util;

import java.io.File;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.examples.esameAttswBellocci.model.Book;
import com.examples.esameAttswBellocci.model.Library;

public class HibernateUtil {
	
	private static SessionFactory sessionFactory;
	private static Properties settings;
	private static String configurationPath = "src/main/resources/";
	
	private HibernateUtil() { }

	public static SessionFactory getSessionFactory() {
		if(sessionFactory == null) {
			try {
				if(settings != null) {
					Configuration configuration = new Configuration();
                	
                	configuration.setProperties(settings);
                	
                	configuration.addAnnotatedClass(Library.class);
                    configuration.addAnnotatedClass(Book.class);
                    
                    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                            .applySettings(configuration.getProperties()).build();

                    sessionFactory = configuration.buildSessionFactory(serviceRegistry);
				}
				else {
	    			sessionFactory = new Configuration()
	                   		.configure(new File(configurationPath +"hibernate.cfg.xml"))
	                   		.buildSessionFactory();
				}
			} catch(Exception e) {
    			e.printStackTrace();
			}
		}
		return sessionFactory;
	}
	
	public static void resetSessionFactory() {
		if(sessionFactory != null) {
			try {
				sessionFactory.close();
			} catch(HibernateException e) {
				e.printStackTrace();
			} finally {
				if(sessionFactory.isClosed())
					sessionFactory = null;
			}
		}
	}
	
	public static void setProperties(Properties properties) {
		settings = properties;
	}
	
	public static Properties getProperties() {
		return settings;
	}
	
	protected static void setSessionFactory(SessionFactory setSessionFactory) {
		sessionFactory = setSessionFactory;
	}

	protected static void setPathConfigurationFile(String path) {
		configurationPath = path;
	}

	protected static SessionFactory getValueSessionFactory() {
		return sessionFactory;
	}
}
