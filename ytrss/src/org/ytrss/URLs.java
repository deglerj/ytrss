package org.ytrss;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class URLs {

	public static String copyToString(String url) throws IOException{
		return copyToString(new URL(url));
	}
	
	public static String copyToString(URL url) throws IOException{
		try(InputStream input = url.openStream()){
			String string = IOUtils.toString(input);
			return string;
		}
	}
	
}
