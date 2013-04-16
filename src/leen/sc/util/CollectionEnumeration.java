package leen.sc.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

public class CollectionEnumeration<E> implements Enumeration<E> {
	private Iterator<E> it; 
	
	public CollectionEnumeration(Collection<E> collection){
		it=collection.iterator();
	}
	
	@Override
	public boolean hasMoreElements() {
		return it.hasNext();
	}

	@Override
	public E nextElement() {
		return it.next();
	}

}
