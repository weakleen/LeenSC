package leen.sc.util;

public interface LeenObjectPool {
	
	public <E> E borrowObject(Class<E> clz) throws PoolFullException;

	public <E> void returnObject(Class<E> clz, E object);
	
	public <E> void setPoolsize(Class<E> clz,int size);
	
	public <E> int getPoolsize(Class<E> clz);

}
