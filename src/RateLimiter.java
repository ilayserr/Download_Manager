/**
 * A token bucket based rate-limiter.
 *
 * This class should implement a "soft" rate limiter by adding maxBytesPerSecond tokens to the bucket every second,
 * or a "hard" rate limiter by resetting the bucket to maxBytesPerSecond tokens every second.
 */

public class RateLimiter implements Runnable {
    private final TokenBucket tokenBucket;
    private final long maxBytesPerSecond;
//    private long lastRefillTimestamp;    

    RateLimiter(TokenBucket tokenBucket, Long maxBytesPerSecond) {
        this.tokenBucket = tokenBucket;
        this.maxBytesPerSecond = maxBytesPerSecond;
//        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    /**
     * adding maxBytesPerSecond tokens each second.
     * @throws InterruptedException
     */
    private void refill() throws InterruptedException {
    	while (!tokenBucket.terminated()){
//	        long currentTimeMillis = System.currentTimeMillis();
//	        long millisecondsPassed = currentTimeMillis - this.lastRefillTimestamp;
//	        if (currentTimeMillis - this.lastRefillTimestamp > 1000) {
//	            //long millisSinceLastRefill = currentTimeMillis - this.lastRefillTimestamp;
//	            tokenBucket.add(maxBytesPerSecond * millisecondsPassed/1000);
//	            this.lastRefillTimestamp = currentTimeMillis;
//	        }
    		tokenBucket.add(maxBytesPerSecond);
    		Thread.sleep(1000);
    	}
    }
    
    @Override
    public void run() {
    	try {
			refill();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}


/****************************/

//
//
//public class RateLimiter implements Runnable {
//    private final TokenBucket tokenBucket;
//    private final long maxBytesPerSecond;
////    private long lastRefillTimestamp;    
//
//    RateLimiter(TokenBucket tokenBucket, Long maxBytesPerSecond) {
//        this.tokenBucket = tokenBucket;
//        this.maxBytesPerSecond = maxBytesPerSecond;
////        this.lastRefillTimestamp = System.currentTimeMillis();
//    }
//
//    /**
//     * adding maxBytesPerSecond tokens each second.
//     * @throws InterruptedException
//     */
//    private void refill() throws InterruptedException {
//    	while (!tokenBucket.terminated()){
////	        long currentTimeMillis = System.currentTimeMillis();
////	        long millisecondsPassed = currentTimeMillis - this.lastRefillTimestamp;
////	        if (currentTimeMillis - this.lastRefillTimestamp > 1000) {
////	            //long millisSinceLastRefill = currentTimeMillis - this.lastRefillTimestamp;
////	            tokenBucket.add(maxBytesPerSecond * millisecondsPassed/1000);
////	            this.lastRefillTimestamp = currentTimeMillis;
////	        }
//    		tokenBucket.add(maxBytesPerSecond);
//    		Thread.sleep(1000);
//    	}
//    }
//    
//    @Override
//    public void run() {
//    	try {
//			refill();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//    }
//}
