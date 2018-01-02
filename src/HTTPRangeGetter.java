import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * A runnable class which downloads a given url.
 * It reads CHUNK_SIZE at a time and writes it into a BlockingQueue.
 * It supports downloading a range of data, and limiting the download rate using a token bucket.
 */
public class HTTPRangeGetter implements Runnable {
    static final int CHUNK_SIZE = 4096;
    private static final int CONNECT_TIMEOUT = 500;
    private static final int READ_TIMEOUT = 2000;
    private final String url;
    private final Range range;
    private final BlockingQueue<Chunk> outQueue;
    private TokenBucket tokenBucket;

    HTTPRangeGetter(
            String url,
            Range range,
            BlockingQueue<Chunk> outQueue,
            TokenBucket tokenBucket) {
        this.url = url;
        this.range = range;
        this.outQueue = outQueue;
        this.tokenBucket = tokenBucket;
    }

    private void downloadRange() throws IOException, InterruptedException {
        //TODO
    }

    @Override
    public void run() {
        try {
            this.downloadRange();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            //TODO
        }
    }
}
