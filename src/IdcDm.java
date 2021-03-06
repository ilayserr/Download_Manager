import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;

/***
 * A download manger
 * 
 * @author ilay serr 
 */
public class IdcDm {
	static int CHUNK_SIZE = 4096;
	static String [] args_cons;
	 
   /**
    * Receive arguments from the command-line, provide some feedback and start the download.
    *
    * @param args command-line arguments
    * @throws IOException 
    */
	public static void main(String[] args) throws IOException {
		args_cons = args;
		int numberOfWorkers = 1;
		Long maxBytesPerSecond = null;
		
		if (args.length < 1 || args.length > 3) {
			System.err.printf("usage:\n\tjava IdcDm URL [MAX-CONCURRENT-CONNECTIONS] [MAX-DOWNLOAD-LIMIT]\n");
			System.exit(1);
		} else if (args.length >= 2) {
			numberOfWorkers = Integer.parseInt(args[1]);
			if (args.length == 3)
				maxBytesPerSecond = Long.parseLong(args[2]);
		}

        String url = args[0];

        System.err.printf("Downloading");
        if (numberOfWorkers > 1)
        	System.err.printf(" using %d connections", numberOfWorkers);
        if (maxBytesPerSecond != null)
        	System.err.printf(" limited to %d Bps", maxBytesPerSecond);
        System.err.printf("...\n");

        try {
			DownloadURL(url, numberOfWorkers, maxBytesPerSecond);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage() + "\nDownload failed.");
        	System.exit(-1);
		}
	}

   /**
    * Initiate the file's metadata, and iterate over missing ranges. For each:
    * 1. Setup the Queue, TokenBucket, DownloadableMetadata, FileWriter, RateLimiter, and a pool of HTTPRangeGetters
    * 2. Join the HTTPRangeGetters, send finish marker to the Queue and terminate the TokenBucket
    * 3. Join the FileWriter and RateLimiter
    *
    * Finally, print "Download succeeded/failed" and delete the metadata as needed.
    *
    * @param url URL to download
    * @param numberOfWorkers number of concurrent connections
    * @param maxBytesPerSecond limit on download bytes-per-second
    * @throws InterruptedException 
    * @throws IOException 
    */
	private static void DownloadURL(String url, int numberOfWorkers, Long maxBytesPerSecond) throws InterruptedException, IOException {
   		
   		long fileSize = 0;
		try {
			fileSize = getFileSize(new URL(url)) - 1;
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		numberOfWorkers = defineNumberOfWorkers(fileSize, numberOfWorkers);		
		int numOfRanges = (int) Math.ceil(fileSize/(double) CHUNK_SIZE);
   		DownloadableMetadata downloadableMetadata = getExistOrNewMetadata(url, numOfRanges, fileSize); 		
   		BlockingQueue<Chunk> chunkQueue = new LinkedBlockingQueue<>();
   		
   		TokenBucket tokenBucket = null;
   		Thread rateLimiterThread = null;
   		if (maxBytesPerSecond != null) {
	   		tokenBucket = new TokenBucket();    		
	        rateLimiterThread = new Thread(new RateLimiter(tokenBucket, maxBytesPerSecond));
	        rateLimiterThread.start();
   		}
		Thread fileWriterThread = new Thread(new FileWriter(downloadableMetadata, chunkQueue, DownloadableMetadata.getName((url)), fileSize));
		fileWriterThread.start();        
	    Range [] rangesForWorkers = getReangesArray(numberOfWorkers, fileSize);
	    ExecutorService excutor= Executors.newFixedThreadPool(numberOfWorkers); 
	    for (int i = 0; i < numberOfWorkers; i++) {
	   		HTTPRangeGetter workerUrlChecker= new HTTPRangeGetter(downloadableMetadata, url, rangesForWorkers[i], chunkQueue, tokenBucket, (i + 1));
			excutor.execute(workerUrlChecker);
	   	}	
       	excutor.shutdown();
       	
       	while(!excutor.isTerminated());	
       	if (tokenBucket != null)
       		tokenBucket.terminate();
       	
       	try {
       		if (rateLimiterThread != null)
       			rateLimiterThread.join();
       		fileWriterThread.join();
   		} catch (InterruptedException e) {
   			e.printStackTrace();
   		}

	}
   
   /**
    * If the size of the file is smaller than 4096 * number of http connections
    * create only one http connection.
    * @param fileSize
    * @param numberOfWorkers
    * @return new numberofworkers
    */
	private static int defineNumberOfWorkers(Long fileSize, int numberOfWorkers) {
   	if (fileSize < CHUNK_SIZE * numberOfWorkers) 
   		return 1;
   	else return numberOfWorkers;
		
	}

   /**
    * return an array of the total ranges of each http connection.
    * @param numberOfWorkers
    * @param fileSize
    * @return the array
    */
	private static Range[] getReangesArray(int numberOfWorkers, long fileSize) {
   	int numOfChunkes = (int) Math.ceil((fileSize + 1)/(double) CHUNK_SIZE);
   	int numofChunkesInRange = (int) Math.ceil(numOfChunkes/(double) numberOfWorkers);
   	Range[] workersRange = new Range[numberOfWorkers];
   	long i = -1;

   	for (int j = 0; j < workersRange.length; j++) {
   		long endOfRange = Math.min((j+1) * (numofChunkesInRange * CHUNK_SIZE) - 1, fileSize);
			workersRange[j] = new Range(i + 1, endOfRange);
			i = endOfRange;
		}
   		return workersRange;
	}
   
   /**
    * compute and return the file size using a HEAD request.
    * @param urlToDownloadFrom
    * @return the file size
    */
	private static long getFileSize(URL urlToDownloadFrom) {
	   	long resultSize = 0;
	   	HttpURLConnection connection = null;
	   	
	   	try {
	   		connection = (HttpURLConnection)urlToDownloadFrom.openConnection();
	   		connection.setRequestMethod("HEAD");
	   		resultSize = connection.getContentLengthLong();
	   	} catch (IOException e) {
	   		e.printStackTrace();
	   	}
	   	finally {
	   		if (connection != null)
	   			connection.disconnect();
	   	}
	   	return resultSize;   	
   }	
   
   /**
    * check if the metadata file exist and returns it or  create a new one,
    * @param url
    * @param numOfRanges
    * @param fileSize
    * @return
    */
   private static DownloadableMetadata getExistOrNewMetadata(String url, int numOfRanges, long fileSize) {
	   	String metadataFileName = DownloadableMetadata.getMetadataName(DownloadableMetadata.getName(url));
	   	
	   	File metadataFile = new File(metadataFileName);
	   	DownloadableMetadata resultMetadataObj = null;
	   	
	   	if (metadataFile.exists()) {
	   		try {
	   			FileInputStream fileIn = new FileInputStream(metadataFile);
	   			ObjectInputStream in = new ObjectInputStream(fileIn);
	   			resultMetadataObj = (DownloadableMetadata) in.readObject();
	   			in.close();
	   			fileIn.close();
	
	   		} catch (IOException e) {
	   			e.printStackTrace();
	   		} catch (ClassNotFoundException e) {
	   			System.out.println("DownloadableMetadata class not found");
	   			e.printStackTrace();			         
	   		}
	   	} else {
	   		resultMetadataObj = new DownloadableMetadata(url, numOfRanges);
	   	}  	
	   	return resultMetadataObj;
   }
   
}

