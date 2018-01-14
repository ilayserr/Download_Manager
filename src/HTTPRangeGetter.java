import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A runnable class which downloads a given url.
 * It reads CHUNK_SIZE at a time and writes it into a BlockingQueue.
 * It supports downloading a range of data, and limiting the download rate using a token bucket.
 */

//public class HTTPRangeGetter implements Runnable {
//    //private final int chunkSize;
//    private static final int CONNECT_TIMEOUT = 1000000;
//    private static final int READ_TIMEOUT = 2000;
//    private final String url;
//    private final Range range;
//    private final BlockingQueue<Chunk> outQueue;
//    private TokenBucket tokenBucket;
//    private DownloadableMetadata downloadableMetadata;
//    private HttpURLConnection connection;
//    
//    /**
//     * constructor
//     * @param url
//     * @param range
//     * @param outQueue
//     * @param tokenBucket
//     */
//    HTTPRangeGetter(
//    		DownloadableMetadata downloadableMetadata,
//            String url,
//            Range range,
//            BlockingQueue<Chunk> outQueue,
//            TokenBucket tokenBucket,
//            HttpURLConnection connection) {
//    	this.downloadableMetadata = downloadableMetadata;
//        this.url = url;
//        this.range = range;
//        this.outQueue = outQueue;
//        this.tokenBucket = tokenBucket;
//        this.connection = connection;
//        //this.chunkSize = range == null ? 0 : (int)(range.getEnd() - range.getStart());
//    }
//
//    /**
//     * downloading the specific range
//     * @throws IOException
//     * @throws InterruptedException
//     */
//    private void downloadRange() throws IOException, InterruptedException {
//
//		if(range == null) {
//			return;
//		}
//		//int seccesfulRead = 0;
//		
//		LinkedList<Range> rangesToRead = downloadableMetadata.getMissingByRange(range) ;
//		//URL urlToConnect = new URL(url);
//    	//connection = (HttpURLConnection)urlToConnect.openConnection();
//    	//connection.setConnectTimeout(CONNECT_TIMEOUT);
//    	//connection.setReadTimeout(READ_TIMEOUT);
//		
//    	// set the range of byte to download
//    	String byteRange = range.getStart() + "-" + range.getEnd();
//    	connection.setRequestProperty("Range", "bytes=" + byteRange);
//    	
//    	connection.connect();
//    
//    	int code = connection.getResponseCode();
//    	if ((code != 200) && (code != 206)) {
//    		System.err.println("Problem In Connection: HTTTP response Code - " + connection.getResponseCode());
//    		return;
//    	}
//    	
//    	BufferedInputStream inData = new BufferedInputStream(connection.getInputStream());
//
//		for (Range chunkRange: rangesToRead) {
//			tokenBucket.take(range.getEnd() - range.getStart() + 1);
//			try {
//				readAndPutOneChunk(inData, range);
//				//seccesfulRead++;
//
//			} catch (IOException e) {
//				System.err.println("io err");
//				//break;
//			}
//			
//		}
//		inData.close();
//		
//		connection.disconnect(); 
//		
//		
//		System.out.println("Thread closing");
//    }
//
//		private void readAndPutOneChunk(BufferedInputStream inputStream, Range rangeToDownload) throws IOException, InterruptedException {
//			//try{
//			
//			int currentNumBytesRead = 0;
//			int size_in_bytes =(int)(rangeToDownload.getEnd() - rangeToDownload.getStart() + 1);
//			byte[] data = new byte[size_in_bytes];
//			long offset = rangeToDownload.getStart();
//			int offsetInData = 0;
//
//			while (offsetInData < size_in_bytes ){
//				if ((currentNumBytesRead = inputStream.read(data, offsetInData, size_in_bytes - offsetInData)) == -1) {
//					//System.err.println("Problem to read from input: numBytest - " + currentNumBytesRead);
//					return;
//				}
//				
//				offsetInData += currentNumBytesRead;
//			}
//
//			outQueue.put(new Chunk(data, offset, size_in_bytes));
//			//}
////			catch(IOException e) {
////				return;
////				//System.out.println("thread disconnected");
////			}
//		}
//
//
//    @Override
//    public void run() {
//        try {
//            this.downloadRange();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
//

/*************************/

public class HTTPRangeGetter implements Runnable {
    //private final int chunkSize;
    private static final int CONNECT_TIMEOUT = 1000000;
    private static final int READ_TIMEOUT = 2000;
    private final String url;
    private final Range range;
    private final BlockingQueue<Chunk> outQueue;
    private TokenBucket tokenBucket;
    private DownloadableMetadata downloadableMetadata;
    private HttpURLConnection connection;
    
    /**
     * constructor
     * @param url
     * @param range
     * @param outQueue
     * @param tokenBucket
     */
    HTTPRangeGetter(
    		DownloadableMetadata downloadableMetadata,
            String url,
            Range range,
            BlockingQueue<Chunk> outQueue,
            TokenBucket tokenBucket,
            HttpURLConnection connection) {
    	this.downloadableMetadata = downloadableMetadata;
        this.url = url;
        this.range = range;
        this.outQueue = outQueue;
        this.tokenBucket = tokenBucket;
        this.connection = connection;
        //this.chunkSize = range == null ? 0 : (int)(range.getEnd() - range.getStart());
    }

    /**
     * downloading the specific range
     * @throws IOException
     * @throws InterruptedException
     */
    private void downloadRange() throws IOException, InterruptedException {

		if(range == null) {
			return;
		}
		int seccesfulRead = 0;
		LinkedList<Range> rangesToRead = downloadableMetadata.getMissingByRange(range) ;
			//URL urlToConnect = new URL(url);
	    	//connection = (HttpURLConnection)urlToConnect.openConnection();
	    	//connection.setConnectTimeout(CONNECT_TIMEOUT);
	    	//connection.setReadTimeout(READ_TIMEOUT);
			
	    	// set the range of byte to download
	    	String byteRange = range.getStart() + "-" + range.getEnd();
	    	connection.setRequestProperty("Range", "bytes=" + byteRange);
	    	
	    	connection.connect();
//	    	System.setProperty("https.protocols", "TLSv1.1");
	    	
	    	int code = connection.getResponseCode();
	    	if ((code != 200) && (code != 206)) {
	    		System.err.println("Problem In Connection: HTTTP response Code - " + connection.getResponseCode());
	    		return;
	    	}
	    	
	    	BufferedInputStream inData = new BufferedInputStream(connection.getInputStream());
	
			for (Range chunkRange: rangesToRead) {
				tokenBucket.take(chunkRange.getEnd() - chunkRange.getStart() + 1);
				try {
					readAndPutOneChunk(inData, chunkRange);
					seccesfulRead++;
	
				} catch (IOException e) {
					System.err.println("io err");
					break;
				}				
			}
			inData.close();	
			connection.disconnect(); 
		
		
		System.out.println("Thread closing");
    }

		private void readAndPutOneChunk(BufferedInputStream inputStream, Range rangeToDownload) throws IOException, InterruptedException {
//			try{
			
			int currentNumBytesRead = 0;
			int size_in_bytes =(int)(rangeToDownload.getEnd() - rangeToDownload.getStart() + 1);
			byte[] data = new byte[size_in_bytes];
			long offset = rangeToDownload.getStart();
			int offsetInData = 0;

			while (offsetInData < size_in_bytes ){
				if ((currentNumBytesRead = inputStream.read(data, offsetInData, size_in_bytes - offsetInData)) == -1) {
					//System.err.println("Problem to read from input: numBytest - " + currentNumBytesRead);
					return;
				}
				
				offsetInData += currentNumBytesRead;
			}

			outQueue.put(new Chunk(data, offset, size_in_bytes));
			}
//			catch(IOException e) {
//				System.out.println("there was a problem, i'll try again");
//				ExecutorService excutor= Executors.newFixedThreadPool(1);
//				URL urlToConnect = new URL(url);
//				HttpURLConnection connection = (HttpURLConnection)urlToConnect.openConnection();
//    			Runnable workerUrlChecker= new HTTPRangeGetter(downloadableMetadata, url, range, outQueue, tokenBucket, connection);
//    			excutor.execute(workerUrlChecker);
//    			connection.disconnect(); 
//    			return;
//			}
//		}


    @Override
    public void run() {
        try {
            this.downloadRange();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
