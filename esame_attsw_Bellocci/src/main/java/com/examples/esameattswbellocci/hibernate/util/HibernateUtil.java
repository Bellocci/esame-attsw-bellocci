package com.examples.esameattswbellocci.hibernate.util;

import java.io.File;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.examples.esameattswbellocci.model.Book;
import com.examples.esameattswbellocci.model.Library;

public class HibernateUtil {
	
	private static final Logger LOGGER = LogManager.getLogger(HibernateUtil.class);
	
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
				LOGGER.error(e.getMessage(), e);
			}
		}
		return sessionFactory;
	}
	
	public static void resetSessionFactory() {
		if(sessionFactory != null) {
			try {
				sessionFactory.close();
			} catch(HibernateException e) {
				LOGGER.error(e.getMessage(), e);
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
