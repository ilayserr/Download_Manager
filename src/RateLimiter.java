/**
 * A token bucket based rate-limiter.
 *
 * This class should implement a "soft" rate limiter by adding maxBytesPerSecond tokens to the bucket every second,
 * or a "hard" rate limiter by resetting the bucket to maxBytesPerSecond tokens every second.
 */

public class RateLimiter implements Runnable {
    private final TokenBucket tokenBucket;
    private final long maxBytesPerSecond;

    RateLimiter(TokenBucket tokenBucket, Long maxBytesPerSecond) {
        this.tokenBucket = tokenBucket;
        this.maxBytesPerSecond = maxBytesPerSecond;
    }

    /**
     * adding maxBytesPerSecond tokens each second.
     * @throws InterruptedException
     */
    private void refill() throws InterruptedException {
    	while (!tokenBucket.terminated()){  		
    		tokenBucket.add(maxBytesPerSecond);
    		Thread.sleep(1000);
    	}
    }
    
    @Override
    public void run() {
    	try {
			refill();
		} catch (InterruptedException e) {
			System.err.println(e.getMessage() + "\nDownload failed");
        	System.exit(-1);
		}
    }
}
