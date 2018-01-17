# Ilay Serr 
# ilayserr@gmail.com


idcDm - The main program, check input from the user and:
Initiate the file's metadata, and iterate over missing ranges. For each:
1. Setup the Queue, TokenBucket, DownloadableMetadata, FileWriter, RateLimiter, and a pool of HTTPRangeGetters
2. Join the HTTPRangeGetters, send finish marker to the Queue and terminate the TokenBucket
3. Join the FileWriter and RateLimiter

DownloadableMetaData - 
Describes a file's metadata: URL, file name, size, and which parts already downloaded to disk.
It implements serializable.

RateLimiter - 
The class runs on different thread that add to the token bucket maxbyteperseconds token every second.

TokenBucket - 
This thread-safe bucket should support the following methods:
take(n): remove n tokens from the bucket (blocks until n tokens are available and taken)
add(n): add n tokens to the bucket (to allow "soft" rate limiting)
terminate(): mark the bucket as terminated (used to communicate between threads)
terminated(): return true if the bucket is terminated, false otherwise

HTTPRangeGetter - 
A runnable class which downloads a given url.
It reads CHUNK_SIZE at a time and writes it into a BlockingQueue.
It supports downloading a range of data, and limiting the download rate using a token bucket.

FileWriter - 
This class takes chunks from the queue, writes them to disk and updates the file's metadata.

Chunk - 
Define a chunk.
Contains an offset, bytes of data, and size

Range - 
Describes a simple range, with a start, an end, and a length

