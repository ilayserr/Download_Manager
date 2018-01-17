import java.util.*;

import org.omg.CORBA.INTERNAL;

/**
 * Describes a file's metadata: URL, file name, size, and which parts already downloaded to disk.
 *
 * The metadata (or at least which parts already downloaded to disk) is constantly stored safely in disk.
 * When constructing a new metadata object, we first check the disk to load existing metadata.
 *
 * CHALLENGE: try to avoid metadata disk footprint of O(n) in the average case
 * HINT: avoid the obvious bitmap solution, and think about ranges...
 */
class DownloadableMetadata implements java.io.Serializable{
	private final String metadataFilename;
    private String url;
    private String filename;
    private boolean[] rangesArray;
    public int lastGivenIndex;
    private int countSuccesfulDownloadedRanges;
    int precentege;
   
    /**
     * constructor
     * @param url
     * @param numOfRanges
     * @param fileSize
     */
    DownloadableMetadata(String url, int numOfRanges){ 
        this.url = url;
        this.filename = getName(url);
        this.metadataFilename = getMetadataName(filename);
        this.rangesArray = new boolean[numOfRanges];
        this.lastGivenIndex = 0;
        this.countSuccesfulDownloadedRanges = 0;
        this.precentege = (int) Math.floor((double)(numOfRanges / 100));
        
    }

    /**
     * create the metadata file name.
     * @param filename
     * @return the metadata file name.
     */
    public static String getMetadataName(String filename) {
        return filename + ".metadata";
    }
    
    /**
     * @return the metadata file name.
     */
    public String GetMetadataFilename() {
		return metadataFilename;
	}

    /**
     * returns the file name.
     * @param path
     * @return the file name.
     */
    public static String getName(String path) {
        return path.substring(path.lastIndexOf('/') + 1, path.length());
    }
	
    /**
     * update the location in the array to be true
     * @param chunk
     */
	void addRange(Chunk chunk) {
		rangesArray[(int)(chunk.getOffset() / rangesArray.length)] = true;
	    countSuccesfulDownloadedRanges++;
	    if (countSuccesfulDownloadedRanges % precentege == 0) {
	    	int precentToPrint = countSuccesfulDownloadedRanges / precentege;
	    	if (precentToPrint != 100)
	    		System.out.println("Downloaded " + precentToPrint + "%");
	    }
	}

	/**
    * returns the file name member.
    * @param path
    * @return the file name member.
    */
    String getFilename() {
        return filename;
    }

    /**
     * check if all the ranges were writen.
     * @return
     */
    boolean isCompleteWriteRanges() {
        return (countSuccesfulDownloadedRanges == rangesArray.length);
    }

    /**
     * return the url
     * @return
     */
    String getUrl() {
        return url;
    }
    
    /**
     * bringing a list of the missing ranges
     * @param range
     * @return
     */
    LinkedList<Range> getMissingByRange (Range range) {
    	LinkedList<Range> missing = new LinkedList<>();
    	for (long i = range.getStart(); i < range.getEnd(); i += IdcDm.CHUNK_SIZE) {
			if (rangesArray[(int)(i / rangesArray.length)] == false) 
				missing.add(new Range(i, Math.min((i + IdcDm.CHUNK_SIZE) - 1 , range.getEnd())));
		}
    	return missing;
    }
}