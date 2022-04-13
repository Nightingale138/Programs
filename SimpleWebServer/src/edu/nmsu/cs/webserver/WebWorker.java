package edu.nmsu.cs.webserver;


/**
 * Web worker: an object of this class executes in its own new thread to receive and respond to a
 * single HTTP request. After the constructor the object executes on its "run" method, and leaves
 * when it is done.
 *
 * One WebWorker object is only responsible for one client connection. This code uses Java threads
 * to parallelize the handling of clients: each WebWorker runs in its own thread. This means that
 * you can essentially just think about what is happening on one client at a time, ignoring the fact
 * that the entirety of the webserver execution might be handling other clients, too.
 *
 * This WebWorker class (i.e., an object of this class) is where all the client interaction is done.
 * The "run()" method is the beginning -- think of it as the "main()" for a client interaction. It
 * does three things in a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it writes out some HTML
 * content for the response content. HTTP requests and responses are just lines of text (in a very
 * particular format).
 * 
 * @author Jon Cook, Ph.D.
 *
 **/


import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class WebWorker implements Runnable
{

	private Socket socket;
	private String fileName;
	private File pathfile;
	private String typeExtension;

	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s)
	{
		socket = s;
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP request and then returns, which
	 * destroys the thread. This method assumes that whoever created the worker created it with a
	 * valid open socket object.
	 **/
	public void run()
	{
		System.err.println("Handling connection...");
		try
		{
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			readHTTPRequest(is);
			if(typeExtension != null) {
				if (typeExtension.equals("html"))
					writeHTTPHeader(os, "text/html", pathfile);
				
				
				// gets the different types of image files
				else if(typeExtension.equals("gif"))
					writeHTTPHeader(os, "image/gif", pathfile);
				
				else if (typeExtension.equals("jpeg"))
					writeHTTPHeader(os, "image/jpg", pathfile);
				
				else if(typeExtension.equals("png"))
					writeHTTPHeader(os, "image/png", pathfile);
			} // end if
			else
				writeHTTPHeader(os, "text/html", pathfile);
			
			writeContent(os, pathfile);
			os.flush();
			socket.close();
		} // end try
		catch (Exception e)
		{
			System.err.println("Output error: " + e);
		} // end catch
		System.err.println("Done handling connection.");
		return;
	} // end class

	/**
	 * Read the HTTP request header.
	 **/
	private void readHTTPRequest(InputStream is)
	{
		String line;
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		while (true)
		{
			try
			{
				while (!r.ready())
					Thread.sleep(1);
				line = r.readLine();
				System.err.println("Request line: (" + line + ")");
				if(line.contains("GET") && !line.contains("favicon")) {
					fileName = line.substring(line.indexOf('/') + 1, line.indexOf(' ', line.indexOf('/')));
					
					//targets the www directory
					fileName = "www/" + fileName;
					typeExtension = fileName.substring(fileName.indexOf('.') + 1);
					pathfile = new File(fileName);
				} // end if
				if (line.length() == 0)
					break;
			} // end try
			catch (Exception e)
			{
				System.err.println("Request error: " + e);
				break;
			} // end catch
		} // end while
		return ;
	}

	/**
	 * Write the HTTP header lines to the client network connection.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 * @param contentType
	 *          is the string MIME content type (e.g. "text/html")
	 **/
	private void writeHTTPHeader(OutputStream os, String contentType, File fname) throws Exception
	{
		if(fname != null) {
			if(fname.exists()) {
				Date d = new Date();
				DateFormat df = DateFormat.getDateTimeInstance();
				df.setTimeZone(TimeZone.getTimeZone("GMT"));
				os.write("HTTP/1.1 200 OK\n".getBytes());
				os.write("Date: ".getBytes());
				os.write((df.format(d)).getBytes());
				os.write("\n".getBytes());
				os.write("Server: Ruben's very own server\n".getBytes());
				os.write("Connection: close\n".getBytes());
				os.write("Content-Type: ".getBytes());
				os.write(contentType.getBytes());
				os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
			} // end if
			else {
				Date d = new Date();
				DateFormat df = DateFormat.getDateTimeInstance();
				df.setTimeZone(TimeZone.getTimeZone("GMT"));
				os.write("HTTP/1.1 404 NOT FOUND\n".getBytes());
				os.write("Date: ".getBytes());
				os.write((df.format(d)).getBytes());
				os.write("\n".getBytes());
				os.write("Server: Ruben's very own server\n".getBytes());
				os.write("Connection: close\n".getBytes());
				os.write("Content-Type: ".getBytes());
				os.write(contentType.getBytes());
				os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
			} // end else
		} // end if
		else {
			Date d = new Date();
			DateFormat df = DateFormat.getDateTimeInstance();
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			os.write("HTTP/1.1 200 OK\n".getBytes());
			os.write("Date: ".getBytes());
			os.write((df.format(d)).getBytes());
			os.write("\n".getBytes());
			os.write("Server: Ruben's very own server\n".getBytes());
			os.write("Connection: close\n".getBytes());
			os.write("Content-Type: ".getBytes());
			os.write(contentType.getBytes());
			os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
		} // end else
		return;
	} // end write

	/**
	 * Write the data content to the client network connection. This MUST be done after the HTTP
	 * header has been written out.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 **/
	private void writeContent(OutputStream os, File fname) throws Exception
	{
		if(fname != null && fname.exists()) {
			
			if(typeExtension.equals("html")) {
				BufferedReader reader = new BufferedReader (new FileReader(fname));
				String contentLine = reader.readLine();
				
				while(contentLine != null) {
					
					os.write(contentLine.getBytes());
					contentLine = reader.readLine();
				} // end while
				
				reader.close();
			} // end if
			
			if(typeExtension.equals("png")) {
				BufferedImage img = null;
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try {
					img = ImageIO.read(fname);
				} // end try
				catch(IOException e){}
				
				ImageIO.write(img, "png", output);
				byte [] data = output.toByteArray();
				os.write(data);
			}
			// gets different types of image types
			if(typeExtension.equals("jpeg")) {
				BufferedImage img = null;
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try {
					img = ImageIO.read(fname);
				} // end try
				catch(IOException e){}
				
				ImageIO.write(img, "jpg", output);
				byte [] data = output.toByteArray();
				os.write(data);
			} // end if
			
			if(typeExtension.equals("gif")) {
				BufferedImage img = null;
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try {
					img = ImageIO.read(fname);
				} // end try
				catch(IOException e){}
				
				ImageIO.write(img, "gif", output);
				byte [] data = output.toByteArray();
				os.write(data);
			} // end if
		} // end outer if
	} // end write
} // end class
