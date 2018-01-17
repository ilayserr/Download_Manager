import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.BlockingQueue;

/**
 * This class takes chunks from the queue, writes them to disk and updates the file's metadata.
 *
 * NOTE: make sure that the file interface you choose writes every update to the file's content or metadata
 *       synchronously to the underlying storage device.
 */

public class FileWriter implements Runnable {

    private final BlockingQueue<Chunk> chunkQueue;
    private DownloadableMetadata downloadableMetadata;
	private File dataFile;
    private File metadataFile;
	private File tempMetadataFile;

    FileWriter(DownloadableMetadata downloadableMetadata, BlockingQueue<Chunk> chunkQueue, String dataFilePath, long fileSize) {
        this.chunkQueue = chunkQueue;
        this.downloadableMetadata = downloadableMetadata;
        this.metadataFile = new File(downloadableMetadata.GetMetadataFilename());
		this.dataFile  = new File(downloadableMetadata.getFilename());
        this.tempMetadataFile = new File(downloadableMetadata.GetMetadataFilename() + ".tmp");
    }

    /**
     * write the file and the metadata file
     * @throws InterruptedException
     * @throws IOException
     */
    private void writeChunks() throws InterruptedException, IOException{
    	RandomAccessFile raf  = new RandomAccessFile(dataFile, "rws");
    	while (!chunkQueue.isEmpty() || !downloadableMetadata.isCompleteWriteRanges()) {
    		Chunk getChunk = chunkQueue.take();
	
			if(getChunk.getData() != null){
				raf.seek(getChunk.getOffset());
				raf.write(getChunk.getData(), 0, getChunk.getSize_in_bytes());
			}
			
			downloadableMetadata.addRange(getChunk);
			FileOutputStream fileOut = new FileOutputStream(tempMetadataFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(downloadableMetadata);
			out.close();
			fileOut.close();
			
			Files.move(tempMetadataFile.toPath(), metadataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
		}
    	raf.close();
    	
        System.out.println("Downloading succedded"); 
        
        try {
            metadataFile.delete();
        } catch (Exception e){
        	System.err.println("Couldn't delete metadata file");
        } 
        return;
    }
    

    @Override
    public void run() {
        try {
            this.writeChunks();
        } catch (InterruptedException | IOException e) {
        	System.err.println(e.getMessage() + "\nDownload failed");
        	System.exit(-1);
        }
    }
}


