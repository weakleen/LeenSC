package leen.sc.response;

public class PlainBufferManager implements BufferManager {
	private int buffersize;
	
	public PlainBufferManager(int buffersize){
		this.buffersize=buffersize;
	}
	
	@Override
	public byte[] getBuffer() {
		return new byte[buffersize];
	}

	@Override
	public void returnBuffer(byte[] toReturn) {

	}

}
