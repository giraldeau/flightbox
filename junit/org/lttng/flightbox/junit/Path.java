package org.lttng.flightbox.junit;


import java.io.File;

public class Path {

	public static File getBaseDir(){
		return new File (".");
	}

	public static File getManifestDir() {
		return new File("./resources/manifest/"); 
	}
	
	public static File getTestManifestDir() {
		return new File("./tests/manifest/"); 
	}

	public static File getTestStubDir() {
		return new File("./tests/stub/"); 
	}
	
	public static File getTraceDir() {
		return new File("./tests/traces/");
	}
	public static File getGraphDir() {
		File graphDir = new File("./tests/graph/");
		if (!graphDir.exists())
			graphDir.mkdirs();
		return graphDir;
	}
}