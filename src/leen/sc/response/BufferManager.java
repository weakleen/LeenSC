package leen.sc.response;

public interface BufferManager {

	public abstract byte[] getBuffer();

	public abstract void returnBuffer(byte[] toReturn);

}