package com.examples.esame_attsw_Bellocci.hibernate.util;

import java.io.File;
import java.util.Properties;
import java.util.function.IntPredicate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.examples.esame_attsw_Bellocci.model.Book;
import com.examples.esame_attsw_Bellocci.model.Library;

public class HibernateUtil {
	
	private static SessionFactory sessionFactory;
	private static Properties settings;
	private static String configuration_path = "src/main/resources/";

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
	                   		.configure(new File(configuration_path +"hibernate.cfg.xml"))
	                   		.buildSessionFactory();
				}
			} catch(Exception e) {
    			e.printStackTrace();
			}
		}
		return sessionFactory;
	}

	public static void setProperties(Properties properties) {
		settings = properties;
	}
	
	public static void resetSessionFactory() {
		if(sessionFactory != null)
			sessionFactory.close();
		sessionFactory = null;
	}
	
	protected static void setSessionFactory(SessionFactory setSessionFactory) {
		sessionFactory = setSessionFactory;
	}

	protected static void setPathConfigurationFile(String path) {
		configuration_path = path;
	}

	protected static SessionFactory getValueSessionFactory() {
		return sessionFactory;
	}
}
