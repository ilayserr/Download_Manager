import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.transform.Templates;


/**
 * Describes a file's metadata: URL, file name, size, and which parts already downloaded to disk.
 *
 * The metadata (or at least which parts already downloaded to disk) is constantly stored safely in disk.
 * When constructing a new metadata object, we first check the disk to load existing metadata.
 *
 * CHALLENGE: try to avoid metadata disk footprint of O(n) in the average case
 * HINT: avoid the obvious bitmap solution, and think about ranges...
 */
class DownloadableMetadata {
    private final String metadataFilename;
    private String filename;
    private String url;
    
    //private boolean completed;
    private long size;
    private boolean [] chunksArr;
    private File metadataFile;
    private int chunk_size;
    
    DownloadableMetadata(String url) throws MalformedURLException {
        this.url = url;
        this.filename = getName(url);
        this.metadataFilename = getMetadataName(filename);
        this.metadataFile = new File(this.metadataFilename);
        
        // this.completed = false;
        this.size = getFileSize(new URL(url));     
        this.chunk_size = 4096;
        
        // in case this a small file with less then 4096 bytes.
        if (size < chunk_size) {
        	chunk_size = (int)size;
        }
        this.chunksArr = new boolean [(int)(Math.ceil(size / chunk_size))];
    }
    
    public void write_file () {
    	File temp = new File("temp.metadata");
    	
	}
    
//    public void fetch_from_file (String filename) throws FileNotFoundException {
//    	// Creating File instance to reference text file in Java
//        File text = new File(filename);
//        
//        // Creating Scanner instnace to read File in Java
//        Scanner scnr = new Scanner(text);
//      
//        // Reading each line of file using Scanner class
//        int lineNumber = 1;
//        while(scnr.hasNextLine()){
//            String line = scnr.nextLine();
//            System.out.println("line " + lineNumber + " :" + line);
//            lineNumber++;
//        } 
//    }

    private static String getMetadataName(String filename) {
        return filename + ".metadata";
    }

    private static String getName(String path) {
        return path.substring(path.lastIndexOf('/') + 1, path.length());
    }
    
    // update the location in the array to be true
    void addRange(Range range) {
    	chunksArr[(int)(range.getStart() / chunk_size) - 1] = true;
	//    	if (this.range.getEnd() == range.getStart())
	//    		this.range = new Range(this.range.getStart(), range.getEnd());
    	
    }

    private static long getFileSize(URL url) {
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).setRequestMethod("HEAD");
            }
            conn.getInputStream();
            return conn.getContentLengthLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).disconnect();
            }
        }
    }
    
    String getFilename() {
        return filename;
    }

    boolean isCompleted() {
        for (int i = 0; i < chunksArr.length; i++) {
			if (!chunksArr[i]) return false;
		}
        return true;
    	//return completed;
    }

    void delete() {
        //TODO
    }

    // return the first available chunk size
    Range getMissingRange() {
    	for (int i = 0; i < chunksArr.length; i++) {
 			if (!chunksArr[i]) return (new Range(i * (long)chunk_size, (i + 1) * (long)chunk_size));
 		}
    	
    	// if completed
		return null;
    }

    String getUrl() {
        return url;
    }
}
