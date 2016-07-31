# EntropyPool2
The pool size can be configured, and should typically be around 512kbit (64kiB). It's always a multiple of 8 bits.

# Dependencies
* [BouncyCastle 1.54](http://www.bouncycastle.org/latest_releases.html)

# Features
* Entropy can be [injected](http://github.com/MarkJeronimus/EntropyPool2/wiki/Inject) from files (assumes 1 bit entropy per byte, unless specified).
* Entropy can be [extracted](http://github.com/MarkJeronimus/EntropyPool2/wiki/Extract) in multiples of 8 bits until entropy is exhausted. The entire pool is used to generate the extracted bytes after which it's [mixed](http://github.com/MarkJeronimus/EntropyPool2/wiki/Mix). 
* After every inject or extract operation, the pool is mixed. Mixing doesn't increase the entropy of the pool. Mixing consists of:
  * [Whitening](http://github.com/MarkJeronimus/EntropyPool2/wiki/Whiten) using an SP 800-90A generator
  * [Permutation](http://github.com/MarkJeronimus/EntropyPool2/wiki/Permute) of all bytes (using a SecureRandom)
  * RC4-inspired [rehashing](http://github.com/MarkJeronimus/EntropyPool2/wiki/Rehash)
