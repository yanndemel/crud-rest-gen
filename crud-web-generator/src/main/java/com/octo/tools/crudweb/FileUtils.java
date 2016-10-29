package com.octo.tools.crudweb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileUtils {

	/**
	   * Copies a directory from a jar file to an external directory.
	   */
	  public static void copyResourcesToDirectory(String jarDir, String destDir)
	      throws IOException {
		  JarFile fromJar = new JarFile(FileUtils.class.getProtectionDomain()
				  .getCodeSource()
				  .getLocation()
				  .getPath());
	    for (Enumeration<JarEntry> entries = fromJar.entries(); entries.hasMoreElements();) {
	      JarEntry entry = entries.nextElement();
	      if (entry.getName().startsWith(jarDir + "/") && !entry.isDirectory()) {
	    	  
	        File dest = new File(destDir + "/" + entry.getName().substring(jarDir.length() + 1));
	        System.out.println("Copy "+entry.getName()+" to "+dest.getPath());
	        File parent = dest.getParentFile();
	        if (parent != null) {
	          parent.mkdirs();
	        }

	        FileOutputStream out = new FileOutputStream(dest);
	        InputStream in = fromJar.getInputStream(entry);

	        try {
	          byte[] buffer = new byte[8 * 1024];

	          int s = 0;
	          while ((s = in.read(buffer)) > 0) {
	            out.write(buffer, 0, s);
	          }
	        } catch (IOException e) {
	          throw new IOException("Could not copy asset from jar file", e);
	        } finally {
	          try {
	            in.close();
	          } catch (IOException ignored) {}
	          try {
	            out.close();
	          } catch (IOException ignored) {}
	        }
	      }
	    }

	  }
	
	static void copyFile(File source, File target) throws IOException {        
	    try (
	            InputStream in = new FileInputStream(source);
	            OutputStream out = new FileOutputStream(target);
	    ) {
	        byte[] buf = new byte[1024];
	        int length;
	        while ((length = in.read(buf)) > 0) {
	            out.write(buf, 0, length);
	        }
	    }
	}

	static void copyDirectory(File source, File target) throws IOException {
	    if (!target.exists()) {
	        target.mkdir();
	    }
	
	    for (String f : source.list()) {
	        FileUtils.copy(new File(source, f), new File(target, f));
	    }
	}

	public static void copy(File sourceLocation, File targetLocation) throws IOException {
	    if (sourceLocation.isDirectory()) {
	        copyDirectory(sourceLocation, targetLocation);
	    } else {
	        copyFile(sourceLocation, targetLocation);
	    }
	}

}
