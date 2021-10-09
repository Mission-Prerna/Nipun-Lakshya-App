package org.odk.collect.android.contracts;

import org.json.JSONArray;
import org.json.JSONException;
import org.odk.collect.android.application.Collect1;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import timber.log.Timber;

/**
 * Created by Umang Bhola on 11/5/20.
 * Samagra- Transforming Governance
 */
public class CSVHelper {

    public enum BuildFailureType {
        FILE_DOES_NOT_EXIST,
        INVALID_KEYS,
        IO_EXCEPTION,
        COPY_NOT_FOUND
    }

    /**
     * The method is used to get the name of media directories wrt. all forms downloaded, which contained the file which is to be edited as per the received data.
     *
     * @param referenceFileName - {{@link String}} Name of the file which is to be checked for presence in Form Media directories.
     * @return {{@link ArrayList <String>}} List of names of media directories for all the forms containing to be modified reference File.
     */
    public static ArrayList<String> fetchFormMediaDirectoriesWithMedia(String referenceFileName) {
        File dir = new File(Collect1.getInstance().getStoragePathProvider().getDirPath(StorageSubdirectory.FORMS));
        File[] files;
        ArrayList<String> directoriesNames = new ArrayList<>();
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        files = dir.listFiles(fileFilter);
        if (files.length == 0) {
            Timber.d("ODK Forms with media have not been downloaded. No such Directory exists.");
            return directoriesNames;
        } else {
            for (File direcName : files) {
                if (direcName.toString().contains("media")) {
                    directoriesNames.add(direcName.toString());
                }
            }
        }
        if (directoriesNames.size() > 0) {
            return fetchValidDirectories(directoriesNames, referenceFileName);
        } else
            return directoriesNames;
    }


    /**
     * This is the abstracted method that will be called by the downloading class to read the form sample ODKs and edit them accordingly. Returns a listener telling
     * if the operation is done or not and also the type of error.
     *
     * @param csvBuildStatusListener - {{@link CSVBuildStatusListener}} CSV Build Operation Listener
     * @param mediaDirectoriesNames  - {{@link ArrayList<String>}} List of the forms' names for which the operation is to be done. (Given that same format of CSV is needed for all the forms), just the path is different.
     * @param studentCSVData              - {{@link JSONArray}} Data to be inserted into the final CSVs, it ideally should contain the data for the keys to be entered.
     * @param mediaFileName          -{{@link String}} Media File to be updated
     */
   public static void buildCSVForODK1(CSVBuildStatusListener csvBuildStatusListener, ArrayList<String> mediaDirectoriesNames,
                               ArrayList<ArrayList<String>> studentCSVData, String mediaFileName,  ArrayList<ArrayList<String>> teacherCSVData) {
        if(mediaDirectoriesNames.size()==0){
            return;
        }
        ArrayList<ArrayList<String>> result = new ArrayList<>(); //List of list of Strings, containing sample csv downloaded.
        CSVReader reader;
        boolean fileAvailability = checkFilesAvailability(mediaDirectoriesNames, mediaFileName);
        if (!fileAvailability) {
            Exception exception = new Exception("Copy of File could not be found in some directory ");
            csvBuildStatusListener.onFailure(exception, BuildFailureType.COPY_NOT_FOUND);
            return;
        }
       String fileDir = Collect1.getInstance().getStoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + File.separator + mediaDirectoriesNames.get(0) + File.separator + mediaFileName;
        try {
            File file = new File(fileDir);
            reader = new CSVReader(new FileReader(file));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                result.add(new ArrayList<>(Arrays.asList(nextLine)));
            }
            Timber.d("Reference CSV from ODK Media Folder has been parsed successfully");
            saveCSV(studentCSVData, csvBuildStatusListener, mediaDirectoriesNames, mediaFileName, teacherCSVData);
            readCsv(mediaDirectoriesNames, mediaFileName);
            Timber.d("Read and write CSV Operation has been successful.");
        } catch (IOException ioException) {

            csvBuildStatusListener.onFailure(ioException, BuildFailureType.IO_EXCEPTION);


        }
    }


    /**
     * @param directories       {@link ArrayList<String>} List of names of media directories for the all the which may/may not be containing to be modified reference File.
     * @param referenceFileName - {{@link String}} Name of the file which is to be checked for presence in Form Media directories.
     * @return {@link ArrayList<String>} List of names of media directories for all the forms containing to be modified reference File.
     */
    private static ArrayList<String> fetchValidDirectories(ArrayList<String> directories, String referenceFileName) {
        ArrayList<String> directoriesAbsoluteName = new ArrayList<>();
        ArrayList<String> directoriesNames = new ArrayList<>(directories);
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        };        for (String directoryName : directories) {
            File dir = new File(directoryName);
            File[] files;
            files = dir.listFiles(fileFilter);
            if (files.length == 0) {
                directoriesNames.remove(directoryName);
            } else {
                boolean isReferenceFilePresent = false;
                for (File filename : files) {
                    if (filename.getName().contains(referenceFileName)) {
                        isReferenceFilePresent = true;
                    }
                }
                if (!isReferenceFilePresent) directoriesNames.remove(directoryName);
            }
        }
        if (directoriesNames.size() > 0) {
            for (String directoryName : directoriesNames) {
                File dir = new File(directoryName);
                directoriesAbsoluteName.add(dir.getName());
            }
            Timber.d("Reference Media File " + referenceFileName + " is contained in directories mentioned ahead:- " + directoriesAbsoluteName);
        } else {
            Timber.d("None of the checked Media Directories contained the file which was expected");
        }
        return directoriesAbsoluteName;
    }

    /**
     * This is the abstracted method that will be called by the downloading class to read the form sample ODKs and edit them accordingly. Returns a listener telling
     * if the operation is done or not and also the type of error.
     *
     * @param csvBuildStatusListener - {{@link CSVBuildStatusListener}} CSV Build Operation Listener
     * @param mediaDirectoriesNames  - {{@link ArrayList<String>}} List of the forms' names for which the operation is to be done. (Given that same format of CSV is needed for all the forms), just the path is different.
     * @param inputData              - {{@link JSONArray}} Data to be inserted into the final CSVs, it ideally should contain the data for the keys to be entered.
     * @param mediaFileName          -{{@link String}} Media File to be updated
     */
    public static void buildCSVForODK(CSVBuildStatusListener csvBuildStatusListener, ArrayList<String> mediaDirectoriesNames,
                               JSONArray inputData, String mediaFileName) {
//        ArrayList<ArrayList<String>> result = new ArrayList<>(); //List of list of Strings, containing sample csv downloaded.
//        CSVReader reader;
//        String listTitle;
//        ArrayList<String> titleRow;
//
//        boolean fileAvailability = checkFilesAvailability(mediaDirectoriesNames, mediaFileName);
//        if (!fileAvailability) {
//            Exception exception = new Exception("Copy of File could not be found in some directory ");
//            csvBuildStatusListener.onFailure(exception, BuildFailureType.COPY_NOT_FOUND);
//            return;
//        }
//        String fileDir = Collect.FORMS_PATH + File.separator + mediaDirectoriesNames.get(0) + File.separator + mediaFileName;
//        try {
//            File file = new File(fileDir);
//            //parsing a CSV file into CSVReader class constructor
//            reader = new CSVReader(new FileReader(file));
//            String[] nextLine;
//            while ((nextLine = reader.readNext()) != null) {
//                result.add(new ArrayList<>(Arrays.asList(nextLine)));
//                // nextLine[] is an array of values from the line
//            }
//            Timber.d("Reference CSV from ODK Media Folder has been parsed successfully");
//            listTitle = result.get(1).get(0);
//            titleRow = result.get(0);
//            Set<String> referenceKeySet = fetchReferenceKeys(result.get(0));
//            if (!validateReferenceKeys(referenceKeySet, inputData)) {
//                csvBuildStatusListener.onFailure(new Exception("Reference Keys are not found"), BuildFailureType.INVALID_KEYS);
//                return;
//            }
//
//            ArrayList<ArrayList<String>> finalCSVData = getCSVData(referenceKeySet, inputData, listTitle, titleRow);
//            saveCSV(finalCSVData, csvBuildStatusListener, mediaDirectoriesNames, mediaFileName);
//            readCsv(mediaDirectoriesNames, mediaFileName);
//            Timber.d("Read and write CSV Operation has been successful.");
//        } catch (IOException | JSONException ioException) {
//            if (ioException instanceof JSONException)
//                csvBuildStatusListener.onFailure(ioException, BuildFailureType.IO_EXCEPTION);
//            else {
//                csvBuildStatusListener.onFailure(ioException, BuildFailureType.IO_EXCEPTION);
//
//            }
//
//        }
    }

    /**
     *
     * @param mediaFileName- {{@link String} Media File name.
     * @param mediaDirectoriesNames - {{@link ArrayList<String>} Media folder name list
     * @return - Return flag if copy operation is successful or nor
     */
    private static boolean checkFilesAvailability(ArrayList<String> mediaDirectoriesNames, String mediaFileName) {
        ArrayList<Boolean> flags = new ArrayList<>();
        for(String mediaDir : mediaDirectoriesNames){
            String fileDir = Collect1.getInstance().getStoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + File.separator + mediaDir + File.separator + mediaFileName;
            File file = new File(fileDir);
            if (!file.exists()) {
                Timber.e("Media file doesn't exist as expected in the folder..... Replacing it with copy.");
                boolean flag = copySourceFile(mediaFileName, Collect1.getInstance().getStoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + File.separator + mediaDir + File.separator);
               flags.add(flag);
            }else{
                flags.add(true);
            }
        }

        for(Boolean flag:flags){
            if(!flag)
            return false;
        }
        return true;
    }

    /**
     *
     * @param mediaFileName- {{@link String} Media File name.
     * @param destinationFolder - {{@link String} Media folder name.
     * @return - Return flag if copy operation is successful or nor
     */
    private static boolean copySourceFile(String mediaFileName, String destinationFolder) {
        String sourcePath = Collect1.getInstance().getStoragePathProvider().getStorageRootDirPath() + "/"+mediaFileName;
        File source = new File(sourcePath);
        if(source.exists()) {
            String destinationPath = destinationFolder + mediaFileName;
            File destination = new File(destinationPath);
            try {
                FileUtils.copyFile(source, destination);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return  true;
        }

        return false;
    }

    /**
     * Write data into the reference CSVs in the Media Folder
     *
     * @param csvBuildStatusListener - {{@link CSVBuildStatusListener}} CSV Build Operation Listener
     * @param mediaDirectoriesNames  - {{@link ArrayList<String>}} List of the forms' names for which the operation is to be done. (Given that same format of CSV is needed for all the forms), just the path is different.
     * @param mediaFileName          {{@link String}} Media File to be updated with extension
     */
    private static void saveCSV(ArrayList<ArrayList<String>> studentCSVData, CSVBuildStatusListener csvBuildStatusListener,
                                ArrayList<String> mediaDirectoriesNames, String mediaFileName, ArrayList<ArrayList<String>> teacherCSVData) {
        CSVWriter writer;
        try {
            for (String form : mediaDirectoriesNames) {
                String formName = form.toLowerCase();
                String student = "student";
                String teacher = "teacher";
                String COVID = "covid";

                    String fileDir = Collect1.getInstance().getStoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + File.separator + mediaDirectoriesNames.get(0) + File.separator + mediaFileName;
                    File outputFile = new File(fileDir);
                    writer = new CSVWriter(new FileWriter(outputFile), ',');
                    for (ArrayList<String> list : teacherCSVData) {
                        String[] stockArr = new String[list.size()];
                        stockArr = list.toArray(stockArr);
                        writer.writeNext(stockArr);
                    }
                    writer.close();
                    storeCSVFileCopy(teacherCSVData, mediaFileName, csvBuildStatusListener);
                    csvBuildStatusListener.onSuccess();

            }
        } catch (IOException ioException) {
            csvBuildStatusListener.onFailure(ioException, BuildFailureType.IO_EXCEPTION);
        }

    }

    private static void storeCSVFileCopy(ArrayList<ArrayList<String>> finalCSVData, String mediaFileName, CSVBuildStatusListener csvBuildStatusListener) {
        CSVWriter writer;
        try {
            String fileDir =  Collect1.getInstance().getStoragePathProvider().getStorageRootDirPath() + "/"+ mediaFileName;
            File outputFile = new File(fileDir);
            writer = new CSVWriter(new FileWriter(outputFile), ',');
            for (ArrayList<String> list : finalCSVData) {
                String[] stockArr = new String[list.size()];
                stockArr = list.toArray(stockArr);
                writer.writeNext(stockArr);
            }
            writer.close();

        } catch (IOException ioException) {
            csvBuildStatusListener.onFailure(ioException, BuildFailureType.IO_EXCEPTION);
        }
    }

    /**
     * This is for test purpose to check CSV has been updated correctly or not, can be removed later.
     *
     * @param mediaDirectoriesNames {{@link ArrayList<String>}} List of Form Names.
     * @param mediaFileName         {{@link String}} Media File to be updated with extension
     */
    private static void readCsv(ArrayList<String> mediaDirectoriesNames, String mediaFileName) {
        String fileDir = Collect1.getInstance().getStoragePathProvider().getDirPath(StorageSubdirectory.FORMS)
                 + File.separator + mediaDirectoriesNames.get(0) + File.separator + mediaFileName;
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        CSVReader reader;
        try {
            File file = new File(fileDir);
            //parsing a CSV file into CSVReader class constructor
            reader = new CSVReader(new FileReader(file));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                result.add(new ArrayList<>(Arrays.asList(nextLine)));
                // nextLine[] is an array of values from the line
            }
            Timber.d("Final CSV has top content as follows: %s", result.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<ArrayList<String>> getCSVData(Set<String> referenceKeySet, JSONArray students, String title, ArrayList<String> firstRow) throws JSONException {
        ArrayList<ArrayList<String>> finalResult = new ArrayList<>();
        finalResult.add(firstRow);
        for (int i = 0; i < students.length(); i++) {
            ArrayList<String> newList = new ArrayList<>();
            newList.add(title);
            for (String s : referenceKeySet) {
                if (students.getJSONObject(i).get(s) != null) {
                    if (students.getJSONObject(i).get(s).toString().contains(" ")) {
                        String key = students.getJSONObject(i).get(s).toString().replace(" ", "_");
                        newList.add(key);
                        newList.add(Objects.requireNonNull(students.getJSONObject(i).get(s)).toString());
                    } else {
                        newList.add(Objects.requireNonNull(students.getJSONObject(i).get(s)).toString());
                        newList.add(Objects.requireNonNull(students.getJSONObject(i).get(s)).toString());
                    }
                }

            }
            finalResult.add(newList);
        }

        return finalResult;
    }

    private static boolean validateReferenceKeys(Set<String> referenceKeySet, JSONArray students) throws JSONException {
        boolean flag = false;
        for (String s : referenceKeySet) {
            flag = students.getJSONObject(0).has(s);
        }
        return flag;
    }


    /**
     * Fetch the to-be-mapped keys from the Reference CSV Downloaded.
     *
     * @param downloadedData {{@link ArrayList<String>}} - Reference CSV's Key  List (Need to be parsed)
     * @return - {@link Set} Set of Strings (Unique) to be picked up later.
     */
    private static Set<String> fetchReferenceKeys(ArrayList<String> downloadedData) {
        Set<String> result = new LinkedHashSet<>();
        for (int i = 1; i < downloadedData.size(); i++) {
            if (downloadedData.get(i).contains("_")) {
                result.add(downloadedData.get(i).split("_")[0]);
            } else {
                result.add(downloadedData.get(i));
            }
        }
        return result;
    }
}

