package leen.sc.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SimpleLeenObjectPool implements LeenObjectPool {

	private Map<Class<?>, LinkedList<?>> poolMap = new HashMap<Class<?>, LinkedList<?>>();
	private Map<Class<?>, Integer> currentSizes = new HashMap<Class<?>, Integer>();
	private Map<Class<?>, Integer> maxSizes = new HashMap<Class<?>, Integer>();

	private final static int DRFAULT_MAX_SIZE = 10;

	@Override
	public <E> E borrowObject(Class<E> clz) throws PoolFullException {
		LinkedList pool = poolMap.get(clz);
		if(pool.isEmpty())
			createObject(clz);
		return (E) pool.pop();
			
	}

	private <E> E createObject(Class<E> clz) throws PoolFullException {
		if (!isPoolInited(clz))
			initPool(clz);
		LinkedList pool = poolMap.get(clz);
		if (!pool.isEmpty())
			return (E) pool.pop();
		if (isFull(clz))
			throw new PoolFullException();
		try {
			E obj = clz.newInstance();
			pool.add(obj);
			return obj;
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"can not create instance of class " + clz);
		}
	}

	private void initPool(Class<?> clz) {
		initPool(clz, DRFAULT_MAX_SIZE);
	}

	private void initPool(Class<?> clz, int maxSize) {
		if (isPoolInited(clz))
			return;
		LinkedList<?> stack = new LinkedList<Object>();
		currentSizes.put(clz, 1);
		maxSizes.put(clz, maxSize);
		poolMap.put(clz, stack);
	}

	public boolean isPoolInited(Class<?> clz) {
		return poolMap.get(clz) != null;
	}

	public boolean isFull(Class<?> clz) {
		if (!isPoolInited(clz))
			throw new IllegalStateException("pool " + clz + " not inited");
		if (currentSizes.get(clz).equals(maxSizes.get(clz)))
			return true;
		return false;
	}

	@Override
	public <E> void returnObject(Class<E> clz, E object) {
		
	}

	@Override
	public <E> void setPoolsize(Class<E> clz, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E> int getPoolsize(Class<E> clz) {
		// TODO Auto-generated method stub
		return 0;
	}

}
