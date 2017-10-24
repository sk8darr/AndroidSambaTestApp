package com.angiellorivas.androidsambatestapp;

/**
 * @author sk8 on 24/10/17.
 */

public abstract class ServiceEvent {

    public static class OneFileDownloaded{
        private String filename;
        public OneFileDownloaded (String filename){
            this.filename = filename;
        }

        String getFilename() {
            return filename;
        }
    }
    public static class OneFileUploaded{
        private String filename;
        public OneFileUploaded (String filename){
            this.filename = filename;
        }

        String getFilename() {
            return filename;
        }
    }
    public static class TotalFiles{
        private int total;

        public TotalFiles(int total) {
            this.total = total;
        }
        int getTotal() {
            return total;
        }
    }
}
