package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author peterlee on 12/2/15.
 */
public class Commit implements Serializable {

    /** The commit SHA. */
    private String _commitSHA;
    /** The parent SHA. */
    private String _parentSHA;
    /** The commit msg. */
    private String _commitMsg;
    /** The timestamp. */
    private String _timestamp;
    /** The access directory. */
    private String accessDir;
    /** The mapped files. */
    private Map<String, String> mappedFiles;
    /** The branch name. */
    private String _branchName;

    /** The commit constructor.
     * @param commitMsg is the message with the commit.
     * @param timestamp is the date and time.
     * @param commitSHA is the commit SHA.
     * @param parentSHA is the parent SHA.
     * @param trackedFiles is the tracked Files.
     * @param branchName is the name of the branch.
     * */
    public Commit(String commitMsg, String timestamp, String commitSHA,
                  String parentSHA, Map<String, String> trackedFiles,
                  String branchName) {
        _timestamp = timestamp;
        _commitMsg = commitMsg;
        _commitSHA = commitSHA;
        _parentSHA = parentSHA;
        _branchName = branchName;
        if (trackedFiles != null) {
            mappedFiles = new HashMap<>();
            mappedFiles.putAll(trackedFiles);
            accessDir = ".gitlet/" + "commits/" + _commitSHA;
            try {
                saveFiles(accessDir);
            } catch (IOException e) {
                System.out.println("Cannot commit this file.");
            }
        } else {
            accessDir = null;
        }
    }

    /** getter for commit SHA.
     * @return the commit SHA.
     * */
    public String getCommitSHA() {
        return _commitSHA;
    }

    /** getter for parent sha.
     * @return the parent SHA.
     * */
    public String getParentSHA() {
        return _parentSHA;
    }

    /** getter for commit msg.
     * @return the commit msg.
     * */
    public String getCommitMsg() {
        return _commitMsg;
    }

    /** getter for timestamp.
     * @return the timestamp.
     * */
    public String getTimestamp() {
        return _timestamp;
    }
    /** getter for mapped files.
     * @return the mapped files.
     * */
    public Map<String, String> getMappedFiles() {
        return mappedFiles;
    }

    /** getter for branch name.
     *
     * @return string name branch.
     */
    public String getBranchName() {
        return _branchName;
    }

    /** save the files.
     * @param directory is the directory.
     * */
    private void saveFiles(String directory) throws IOException {
        Set<String> trackedPaths = mappedFiles.keySet();
        for (String files : trackedPaths) {
            File destination = new File(directory + "/" + files);
            File source = new File(files);
            if (destination.getParentFile() != null) {
                createParentFile(destination);
            }
            byte[] in = Utils.readContents(source);
            Utils.writeContents(destination, in);
            mappedFiles.put(files, destination.toString());
        }
    }

    /** create the parent file.
     * @param destination is the destination directory.
     * */
    private void createParentFile(File destination) {
        destination.getParentFile().mkdirs();
    }

}
