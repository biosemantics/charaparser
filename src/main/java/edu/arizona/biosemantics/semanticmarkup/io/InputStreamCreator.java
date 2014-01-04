package edu.arizona.biosemantics.semanticmarkup.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

/**
 * Adopted largely from edu.stanford.nlp.io.IOUtils
 * @author rodenhausen
 */
public class InputStreamCreator {

	/**
	 * Locates this file either using the given URL, or in the CLASSPATH, or in
	 * the file system The CLASSPATH takes priority over the file system! This
	 * stream is buffered and gunzipped (if necessary).
	 * 
	 * @param textFileOrUrl
	 * @return An InputStream for loading a resource
	 * @throws IOException
	 */
	 public InputStream readStreamFromString(String url) throws IOException {
		InputStream in;
		if (url.matches("https?://.*")) {
			URL u = new URL(url);
			URLConnection urlConnection = u.openConnection();
			in = urlConnection.getInputStream();
		} else {
			try {
				in = findStreamInClasspathOrFileSystem(url);
			} catch (FileNotFoundException e) {
				try {
					// Maybe this happens to be some other format of URL?
					URL u = new URL(url);
					URLConnection uc = u.openConnection();
					in = uc.getInputStream();
				} catch (IOException e2) {
					// Don't make the original exception a cause, since it is
					// almost certainly bogus
					throw new IOException("Unable to resolve \"" + url + "\" as either " + "class path, filename or URL"); // ,																											// e2);
				}
			}
		}

		// buffer this stream
		in = new BufferedInputStream(in);

		// gzip it if necessary
		if (url.endsWith(".gz"))
			in = new GZIPInputStream(in);
		return in;
	}

	private InputStream findStreamInClasspathOrFileSystem(String url) throws IOException {
		// ms 10-04-2010:
		// - even though this may look like a regular file, it may be a path
		// inside a jar in the CLASSPATH
		// - check for this first. This takes precedence over the file system.
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(url);
		// if not found in the CLASSPATH, load from the file system
		if (is == null)
			is = new FileInputStream(url);
		return is;
	}
	
}
