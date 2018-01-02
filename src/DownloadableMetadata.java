import java.io.*;
import java.net.*;

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
    private boolean completed;
    private Range range;
    private File metadataFile;
    private long size;
    
    DownloadableMetadata(String url) {
        this.url = url;
        this.filename = getName(url);
        this.metadataFilename = getMetadataName(filename);
        this.completed = false;
        this.size = 0;
        this.range = new Range((long)0, (long)0);      
        
        
    }

    private static String getMetadataName(String filename) {
        return filename + ".metadata";
    }

    private static String getName(String path) {
        return path.substring(path.lastIndexOf('/') + 1, path.length());
    }

    void addRange(Range range) {
    	if (this.range.getEnd() == range.getStart())
    		this.range = new Range(this.range.getStart(), range.getEnd());
    }

    String getFilename() {
        return filename;
    }

    boolean isCompleted() {
        return completed;
    }

    void delete() {
        //TODO
    }

    Range getMissingRange() {
        //TODO
    }

    String getUrl() {
        return url;
    }
}
