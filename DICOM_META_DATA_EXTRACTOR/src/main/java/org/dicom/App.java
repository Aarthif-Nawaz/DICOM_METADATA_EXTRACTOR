//Authour - Aarthif Nawaz
// Purpose - To Decode DICOM Image File tags and store it into the database

// Package Name
package org.dicom;

//Imports related to the java library

import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static void main(String[] args) throws Exception {
        // To get rid of all the logging that come from the mongodb database
        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);
        // Creating a new object Of DICOMMetaData class
        DICOMMetaData DICOM_DECODER = new DICOMMetaData();
        // This object calls the postTags method to submit all the tags to the Database
        DICOM_DECODER.postTags();
        // This object is created to iterate through all the files and decode the details of the file and save it to the database
        DICOM_DECODER.iterateThroughDatasets("C:/Users/Aarthif/DICOM_META_DATA_EXTRACTOR/Images");


    }
}
