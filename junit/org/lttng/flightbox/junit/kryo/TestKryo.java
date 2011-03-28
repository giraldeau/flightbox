package org.lttng.flightbox.junit.kryo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;

public class TestKryo {

	/*
	 * Exercise Kryo
	 */
	
	private static String db = "tests/kryo.db"; 
	
	@BeforeClass
	public static void setup() {
		File f = new File(db);
		if (f.exists()) {
			f.delete();
		}
	}
	
	class FixedSizeObject {
		public int i = 1;
	}
	
	class VariableSizeObject {
		public String str;
		public ArrayList<Integer> list;
		
		public VariableSizeObject() {
			list = new ArrayList<Integer>();
		}
	}
	
	@Test
	public void testKryoWrite() throws IOException {
		FileOutputStream outStream = new FileOutputStream(db);
		Kryo kryo = new Kryo();
		kryo.register(FixedSizeObject.class);
		ObjectBuffer buffer = new ObjectBuffer(kryo);
		FixedSizeObject obj = new FixedSizeObject();
		obj.i = 128;
		
		for (int i=0; i<1000; i++)
			buffer.writeObjectData(outStream, obj);

		outStream.flush();
		outStream.close();
	}
	
}
