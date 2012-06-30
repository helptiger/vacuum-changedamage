package vacuum.changedamage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class Updater {
	
	private static String downloadText(String location) throws IOException{
		URL url = new URL(location);
		BufferedInputStream bis = new BufferedInputStream(url.openStream());
		StringBuffer buf = new StringBuffer();
		int i;
		while((i = bis.read()) != -1)
			buf.append((char)i);
		bis.close();
		return buf.toString();
	}
	
	private static void copy(InputStream is, OutputStream os) throws IOException{
		BufferedInputStream in = new BufferedInputStream(is);
		BufferedOutputStream out = new BufferedOutputStream(os);
		int i;
		while((i = is.read()) != -1) os.write(i);
		in.close();
		out.close();
	}
	
	public static void update(String jarURL, String versionURL, String downloadLocation, String tempLocation, String currentVersion) throws IOException{
		if(!downloadText(versionURL).equals(currentVersion)){
			File temp = new File(tempLocation);
			File newLoc = new File(downloadLocation);
			System.out.println("Temporary file: " + temp + "; JAR file: " + newLoc);
			temp.createNewFile();
			temp.deleteOnExit();
			copy(new URL(jarURL).openStream(), new FileOutputStream(temp));
			copy(new FileInputStream(temp), new FileOutputStream(newLoc, false));
		}
	}
}
