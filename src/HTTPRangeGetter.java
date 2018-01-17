import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;


/**
 * A runnable class which downloads a given url.
 * It reads CHUNK_SIZE at a time and writes it into a BlockingQueue.
 * It supports downloading a range of data, and limiting the download rate using a token bucket.
 */

public class HTTPRangeGetter implements Runnable {
    private static final int CONNECT_TIMEOUT = 50000;
    private static final int READ_TIMEOUT = 20000;
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
            TokenBucket tokenBucket, int connectionNumber) {  
    	this.downloadableMetadata = downloadableMetadata;
        this.url = url;
        this.range = range;
        this.outQueue = outQueue;
        this.tokenBucket = tokenBucket;

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
		
		LinkedList<Range> rangesToRead = downloadableMetadata.getMissingByRange(range) ;
		URL urlToConnect = new URL(url);
    	connection = (HttpURLConnection)urlToConnect.openConnection();
    	connection.setConnectTimeout(CONNECT_TIMEOUT);
    	connection.setReadTimeout(READ_TIMEOUT);			
    	String byteRange = range.getStart() + "-" + range.getEnd();
    	connection.setRequestProperty("Range", "bytes=" + byteRange);
    	int code = connection.getResponseCode();
    	if ((code != 200) && (code != 206)) {
    		System.err.println("Problem In Connection: HTTTP response Code - " + connection.getResponseCode());
    		return;
    	}
    	
    	BufferedInputStream inData = new BufferedInputStream(connection.getInputStream());
    	
    	for (Range chunkRange: rangesToRead) {
			if (tokenBucket != null)
				tokenBucket.take(chunkRange.getEnd() - chunkRange.getStart() + 1);
			int check = readAndPutOneChunk(inData, chunkRange);
			if (check == -1) {
				connection.disconnect(); 
				System.err.println("Could not read chunk.\nDownload failed");
				System.exit(1);
			}	
		}
		inData.close();	
		connection.disconnect(); 
		return;
    } 

    /**
     * write one chunk into the file
     * @param inputStream
     * @param rangeToDownload
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
	private int readAndPutOneChunk(BufferedInputStream inputStream, Range rangeToDownload) {
		try{		
			int currentNumBytesRead = 0;
			int size_in_bytes =(int)(rangeToDownload.getEnd() - rangeToDownload.getStart() + 1);
			byte[] data = new byte[size_in_bytes];
			long offset = rangeToDownload.getStart();
			int offsetInData = 0;
			while (offsetInData < size_in_bytes ) {
				if ((currentNumBytesRead = inputStream.read(data, offsetInData, size_in_bytes - offsetInData)) == -1) {
					return -1;
				}			
				offsetInData += currentNumBytesRead;
			}		
			outQueue.add(new Chunk(data, offset, size_in_bytes));
			
		} catch(IOException e) {
			System.err.println("Could not read chunk.");
			return -1;
		}
		return 0;
	}


    @Override
    public void run() {
        try {
            this.downloadRange();
        } catch (IOException | InterruptedException e) {
        	System.err.println(e.getMessage() + "\nDownload failed");
        	System.exit(1);
      
        }
    }
}
