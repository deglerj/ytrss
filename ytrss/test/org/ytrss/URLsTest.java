package org.ytrss;

import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;

public class URLsTest {

	@Test
	public void testCopyToStringDoesntFail() throws IOException{
		final String string = URLs.copyToString("http://youtube.com/");
		assertFalse(string.isEmpty());
	} 
	
}
