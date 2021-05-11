package com.jltorroba.legacyblob;


public interface StorageService {

    byte[] save (byte[] data);

    byte[] update (byte[] source, byte[] data);

    byte[] get (byte[] source);

}
