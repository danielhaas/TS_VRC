package hk.warp.vrc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Caching {
	
	final File theCacheRoot = new File("cache");
	
	public String checkCache(String collection, String search) throws IOException {
		final File myCacheFile = getCacheFile(collection, search);
		if (!myCacheFile.exists()) return null;
		
		if (System.currentTimeMillis() - myCacheFile.lastModified()> 1000 *60 *60 *24) return null;

		return readFile(myCacheFile);
	}



	static String readFile(File path) 
			throws IOException 
	{
		final byte[] encoded = Files.readAllBytes(Paths.get(path.getAbsolutePath()));
		return new String(encoded);
	}

	static void writeFile(File path, String aString) 
			throws IOException 
	{
		Files.write(Paths.get(path.getAbsolutePath()), aString.getBytes());
	}


	private File getCacheFile(String collection, String search) {
		if (search!=null && search.length()>0)
			return new File(theCacheRoot, collection+"_" + search.replace('=', '_'));
		return new File(theCacheRoot, collection);
	}

	public void writeCache(String collection, String search, String toSave) throws IOException {
		final File myCacheFile = getCacheFile(collection, search);
		writeFile(myCacheFile, toSave);
	}

}
