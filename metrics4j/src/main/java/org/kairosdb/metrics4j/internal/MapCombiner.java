package org.kairosdb.metrics4j.internal;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 Statically combines two maps without copying data.
 @param <K> Key
 @param <V> Value
 */
@ToString
@EqualsAndHashCode
public class MapCombiner<K, V> implements Map<K, V>
{
	private final Map<K, V> m_first;
	private final Map<K, V> m_second;

	public MapCombiner(Map<K, V> first, Map<K, V> second)
	{
		m_first = first;
		m_second = second;
	}

	@Override
	public int size()
	{
		return m_first.size() + m_second.size();
	}

	@Override
	public boolean isEmpty()
	{
		return (m_first.isEmpty() && m_second.isEmpty());
	}

	@Override
	public boolean containsKey(Object key)
	{
		return m_first.containsKey(key) || m_second.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return m_first.containsValue(value) || m_second.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		V ret = m_first.get(key);
		if (ret == null)
			ret = m_second.get(key);

		return ret;
	}

	@Override
	public V put(K key, V value)
	{
		return null;
	}

	@Override
	public V remove(Object key)
	{
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{

	}

	@Override
	public void clear()
	{

	}

	@Override
	public Set<K> keySet()
	{
		Set<K> ret = new HashSet<>(m_second.keySet());
		ret.addAll(m_first.keySet());
		return ret;
	}

	@Override
	public Collection<V> values()
	{
		return null;
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		Set<Entry<K, V>> ret = new HashSet<>(m_second.entrySet());
		ret.addAll(m_first.entrySet()); //first will override second
		return ret;
	}
}
