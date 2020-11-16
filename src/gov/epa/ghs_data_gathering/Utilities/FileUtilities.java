package gov.epa.ghs_data_gathering.Utilities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtilities {

	public static void deleteFolder2(String folderPath)  {

		File folder=new File(folderPath);
		
		if (folder.isDirectory()) {
			File []files=folder.listFiles();
			
			if (files.length==0) {
//				System.out.println(folder.getAbsolutePath());
				folder.delete();
				return;
			} else {
				if (folder.getName().contains("ToxRun_")) System.out.println(folder.getName()+"\t"+files.length);
			}
			
//			System.out.println(folder.getAbsolutePath());
			for (File file:files) {
				if (!file.isDirectory()) {
//					System.out.println("delete file:"+file.getAbsolutePath());
					file.delete();
				} else {
					deleteFolder2(file.getAbsolutePath());
				}
				file.delete();
			}
			
			
		} else {
			folder.delete();
		}
		
		
	}
	
	
	public static void deleteFolder(String folderPath)  {

		Path directory = Paths.get(folderPath);

		try {

			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
					
					try {
						Files.delete(file); // this will work because it's always a File
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					try {
						Files.delete(dir); //this will work because Files in the directory are already deleted
					
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					return FileVisitResult.SKIP_SUBTREE;
					 
				}
			});

		} catch (Exception ex) {
			System.out.println("here");
			System.out.println(ex);
		}
	}

	

	//Add file or folder to Jar file
	public static void addToJarFile(File source, JarOutputStream target) throws IOException
	{
		BufferedInputStream in = null;
		try
		{
			if (source.isDirectory())
			{
				String name = source.getPath().replace("\\", "/");
				if (!name.isEmpty())
				{
					if (!name.endsWith("/"))
						name += "/";
					JarEntry entry = new JarEntry(name);
					entry.setTime(source.lastModified());
					target.putNextEntry(entry);
					target.closeEntry();
				}
				for (File nestedFile: source.listFiles())
					addToJarFile(nestedFile, target);
				return;
			}

			JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
			entry.setTime(source.lastModified());
			target.putNextEntry(entry);
			in = new BufferedInputStream(new FileInputStream(source));

			byte[] buffer = new byte[1024];
			while (true)
			{
				int count = in.read(buffer);
				if (count == -1)
					break;
				target.write(buffer, 0, count);
			}
			target.closeEntry();
		}
		finally
		{
			if (in != null)
				in.close();
		}
	}


	public static void createZipFileFromFolder(String folderHtml,String destFilePath) {

		try {
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			ZipOutputStream target = new ZipOutputStream(new FileOutputStream(destFilePath));

			FileUtilities.addToZipFile(new File(folderHtml), target);
			target.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	public static void createTextFileFromFolder(String folderHtml,String destFilePath) {

		try {

			File Folder=new File(folderHtml);
			
			File [] files=Folder.listFiles();
			
			FileWriter fw=new FileWriter(destFilePath);
			
			for (int i=0;i<files.length;i++) {
				fw.write("<file>\r\n");
				ArrayList<String>lines=gov.epa.ghs_data_gathering.Utilities.Utilities.readFileToArray(files[i].getAbsolutePath());
				
				if (i%1000==0) {
					System.out.println(i);
				}
				
				for (String line:lines) {
					fw.write(line+"\r\n");
				}
				
				fw.write("</file>\r\n");
				fw.flush();
			}
			
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//Add file or folder to Jar file
	public static void addToZipFile(File source, ZipOutputStream target) throws IOException {
		BufferedInputStream in = null;
		try {
			if (source.isDirectory()) {
				String name = source.getPath().replace("\\", "/");
				if (!name.isEmpty()) {
					if (!name.endsWith("/"))
						name += "/";
					ZipEntry entry = new ZipEntry(name);
					entry.setTime(source.lastModified());
					target.putNextEntry(entry);
					target.closeEntry();
				}
				for (File nestedFile : source.listFiles())
					addToZipFile(nestedFile, target);
				return;
			}

			ZipEntry entry = new ZipEntry(source.getPath().replace("\\", "/"));
			entry.setTime(source.lastModified());
			target.putNextEntry(entry);
			in = new BufferedInputStream(new FileInputStream(source));

			byte[] buffer = new byte[1024];
			while (true) {
				int count = in.read(buffer);
				if (count == -1)
					break;
				target.write(buffer, 0, count);
			}
			target.closeEntry();
		} finally {
			if (in != null)
				in.close();
		}
	}

	/**
	 * * Add a file into Zip archive in Java. *
	 * 
	 * @param fileName 
	 * @param zos 
	 * @throws FileNotFoundException 
	 * @throws IOException
	 */
//	Read more: http://www.java67.com/2016/12/how-to-create-zip-file-in-java-zipentry-example.html#ixzz5nHcSycBK
	public static void writeToZipFile(String fileName,String folderSrc, String folderDest,ZipOutputStream zipStream)
			throws FileNotFoundException, IOException {
		
//		System.out.println("Writing file : '" + path + "' to zip file");
		File aFile = new File(folderSrc+File.separator+fileName);
		FileInputStream fis = new FileInputStream(aFile);
		
		ZipEntry zipEntry = new ZipEntry(folderDest+File.separator+fileName);
		zipStream.putNextEntry(zipEntry);
		
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipStream.write(bytes, 0, length);
		}
		zipStream.closeEntry();
		fis.close();
	}	
	
	/**
	 * Downloads a string for a given URL
	 * @param url
	 * @return
	 */
	public static String getText(String url) {
		//		   TODO move to a utility class    

		try {
			URL website = new URL(url);
			URLConnection connection = website.openConnection();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							connection.getInputStream()));

			StringBuilder response = new StringBuilder();
			String inputLine;

			while ((inputLine = in.readLine()) != null) 
				response.append(inputLine);

			in.close();

			return response.toString();

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
	public static String getText_UTF8(String url) {
		//		   TODO move to a utility class    

		try {

			URL website = new URL(url);
			URLConnection connection = website.openConnection();

			
		    BufferedReader input = new BufferedReader(
		            new InputStreamReader(connection.getInputStream(), "UTF-8")); 
		    
		    StringBuilder strB = new StringBuilder();
		    String str;
		    while (null != (str = input.readLine())) {
		        strB.append(str).append("\r\n"); 
		    }
		    input.close();
			
			
			return strB.toString();

		} catch (Exception ex) {
			return null;
		}
	}



	/**
	 * Read a file line by line until you hit seek string
	 * 
	 * @param seek
	 * @param br
	 * @return
	 */
	public static String seek(String seek,BufferedReader br) {
		try {

			String Line="";

			int counter=0;

			while (true) {
				Line=br.readLine();
				counter++;

				//				System.out.println(counter+"\t"+seek+"\t"+Line);

				if (Line==null) break;

				if (Line.indexOf(seek)>-1 ) {
					return Line;
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Download a file by bytes instead of lines to get whole file faster, works with.xls files as well
	 * 
	 * @param strURL
	 * @param outputFilePath
	 */
	public static void downloadfile2(String strURL,String outputFilePath) {

		try {

			URL website = new URL(strURL);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(outputFilePath);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

		} catch (FileNotFoundException ex1) {
//			System.out.println("file not found");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Download a webpage line by line:
	 * 
	 * @param URL
	 * @param destFilePath
	 */
	public static void downloadFile(String URL,String destFilePath) {

		try {


			File destFile=new File(destFilePath);
			destFile.getParentFile().mkdirs();
			
//			System.out.println(URL);
			//			System.out.println(destFilePath+"\n");

			java.net.URL myURL = new java.net.URL(URL);

			BufferedReader br
			= new BufferedReader(new InputStreamReader(myURL.openStream()));

			FileWriter fw=new FileWriter(destFilePath);

			int counter=0;

			while (true) {
				String Line=br.readLine();

				if (Line==null) break;

				//				System.out.println(counter+" " +Line);
				//				System.out.println(Line);

				fw.write(Line+"\r\n");
				fw.flush();

				counter++;
			}

			br.close();
			fw.close();

		} catch (FileNotFoundException ex1) {
			System.out.println("file not found:"+URL);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FileUtilities.deleteFolder2("R:\\NCCT Results 11_16_17\\web-reports-12");
		
//		FileUtilities.deleteFolder2("R:\\NCCT Results 11_16_17\\web-reports-qr2");
		
		
//		FileUtilities.deleteFolder("R:\\bob");
	}

}
