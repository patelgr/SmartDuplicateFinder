package net.hiralpatel.enums;

public enum HashingTechnique {
    BASE, // SKIP
    CRC32,    // Fast but not cryptographically secure
    ADLER32,  // Similar to CRC32 in terms of use cases and efficiency
    MD5,      // Faster but cryptographically broken
    SHA1,     // Faster than SHA-2 family but less secure
    SHA256,   // Balance between speed and security in SHA-2 family
    SHA384,   // Larger output size than SHA-256, more secure but slower
    SHA512,   // Largest output size in SHA-2, secure but slower than SHA-256 and SHA-384
    SHA3,     // Designed as an alternative to SHA-2, secure and efficient
    BLAKE2b,  // Faster than SHA-3, very efficient
    BLAKE3;   // Currently one of the most efficient and secure hashing algorithms

    public HashingTechnique nextLevel() {
        int nextOrdinal = this.ordinal() + 1;
        if (nextOrdinal >= HashingTechnique.values().length) {
            return null; // or return this; to indicate there is no higher level
        }
        return HashingTechnique.values()[nextOrdinal];
    }
}
