import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

/**
 * This class takes chunks from the queue, writes them to disk and updates the file's metadata.
 *
 * NOTE: make sure that the file interface you choose writes every update to the file's content or metadata
 *       synchronously to the underlying storage device.
 */


//public class FileWriter implements Runnable {
//
//    private final BlockingQueue<Chunk> chunkQueue;
//    private DownloadableMetadata downloadableMetadata;
//   // private static  boolean isCompleted = false;
//   // private String dataFilePath;
//    private File tempFile;
//	private File dataFile;
//    private File metadataFile;
//	private File tempMetadataFile;
//    private long fileSize;
//
//    FileWriter(DownloadableMetadata downloadableMetadata, BlockingQueue<Chunk> chunkQueue, String dataFilePath, long fileSize) {
//        this.chunkQueue = chunkQueue;
//        this.downloadableMetadata = downloadableMetadata;
//       // this.dataFilePath = dataFilePath;
//        this.fileSize = fileSize;
//        this.metadataFile = new File(downloadableMetadata.GetMetadataFilename());
//		this.dataFile  = new File(downloadableMetadata.getFilename());
////        this.tempFile = new File(downloadableMetadata.getFilename() + ".tmp");
//        this.tempMetadataFile = new File(downloadableMetadata.GetMetadataFilename() + ".tmp");
//
//    }
//
//    private void writeChunks() throws InterruptedException, IOException{
//
//    	// check if add count will solve the problem  *******************
//        while(!downloadableMetadata.isCompleteWriteRanges()) {
//        	
//        	Chunk getChunk = null;
//        	RandomAccessFile raf  = null;
//      	
//        	while (!chunkQueue.isEmpty()) {
//        		
//			    getChunk = chunkQueue.take();
//
//				try {
//					raf = new RandomAccessFile(dataFile, "rw");
//					raf.setLength(fileSize);
//					if(getChunk.getData() != null){
//						raf.seek(getChunk.getOffset());
//						raf.write(getChunk.getData(), 0, getChunk.getSize_in_bytes());
//					}
//
//					downloadableMetadata.addRange(getChunk);
//					FileOutputStream fileOut = new FileOutputStream(tempMetadataFile);
//					ObjectOutputStream out = new ObjectOutputStream(fileOut);
//					out.writeObject(downloadableMetadata);
//					out.close();
//					fileOut.close();
//
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}finally {
//					raf.close();
//					
//					// check if the content of the file passed from one file to another **************
//					//tempFile.renameTo(dataFile);
//					tempMetadataFile.renameTo(metadataFile);
//			    }
//
//			}
//        		
//        }
//        System.out.println("Finish downloading :)");        
//        try {
//            metadataFile.delete();
//        } catch (Exception e){
//        	System.err.println("Couldn't delete metadata file");
//        }
//    }
//    
//   // public static void terminate() {
//   // 	isCompleted = true;
//  //  }
//
//    @Override
//    public void run() {
//        try {
//            this.writeChunks();
//        } catch (InterruptedException | IOException e) {
//        	e.printStackTrace();
//        }finally {
//
//		}
//    }
//}
//


/********************************/

public class FileWriter implements Runnable {

    private final BlockingQueue<Chunk> chunkQueue;
    private DownloadableMetadata downloadableMetadata;
   // private static  boolean isCompleted = false;
   // private String dataFilePath;
    private File tempFile;
	private File dataFile;
    private File metadataFile;
	private File tempMetadataFile;
    private long fileSize;

    FileWriter(DownloadableMetadata downloadableMetadata, BlockingQueue<Chunk> chunkQueue, String dataFilePath, long fileSize) {
        this.chunkQueue = chunkQueue;
        this.downloadableMetadata = downloadableMetadata;
       // this.dataFilePath = dataFilePath;
        this.fileSize = fileSize;
        this.metadataFile = new File(downloadableMetadata.GetMetadataFilename());
		this.dataFile  = new File(downloadableMetadata.getFilename());
//        this.tempFile = new File(downloadableMetadata.getFilename() + ".tmp");
        this.tempMetadataFile = new File(downloadableMetadata.GetMetadataFilename() + ".tmp");

    }

    private void writeChunks() throws InterruptedException, IOException{

    	// check if add count will solve the problem  *******************
        while(!downloadableMetadata.isCompleteWriteRanges()) {
        	
        	Chunk getChunk = null;
        	RandomAccessFile raf  = null;
      	
        	while (!chunkQueue.isEmpty()) {
        		
			    getChunk = chunkQueue.take();

				try {
					raf = new RandomAccessFile(dataFile, "rw");
					raf.setLength(fileSize);
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

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					raf.close();
					
					// check if the content of the file passed from one file to another **************
					//tempFile.renameTo(dataFile);
					tempMetadataFile.renameTo(metadataFile);
			    }

			}
        		
        }
        System.out.println("Finish downloading :)");        
        try {
            metadataFile.delete();
        } catch (Exception e){
        	System.err.println("Couldn't delete metadata file");
        }
    }
    
   // public static void terminate() {
   // 	isCompleted = true;
  //  }

    @Override
    public void run() {
        try {
            this.writeChunks();
        } catch (InterruptedException | IOException e) {
        	e.printStackTrace();
        }finally {

		}
    }
}


/*******************************/

//public class FileWriter implements Runnable {
//
//    private final BlockingQueue<Chunk> chunkQueue;
//    private DownloadableMetadata downloadableMetadata;
//
//    FileWriter(DownloadableMetadata downloadableMetadata, BlockingQueue<Chunk> chunkQueue) {
//        this.chunkQueue = chunkQueue;
//        this.downloadableMetadata = downloadableMetadata;
//    }
//
//    private void writeChunks() throws IOException {
//        //TODO
//    }
//
//    @Override
//    public void run() {
//        try {
//            this.writeChunks();
//        } catch (IOException e) {
//            e.printStackTrace();
//            //TODO
//        }
//    }
//}
