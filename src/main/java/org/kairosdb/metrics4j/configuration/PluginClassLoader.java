package org.kairosdb.metrics4j.configuration;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class PluginClassLoader extends URLClassLoader
{
	ClassLoader m_parentLoader;

	public PluginClassLoader(URL[] urls, ClassLoader parent)
	{
		super(urls, parent);
		m_parentLoader = parent;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException
	{
		synchronized (getClassLoadingLock(name))
		{
			// First, check if the class has already been loaded
			Class<?> c = findLoadedClass(name);

			if (c == null)
			{
				// If still not found, then invoke findClass in order
				// to find the class.
				try
				{
					c = findClass(name);
				}
				catch (ClassNotFoundException e)
				{
					//pass to the parent to throw exception
				}
			}

			if (c == null)
			{
				c = m_parentLoader.loadClass(name);
			}
			if (resolve)
			{
				resolveClass(c);
			}
			return c;
		}
	}

	@Override
	public URL getResource(String name)
	{
		URL url;

		url = findResource(name);

		if (url == null)
			url = m_parentLoader.getResource(name);

		return url;
	}


	@Override
	public Enumeration<URL> getResources(String name) throws IOException
	{
		return new ConcatEnumeration(findResources(name), m_parentLoader.getResources(name));
	}


	private class ConcatEnumeration implements Enumeration<URL>
	{
		Enumeration<URL> m_first;
		Enumeration<URL> m_second;

		public ConcatEnumeration(Enumeration<URL> first, Enumeration<URL> second)
		{
			m_first = first;
			m_second = second;
		}

		@Override
		public boolean hasMoreElements()
		{
			return m_first.hasMoreElements() || m_second.hasMoreElements();
		}

		@Override
		public URL nextElement()
		{
			return m_first.hasMoreElements() ? m_first.nextElement() : m_second.nextElement();
		}
	}
}
