package com.ld.myfastjson.serializer;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.SoftReference;

import com.ld.myfastjson.utils.IOUtils;

public final class SerializerWriter extends Writer {

	/**
	 * 数据缓存
	 */
	protected char buf[];
	
	/**
	 * 字符串长度
	 */
	protected int count;
	
	/**
	 * buf数组缓存，一次处理完后，将数组继续留在缓存中，在当前线程再次序列化时使用
	 */
	private final static ThreadLocal<SoftReference<char[]>> bufLocal = new ThreadLocal<SoftReference<char[]>>();
	
	public SerializerWriter() {
		SoftReference<char[]> ref = bufLocal.get();
		
		if (ref != null) {
			buf = ref.get();
			bufLocal.set(null);
		}
		
		if (buf == null) 
			buf = new char[1024];
	}
	
	@Override
	public void close() {
		bufLocal.set(new SoftReference<char[]>(buf));
	}
	
	/* Write 方法重写 begin */
	public void write(int c) {
		int newCount = count + 1;
		if (newCount > buf.length)
			expandCapacity(newCount);
		
		buf[count] = (char) c;
		count = newCount;
	}
	
	public void write(char c) {
		int newCount = count + 1;
		if (newCount > buf.length)
			expandCapacity(newCount);
		
		buf[count] = c;
		count = newCount;
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) {
		if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length)
			throw new IndexOutOfBoundsException();
		
		int newCOunt = count + len;
		if (newCOunt > buf.length)
			expandCapacity(newCOunt);
		
		System.arraycopy(cbuf, off, buf, count, len);
		count = newCOunt;
	}
	
	@Override
	public void write(String str) {
		if (str == null) {
			writeNull();
			return;
		}
		
		int newCount = count + str.length();
		if (newCount > buf.length)
			expandCapacity(newCount);
		
		str.getChars(0, str.length(), buf, count);
		count = newCount;
	}
	
	public void write(String str, int off, int len) {
		int newCount = count + len;
		if (newCount > buf.length)
			expandCapacity(newCount);
		
		str.getChars(off, off + len, buf, count);
		count = newCount;
	}
	
	@Override
	public SerializerWriter append(char c) {
		write(c);
		
		return this;
	}
	
	@Override
	public SerializerWriter append(CharSequence csq) {
		String s = csq == null ? "null" : csq.toString();
		write(s, 0, s.length());
		
		return this;
	}
	
	@Override
	public SerializerWriter append(CharSequence csq, int start, int end) {
		String s = csq == null ? "null" : csq.subSequence(start, end).toString();
		write(s, 0, s.length());
		
		return this;
	}
	/* Write 方法重写 end */
	
	public void writeNull() {
		int newCount = count + 4;
		if (newCount > buf.length)
			expandCapacity(newCount);
		
		buf[count] = 'n';
		buf[count + 1] = 'u';
		buf[count + 2] = 'l';
		buf[count + 3] = 'l';
		count = newCount;
	}
	
	/* 整形和长整形 begin */
	public void writeInt(int i) {
		if (i == Integer.MIN_VALUE)
			write("-2147483648");
		
		int size = i < 0 ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
		int newCount = count + size;
		if (newCount > buf.length)
			expandCapacity(newCount);
		
		IOUtils.getChars(i, newCount, buf);
		
		count = newCount;
	}
	
	public void writeLong(long i) {
		if (i == Long.MIN_VALUE) {
			write("-9223372036854775808");
			return;
		}
		
		int size = i < 0 ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
		
		int newCount = count + size;
		if (newCount > buf.length)
			expandCapacity(newCount);
		
		IOUtils.getChars(i, newCount, buf);
		
		count = newCount;
	}
	/* 整形和长整形 end */
	
	/* 基本类型数组 begin */
	public void writeBooleanArray(boolean[] array) {
		int totalSize = 2;
		for (int i = 0; i < array.length; i++) {
			if (i != 0)
				totalSize++;
			
			boolean val = array[i];
			int size;
			if (val) {
				size = 4;
			} else {
				size = 5;
			}
			
			totalSize += size;
		}
		
		int newCount = count + totalSize;
		if (totalSize > buf.length) 
			expandCapacity(newCount);
		
		buf[count] = '[';
		int currentSize = count + 1;
		for (int i = 0; i < array.length; i++) {
			if (i != 0)
				buf[currentSize++] = ',';
			
			boolean val = array[i];
			if (val) {
				buf[currentSize++] = 't';
				buf[currentSize++] = 'r';
				buf[currentSize++] = 'u';
				buf[currentSize++] = 'e';
			} else {
				buf[currentSize++] = 'f';
				buf[currentSize++] = 'a';
				buf[currentSize++] = 'l';
				buf[currentSize++] = 's';
				buf[currentSize++] = 'e';
			}
		}
		buf[currentSize] = ']';
		
		count = newCount;
	}
	
	public void writeIntArray(int[] array) {
		
	}
	
	public void writeShortArray(short[] array) {
		
	}
	
	public void writeLongArray(long[] array) {
		
	}
	
	public void writeByteArray(byte[] array) {
		
	}
	/* 基本类型数组 end */

	public void reset() {
		count = 0;
	}
	
	@Override
	public void flush() throws IOException {
	}
	
	public String toString() {
		return new String(buf, 0, count);
	}
	
	/**
	 * 数组扩容
	 * 
	 * @param minimumCapacity
	 */
	private void expandCapacity(int minimumCapacity) {
		int newCapacity = buf.length * 3 / 2 + 1;
		
		if (newCapacity < minimumCapacity)
			newCapacity = minimumCapacity;
		
		char newValue[] = new char[newCapacity];
		System.arraycopy(buf, 0, newValue, 0, count);
		buf = newValue;
	}

}
