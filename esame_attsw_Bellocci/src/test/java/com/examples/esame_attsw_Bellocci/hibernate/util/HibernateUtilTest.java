package com.examples.esame_attsw_Bellocci.hibernate.util;

import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HibernateUtilTest {

	private StandardServiceRegistry registry;
	private static Properties settings;

	@BeforeClass
	public static void setupSettingsHibernate() {
		settings = new Properties();
		
		settings.put(Environment.DRIVER, "org.h2.Driver");
		settings.put(Environment.URL, "jdbc:h2:mem:test");
		settings.put(Environment.USER, "user");
		settings.put(Environment.PASS, "password");
		settings.put(Environment.POOL_SIZE, "1");
		settings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
		settings.put(Environment.SHOW_SQL, "true");
		settings.put(Environment.FORMAT_SQL, "true");
		settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(Environment.HBM2DDL_AUTO, "create-drop");
	}
	
	@Before
	public void setup() {
		HibernateUtil.setSessionFactory(null);
	}
	
	@Test
	public void testGetSessionFactoryWhenPropertiesIsNotEmptyAndSessionFactoryIsNullShouldInitializeIt() {
		HibernateUtil.setProperties(settings);
		assertThat(HibernateUtil.getSessionFactory()).isNotNull();
	}
	
	@Test
	public void testGetSessionFactoryWhenSessionFactoryAndPropertiesAreNullShouldInitalizeByXMLFile() {
		HibernateUtil.setProperties(null);
		HibernateUtil.setPathConfigurationFile("src/test/resources/");
		assertThat(HibernateUtil.getSessionFactory()).isNotNull();
	}
	
	@Test
	public void testGetSessionFactoryWhenItIsAlreadyInitializeShouldReturnTheSameObject() {
		HibernateUtil.setProperties(settings);
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		assertThat(sessionFactory).isNotNull();
		assertThat(HibernateUtil.getSessionFactory()).isEqualTo(sessionFactory);
	}
	
}
