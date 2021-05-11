package com.jltorroba.legacyblob;


public class StorageServiceFactory {

    private static StorageService service = new DefaultService();

    public static StorageService load () {
        return service;
    }

    public static void setService (StorageService service) {
        StorageServiceFactory.service = service;
    }
    private static class DefaultService implements StorageService {

        @Override
        public byte[] save (byte[] data) {
            return data;
        }

        @Override
        public byte[] update (byte[] source, byte[] data) {
            return data;
        }

        @Override
        public byte[] get (byte[] source) {
            return source;
        }

    }

}
