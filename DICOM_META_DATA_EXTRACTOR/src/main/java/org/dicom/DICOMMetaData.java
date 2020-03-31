// Package Name
package org.dicom;

//Imports external to the java library

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.TagFromName;
import org.bson.Document;

import java.io.File;
import java.lang.reflect.Field;

//Imports that use the java library

public class DICOMMetaData {
    // This variable is used to read/write DICOM images
    private static AttributeList DICOM_READER = new AttributeList();
    // This String array Stores all the tag names related to the sample DICOM image files
    private static String[] TAG_NAMES_SAMPLE = {"File Meta Information Group Length", "Media Storage SOP Class", "Media Storage SOP Instance", "Transfer Syntax UID ", "Implementation Class UI", "Implementation Version", "Source Application Entry", "Identifying Group Length", "Image Type", "SOP Class UID", "SOP Instance UID", "Modality", "Conversion Type", "Patient Group Length", "Patient Name", "Patient ID", "Relationship Group Length", "Study Instance UID", "Series Instance UID", "Acquisition Number", "Instance Number", "Image Presentation Group", "Sample Per Pixel", "Photometric Interpretation", "Rows", "Columns", "Bits Allocated", "Bits Stored", "High Bit", "Pixel Representation", "Pixel Data Group Length"};
    // This String array stores all the tag names related to the brain DICOM image files
    private static String[] TAG_NAMES_BRAIN = {"File Meta Information Group Length", "File Meta Info Version", "Media Storage SOP Class UID", "Media Storage SOP Instance UID", "Transfer Syntax UID", "Implementation Class UID", "Implementation Version Name", "Source Application Entity Title", "Specific Character Set", "Image Type", "SOP Class UID", "SOP Instance UID", "Study Date", "Series Date", "Acquisition Date", "Content Date", "Study Time", "Series Time", "Acquisition Time", "Content Time", "-", "Modality", "Manufacturer", "-", "-", "-", "Station Name", "Study Description", "Series Description", "Manufacturers Model Name", "Start Of Item", "Patient Group Length", "Patient Name", "Patient ID", "Patient Birth Date", "Patient Sex", "Patient Age", "Patient Size", "Patient Weight", "Patient Comments", "Acquisition Group Length", "Scanning Sequence", "Sequence Variant", "Scan Options", "MR Acquisition Type", "Sequence Name", "Angio Flag", "Slice Thickness", "Repetition Time", "Echo Time", "Number Of Averages", "Imaging Frequency", "Imaged Nucleus", "Echo Number", "Magnetic Field Strength", "Spacing Between Slices", "Number Of Phase Encoding Steps", "Echo Train Length", "Percent Sampling", "Percent Phase Field Of View", "Pixel Bandwidth", "Device Serial Number", "Software Version", "Protocol Name", "Date Of Last Calibration", "Time Of Last Calibration", "Transmit Coil Name", "Acquisition Matrix", "In Plane Phase Encoding Direction", "Flip Angle", "Variable Flip Angle", "SAR", "DB-Dt", "Patient Position", "Relationship Group Length", "Study Instance UID", "Series Instance UID", "Study ID", "Series Number", "Acquisition Number", "Instance Number", "Image Position Patient", "Image Orientation Patient", "Frame Of Reference UID", "-", "Slice Location", "Image Presentation Group Length", "Samples Per Pixel", "Photometric Interpretation", "Rows", "Columns", "Pixel Spacing", "Bits Allocated", "Bits Stored", "High Bit", "Pixel Representation", "Smallest Image Pixel ", "Largest Image Pixel ", "Window Center", "Window Width", "Window Center And Width Explanation", "Lower Range Of Pixels 1e", "Lower Range Of Pixels 1f", "Upper Range Of Pixels 2", "Study Group Length", "Study Status ID", "Requested Procedure Description", "Performed Procedure Step Start Date", "Performed Procedure Step Start Time", "Performed Procedure Step ID", "Performed Procedure Step Description", "Pixel Data Group Length", "Pixel Data"};
    // This array is used to get all the tags given as public instance variable in the TagFromNameClass
    private Field[] DICOMtags = TagFromName.class.getFields();
    // Connection to the MongoDB Server
    private MongoClient mongoClient = MongoClients.create(
            "mongodb+srv://dicom:12345@dicom-vc06h.mongodb.net/test?retryWrites=true&w=majority");

    // This function is used put all the tags to the database
    public void postTags() {
        // This variable is used to access the mongodb databses collection to insert data
        MongoCollection<Document> tags = mongoClient.getDatabase("DICOM").getCollection("Tags");
        // This variable is used in order to create a MongoDB document
        Document document = new Document();
        int count = 1;
        for (Field i : DICOMtags) {
            try {
                // Get the tag names from the field
                String tagName = i.toString().substring(83);
                // Append it to the document
                document.append(String.valueOf(count), tagName);

            } catch (Exception e) {
                System.out.println(e.toString());
            }
            count += 1;
        }
        // Insert it to the database
        tags.insertOne(document);

    }

    public void iterateThroughDatasets(String pathName) throws Exception {
        // This variable is used to access the mongodb databses collection to insert data
        MongoCollection<Document> imageDetails = mongoClient.getDatabase("DICOM").getCollection("ImageDetails");
        File path = new File(pathName);
        // Get all the file names
        File[] files = path.listFiles();
        int c = 0;
        // Iterate through all the files
        for (int i = 0; i < files.length; i++) {
            // This variable is used in order to create a MongoDB document
            Document document = new Document();
            // Check if the particular file is a DICOM file
            if (files[i].isFile() && (getFileExtension(files[i]).equalsIgnoreCase("dcm") || getFileExtension(files[i]).equals("DICOM"))) { //this line weeds out other directories/folders
                // Read the file using the attribute list provided by pixelmed
                DICOM_READER.read(files[i]);
                // Append the file type
                document.append("Image Name", files[i].toString().substring(50));
                for (AttributeTag attributeTag : DICOM_READER.keySet()) {
                    try {
                        // Check the filetype
                        if (files[i].toString().substring(50).startsWith("1")) {
                            // Append the tags with the images details
                            document.append(TAG_NAMES_BRAIN[c], Attribute.getDelimitedStringValuesOrDefault(DICOM_READER, attributeTag, "TAG NOT FOUND"));
                        } else {
                            // Append the tags with the images details
                            document.append(TAG_NAMES_SAMPLE[c], Attribute.getDelimitedStringValuesOrDefault(DICOM_READER, attributeTag, "TAG NOT FOUND"));
                        }
                    } catch (Exception ex) {
                        continue;
                    }
                    c += 1;
                }
                // Insert the document
                imageDetails.insertOne(document);
                // Clear the file to read a new file
                DICOM_READER.clear();
                c = 0;
            }
        }
        System.out.println("Successfully Added DICOM DETAILS to the database ! ");
    }

    // Function to get the file extension of a file
    private String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }


}
