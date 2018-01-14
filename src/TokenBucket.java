import java.util.concurrent.atomic.AtomicLong;

/**
 * A Token Bucket (https://en.wikipedia.org/wiki/Token_bucket)
 *
 * This thread-safe bucket should support the following methods:
 *
 * - take(n): remove n tokens from the bucket (blocks until n tokens are available and taken)
 * - set(n): set the bucket to contain n tokens (to allow "hard" rate limiting)
 * - add(n): add n tokens to the bucket (to allow "soft" rate limiting)
 * - terminate(): mark the bucket as terminated (used to communicate between threads)
 * - terminated(): return true if the bucket is terminated, false otherwise
 *
 */

class TokenBucket {
	
   // private final long capacity;
    private final double refillTokenRate;
   // private double availableTokens;
    private AtomicLong  availableTokens;
    //private long lastRefillTimestamp;
    private boolean terminate = false;
    
//    TokenBucket(long capacity, long refillTokens, long refillPeriodMillis) {
//        this.capacity = capacity;
//        this.availableTokens = new AtomicLong();
//        this.refillTokenRate = refillTokens / (double)refillPeriodMillis;
//        //this.lastRefillTimestamp = System.currentTimeMillis();
//    }
    TokenBucket(long maxBytesPerSecond) {
        //this.capacity = capacity;
        this.availableTokens = new AtomicLong();
        this.refillTokenRate = maxBytesPerSecond;
        //this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public void take(long tokens) throws InterruptedException {
//    	refill();
        while (availableTokens.get() < tokens) {
        	Thread.sleep(1000);  ///(long) (refillTokenRate/(tokens - availableTokens.get())*1000));
        	//Thread.sleep((long) (refillTokenRate/(tokens - availableTokens.get())));
        }
        
        availableTokens.getAndAdd(-tokens);
    }
    
    public void set(long tokens) {
    	this.availableTokens.set(tokens);
    }
    
    void add(long tokens) {
    	//this.availableTokens = Math.min(this.capacity, this.availableTokens + tokens);
    	availableTokens.getAndAdd(tokens);
    }
    
    void terminate() {
        this.terminate = true;
    }

    boolean terminated() {
		return this.terminate;
    }
}


//class TokenBucket {
//	
//    private final long capacity;
//    private final double refillTokenRate;
//    private double availableTokens;
//    //private long lastRefillTimestamp;
//    private boolean terminate = false;
//    
//    TokenBucket(long capacity, long refillTokens, long refillPeriodMillis) {
//        this.capacity = capacity;
//        this.availableTokens = capacity;
//        this.refillTokenRate = refillTokens / (double)refillPeriodMillis;
//        //this.lastRefillTimestamp = System.currentTimeMillis();
//    }
//
//    synchronized void take(long tokens) throws InterruptedException {
////    	refill();
//        while (!terminate && availableTokens < tokens) {
//        	Thread.sleep((long) (refillTokenRate/(tokens - availableTokens)*1000));
//        }
//        this.availableTokens -= tokens;
//    }
//    
////    private void refill() {
////        long currentTimeMillis = System.currentTimeMillis();
////        if (currentTimeMillis > this.lastRefillTimestamp) {
////            long millisSinceLastRefill = currentTimeMillis - this.lastRefillTimestamp;
////            add(millisSinceLastRefill * (long)this.refillTokenRate);
////            this.lastRefillTimestamp = currentTimeMillis;
////        }
////    }
//    
//    void set(long tokens) {
//        this.availableTokens = Math.min(this.capacity, tokens);
//    }
//    
//    void add(long tokens) {
//    	this.availableTokens = Math.min(this.capacity, this.availableTokens + tokens);
//    }
//    
//    void terminate() {
//        this.terminate = true;
//    }
//
//    boolean terminated() {
//		return this.terminate;
//    }
//}
