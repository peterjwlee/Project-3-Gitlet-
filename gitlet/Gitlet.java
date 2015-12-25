package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;


/**
 * @author wesleywan and peterlee on 12/5/15.
 */
public class Gitlet implements Serializable {

    /** This is the constructor for the gitlet object. */
    private static Gitlet gitlet;
    /** A map to keep track of all branches.*/
    private Map<String, Branch> branchMap;
    /** A set to keep track of all removed files.*/
    private Set<String> removedFiles;
    /** A set to keep track of all staged files.*/
    private Set<String> stagedFiles;
    /** A map to keep track of all commits.*/
    private Map<String, Commit> commitTree;
    /** The current branch. .*/
    private Branch currentHead;

    /** The gitlet constructor. */
    public Gitlet() {
        branchMap = new HashMap<>();
        removedFiles = new HashSet<>();
        stagedFiles = new HashSet<>();
        commitTree = new HashMap<>();
        currentHead = null;
    }

    /** Helper method to get time stamp of commits.
     * @return the time stamp. */
    public String timestamp() {
        Date currTime = new Date();
        SimpleDateFormat commitTime =
                new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return commitTime.format(currTime);
    }

    /**
     * Init creates a new version control system in the current directory.
     * Starts with one commit (no files, has
     * commitMSG: initial commit) Has a single branch: master, which points
     * to this initial commit and is the current
     * branch. Runtime: Constant Failures: If there's already a directory,
     * then abort and print error. Should not
     * overwrite. Dangerous: No. Returns void
     * <p>
     * Helpers: commit(), branch(), timestamp();
     */
    public void init() {
        if (new File(".gitlet/").exists()) {
            System.out.println("A gitlet version-control system"
                    + " already exists in the current directory.");
        } else {
            new File(".gitlet/").mkdir();
            String timestamp = timestamp();
            String initialCommitSHA = Utils.sha1("initial commit", timestamp,
                    "commit");
            Commit initialCommit = new Commit("initial commit", timestamp,
                    initialCommitSHA, null, new HashMap<>(), "master");
            Branch masterBranch = new Branch("master", initialCommit);
            currentHead = masterBranch;
            branchMap.put("master", masterBranch);
            commitTree.put(initialCommitSHA, initialCommit);
        }
    }

    /**
     * Adds copies of files within the stageSet. case 1: If the file does not
     * exist at all then print out "File does not
     * exist" and return nothing. case 2" If the file already exists in the
     * StageSet then return nothing. helper
     * methods: we need a method that will copy files from one place to another
     * while creating any necessary folders. If
     * files do not exist in certain directories, then git will add those files.
     * helper method: boolean: checks and
     * compares two files. We need a start and a destination. should we replace
     * files if both files are identical in the
     * destination or do we do nothing?
     * @param filePath is the path of the file.
     */
    public void add(String filePath) {
        Path copyFilePath = Paths.get(filePath);
        if (!Files.exists(copyFilePath)) {
            System.out.println("File does not exist.");
            return;
        }
        String nameFileSHA =
                Utils.sha1(Utils.readContents(copyFilePath.toFile()));
        if (removedFiles.contains(filePath)) {
            removedFiles.remove(filePath);
            return;
        }
        for (String trackedFile : currentHead.getMappedFiles().keySet()) {
            String trackedFileSHA =
                    Utils.sha1(Utils
                            .readContents(Paths.get(currentHead
                                    .getMappedFiles().get(trackedFile))
                                    .toFile()));
            if (filePath.equals(trackedFile)
                    && nameFileSHA.equals(trackedFileSHA)) {
                return;

            }
        }
        if (stagedFiles.isEmpty()) {
            stagedFiles.add(filePath);
        } else {
            for (String stageFile : stagedFiles) {
                String stageFileSHA =
                        Utils.sha1(Utils
                                .readContents(Paths.get(stageFile).toFile()));
                if (nameFileSHA.equals(stageFileSHA)) {
                    return;
                }
            }
        }
        stagedFiles.add(filePath);
    }

    /**
     * Commit method: commit method takes a picture of certain files like a
     * snap shot aka it saves the files however, we
     * need to restore those files if we ever need them again* Parameters: A
     * string message? we need our branch class to
     * point where the current commit is at. We need to make sure our commit
     * class works so it can create commits with
     * all the following things: dates, the name of the commit, sha1 values,
     * and the format of the dates and etc we need
     * to clear the stagin area. helper methods: possiblly the same copy
     * method that we used for add helper methods:
     * deleting files or folders with files we can clear files in a hash set
     * or we can just delete files after making a
     * commit because we need to clear the staging area when a commit is made.
     * @param commitMSG the commit msg.
     */
    public void commit(String commitMSG) {
        String parentSHA = currentHead.getCommit().getCommitSHA();
        Map<String, String> currMap = new HashMap<>();
        if (commitMSG.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (stagedFiles.isEmpty() && removedFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Map<String, String> trackedFiles =
                currentHead.getCommit().getMappedFiles();
        for (String file : trackedFiles.keySet()) {
            if (!removedFiles.contains(file)) {
                currMap.put(file, trackedFiles.get(file));
            }
        }
        for (String stageFile : stagedFiles) {
            currMap.put(stageFile, null);
        }
        String timestamp = timestamp();
        String commitSHA = Utils.sha1(commitMSG, timestamp, parentSHA,
                "commit");
        Commit nextCommit = new Commit(commitMSG, timestamp, commitSHA,
                parentSHA, currMap, currentHead.getBranchName());
        commitTree.put(commitSHA, nextCommit);
        currentHead.updateHead(nextCommit);
        branchMap.put(currentHead.getBranchName(), currentHead);
        stagedFiles.clear();
        removedFiles.clear();
    }

    /**
     * Remove:
     * Untracks the file, indicate (within the .gitlet) that it should not
     * be included in the next commit.
     * Remove the file from the working directory if it's currently tracked
     * If staged, unstage but don't remove from working directory unless
     * tracked by current commit
     * Runtime: constant
     * Failure: File not staged or tracked by head commit, print
     * ("No reason to remove the file.")
     * Dangerous: Yes (BUT USE UTILITY METHODS)
     * @param file the file to remove.
     */
    public void remove(String file) {
        if (!stagedFiles.contains(file)
                && currentHead.getMappedFiles().get(file) == null) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (currentHead.getMappedFiles().containsKey(file)) {
            removedFiles.add(file);
            Utils.restrictedDelete(file);
        }
        stagedFiles.remove(file);
    }

    /**
     * log method; return type will be void. When we code log it must have
     * print statements that print the sha1-value
     * ids, the date, the time of commit, and a commit message We need to
     * use our branch class or the head to find where
     * we are currently at. If we know where we are currently at then we can
     * just print out the statements We ned to go
     * through all commits and just print out necessaey commits/ We can use
     * iteration or a while loop to do this job.
     * helper methods: i dont think any are necessary Iteration: if the
     * sha1-values equal to the parentID or CommitID
     * then keep iterating once the sha1-values are equal to where you want
     * to end up at then stop the iteration
     */
    public void log() {
        Commit currHead = currentHead.getCommit();
        String currHeadSHA = currHead.getCommitSHA();
        while (!commitTree.isEmpty()) {
            logHelper(currHead, currHeadSHA);
            currHeadSHA = currHead.getParentSHA();
            currHead = commitTree.get(currHeadSHA);
            if (currHead.getParentSHA() == null) {
                logHelper(currHead, currHeadSHA);
                break;
            }
        }
    }

    /** the helper method for the log.
     *
     * @param currHead the current head commit.
     * @param currHeadSHA the current head sha.
     */
    public void logHelper(Commit currHead, String currHeadSHA) {
        System.out.println("===");
        System.out.println("Commit " + currHeadSHA);
        System.out.println(currHead.getTimestamp());
        System.out.println(currHead.getCommitMsg());
        System.out.println();
    }


    /**
     * global log method: type: void Literally iterate through the hashmap
     * of all the commits and print out the
     * necessary messages. basically find a way to go to the very current
     * commit aka the commit at the end of the linked
     * list Traverse toward the linked list once we hit each commit print
     * the contents then move on to the next commit
     * and once we hit the beginnign of the linked list aka the very first
     * commit then we will end the iteration. Helper
     * methods: none needed We would end the iteration with the sha1-value
     * that is assigned to the very first commit.
     */
    public void globalLog() {
        for (String allCommits : commitTree.keySet()) {
            Commit gCommit = commitTree.get(allCommits);
            System.out.println("===");
            System.out.println("Commit " + gCommit.getCommitSHA());
            System.out.println(gCommit.getTimestamp());
            System.out.println(gCommit.getCommitMsg());
            System.out.println();
        }
    }

    /** find method:
     * We can find the commit based on the commit message aka the string
     * value of that commit.
     * Prints out all the commit ids with the same string messages.
     * We can make an Arraylist of commits or we need to access a list of
     * commits so we can find its string values.
     * if the array list or whatever list we have with commits are not null
     * then we go through some type of iteration.
     * Iteration: for loop that goes through all the commits in the commit
     * array list or the list of commits then print
     * the Commit ID. I think we need sha-1 values for this because the commit
     * ID is essentially the commit ID
     * so we would print out the sha1-values which i think is the commit ID.
     * If the commit does not id we would print out commit does not exists
     * (failure case)
     *
     * @param commitMessage the commit messages.
     * */
    public void find(String commitMessage) {
        List<Commit> listOfCommits = new ArrayList<>();
        for (Commit commit : commitTree.values()) {
            if (commit.getCommitMsg().equals(commitMessage)) {
                listOfCommits.add(commit);
            }
        }
        if (!listOfCommits.isEmpty()) {
            for (Commit commits : listOfCommits) {
                System.out.println(commits.getCommitSHA());
            }
        } else {
            System.out.println("Found no commit with that message.");
        }
    }


    /** status method:
     * essentially just print statements
     * Branch: so basically for loop through all the keys within the hashmap
     * that contains the string and the commits
     * then print out * if one of the commits is the current commit
     * then print out the current branch
     * Stage Files: same ideas as branch make a for loop that goes through
     * all files within stage file hashmap or hash set
     * 1. print out all stage names  in each stage set
     * Removed files: same thing as stage files. USe the remove method because
     * it will contain all remove files
     * 1. Use a for loop that will go through all the remove files and print
     * the files name.
     * Untracked Files: same thing as stage files and branches. Go through
     * the hash set or hash map with all untracked
     * files then print out each and every untracked files
     * Modifications not staged for commmit:
     * not that sure....should discuss more*/
    public void status() {
        System.out.println("=== Branches ===");
        System.out.println("*" + currentHead.getBranchName());
        for (Branch branch : branchMap.values()) {
            if (!currentHead.getBranchName().equals(branch.getBranchName())) {
                System.out.println(branch.getBranchName());
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String stageFile : stagedFiles) {
            System.out.println(stageFile);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String remove : removedFiles) {
            System.out.println(remove);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }



    /**
     *  the branch method.
     *
     * @param branchName the branch name.
     *
     * */
    public void branch(String branchName) {
        if (branchMap.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            Branch splitBranch = new Branch(branchName,
                    currentHead.getCommit());
            branchMap.put(branchName, splitBranch);
        }
    }

    /** the remove branch method.
     *
     * @param currentBranch the current branch to remove.
     */
    public void removeBranch(String currentBranch) {
        if (!branchMap.containsKey(currentBranch)) {
            System.out.println("A branch with that name does not exist.");
        } else if (currentHead.getBranchName().equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branchMap.remove(currentBranch);
        }
    }

    /**
     * Checkout:
     * 1. Takes the version as it exists in the head commit, front of the
     * current branch, and puts it in the
     *    working directory. Overwrites file if there is one. New version of
     *    the file is not staged.
     * 2. Takes the version as it exists in the commit with this id, puts
     * it in the working directory.
     *    Overwrites file if there is one, new version of the file is not
     *    staged.
     * 3. Takes all files in the commit at the head of the given branch,
     * puts them in the working directory,
     *    overwrites if they exist. Once done, given branch becomes current
     *    branch.
     *    Any files present in the current branch and not in the checked out
     *    branch are deleted.
     *    Staging area cleared, unless checked out branch is current branch
     *    Failure cases:
     * 1. If the file does not exist in the previous commit, aborts, printing
     * the error message
     *    File does not exist in that commit.
     * 2. If no commit with the given id exists, print No commit with that id
     * exists. Else, if the file
     *    does not exist in the given commit, print File does not exist in that
     *    commit.
     * 3. If no branch with that name exists, print No such branch exists.
     * If that branch is the current branch,
     *    print No need to checkout the current branch. If a working file is
     *    untracked in the current branch
     *    and would be overwritten by the checkout, print There is an untracked
     *    file in the way; delete it or
     *    add it first. and exit; perform this check before doing anything
     *    else.
     *
     *    @param abbrev the abbreviation for the commit id.
     *    @param fileName the file name to checkout.
     *  */
    public void checkout(String abbrev, String fileName) {
        boolean noCommit = true;
        for (String commitSHA : commitTree.keySet()) {
            if (commitSHA.startsWith(abbrev)) {
                noCommit = false;
                if (!commitTree.get(commitSHA).getMappedFiles()
                        .containsKey(fileName)) {
                    System.out.println("File does not exist in that commit.");
                    return;
                }
                byte[] tmpFile = Utils.readContents(
                        new File(commitTree.get(commitSHA).getMappedFiles()
                                .get(fileName)));
                if (Files.exists(new File(fileName).toPath())) {
                    Utils.writeContents(new File(fileName), tmpFile);
                } else {
                    File restoredFile = new File(fileName);
                    Utils.writeContents(restoredFile, tmpFile);
                }
            }
        }
        if (noCommit) {
            System.out.println("No commit with that id exists.");
        }
    }

    /** the method to checkout files or branches.
     *
     * @param name the name of the file or branch.
     */
    public void checkout(String name) {
        List<String> directoryFiles = Utils.plainFilenamesIn(".");
        if (new File(name).isFile()) {
            if (!currentHead.getMappedFiles().containsKey(name)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            String committedFile = currentHead.getMappedFiles().get(name);
            checkoutHelper(committedFile, name, directoryFiles);
        } else {
            if (!branchMap.containsKey(name)) {
                System.out.println("No such branch exists.");
                return;
            }
            Branch givenBranch = branchMap.get(name);
            if (currentHead == givenBranch) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            for (String file : directoryFiles) {
                if (!currentHead.getMappedFiles().containsKey(file)
                        && !stagedFiles.contains(file)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    return;
                }
            }
            for (String committedFile
                    : givenBranch.getMappedFiles().keySet()) {
                checkoutHelper(givenBranch.getMappedFiles().get(committedFile),
                        committedFile, directoryFiles);
            }
            for (String dirFile : directoryFiles) {
                if (!givenBranch.getMappedFiles().containsKey(dirFile)) {
                    Utils.restrictedDelete(dirFile);
                }
            }
            stagedFiles.clear();
            currentHead = givenBranch;
        }
    }

    /** checkout helper method.
     *
     * @param committedFile the file being committed,
     * @param name the name of the file.
     * @param directoryFiles the files in the working directory.
     */
    public void checkoutHelper(String committedFile, String name,
                               List<String> directoryFiles) {
        byte[] fileContents = Utils.readContents(new File(committedFile));
        if (directoryFiles.contains(name)) {
            Utils.writeContents(new File(name), fileContents);
        } else {
            File tmpFile = new File(name);
            Utils.writeContents(tmpFile, fileContents);
        }
    }


    /** Reset method.
     * Reset:
     * Param: String fileAbbrev
     * Checks out all the files tracked by the given commit
     * Removes tracked files that are not present in the given file
     * Moves the currHead to the commit node
     * Staging area is cleared
     * Basically checkout, but changes the curr branch head
     * Runtime: linear
     * Failure case: If no commit with the given id exists, print
     * (No commit with that id exists.)
     * If a working file is untracked in the current branch and
     * would be overwritten by the reset,
     * print (There is an untracked file in the way; delete it or
     * add it first.)
     * Helper: Need a find abbreviated id helper
     *
     * @param fileAbbrev the abbreviation for the commit ID.
     */
    public void reset(String fileAbbrev) {
        List<String> filesInDirectory = Utils.plainFilenamesIn(".");
        for (String file : filesInDirectory) {
            if (!stagedFiles.contains(file)
                    && !currentHead.getMappedFiles().containsKey(file)) {
                System.out.println("There is an untracked file in the "
                        + "way; delete it or add it first.");
            }
        }
        boolean containsCommit = false;
        for (String commitSHA : commitTree.keySet()) {
            if (commitSHA.startsWith(fileAbbrev)) {
                containsCommit = true;
                currentHead.updateHead(commitTree.get(commitSHA),
                        commitTree.get(commitSHA).getBranchName());
                break;
            }
        }
        if (containsCommit) {
            for (String file : currentHead.getMappedFiles().keySet()) {
                byte[] tmpFile = Utils.readContents(new File(currentHead
                        .getMappedFiles().get(file)));
                if (Files.exists(new File(file).toPath())) {
                    Utils.writeContents(new File(file), tmpFile);
                } else {
                    File restoredFile = new File(file);
                    Utils.writeContents(restoredFile, tmpFile);
                }
            }
            for (String file : filesInDirectory) {
                if (!currentHead.getMappedFiles().containsKey(file)) {
                    Utils.restrictedDelete(file);
                }
            }
            stagedFiles.clear();
        } else {
            System.out.println("No commit with that id exists.");
        }
    }

    /** the merge method.
     *
     * @param givenBranch the branch you try to merge.
     */
    public void merge(String givenBranch) {
        if (!branchMap.containsKey(givenBranch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (!stagedFiles.isEmpty() || !removedFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        List<String> directoryFiles = Utils.plainFilenamesIn(".");
        for (String file : directoryFiles) {
            if (!currentHead.getMappedFiles().containsKey(file)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                return;
            }
        }
        Commit currCommit = currentHead.getCommit();
        Commit gCommit = branchMap.get(givenBranch).getCommit();
        Commit splitPointCommit = findSplitPoint(currCommit, gCommit);
        if (givenBranch.equals(currentHead.getBranchName())) {
            System.out.println("Cannot merge a branch with itself.");
        }
        if (splitPointCommit == gCommit) {
            System.out.println("Given branch is an ancestor of the current "
                    + "branch.");
            return;
        }
        if (splitPointCommit == currCommit) {
            currentHead.updateHead(gCommit);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        boolean isMerged = mergeType(splitPointCommit, gCommit, currCommit);
        if (isMerged) {
            commit("Merged " + currentHead.getBranchName() + " with "
                    + givenBranch + ".");
            removedFiles.clear();
        } else {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** the merge helper method.
     *
     * @param splitPointCommit the split point commit.
     * @param gCommit the given commit.
     * @param currCommit the current commit.
     * @return a boolean
     */
    public boolean mergeType(Commit splitPointCommit, Commit gCommit,
                             Commit currCommit) {
        boolean goodMerge = true;
        Set<String> currCommitFiles = currCommit.getMappedFiles().keySet();
        String currCommitSHA = currCommit.getCommitSHA();
        Set<String> gCommitFiles = gCommit.getMappedFiles().keySet();
        String gCommitSHA = gCommit.getCommitSHA();
        Set<String> splitPointFiles = splitPointCommit.getMappedFiles()
                .keySet();
        String splitCommitSHA = splitPointCommit.getCommitSHA();
        for (String splitFile : splitPointFiles) {
            if (gCommitFiles.contains(splitFile) && currCommitFiles
                    .contains(splitFile)) {
                if (!mergeHelper(splitFile, splitPointCommit.getMappedFiles()
                                .get(splitFile), splitCommitSHA,
                        gCommit.getMappedFiles().get(splitFile), gCommitSHA,
                        currCommit.getMappedFiles().get(splitFile),
                        currCommitSHA)) {
                    goodMerge = false;
                }
            } else if (currCommitFiles.contains(splitFile)
                    && !gCommitFiles.contains(splitFile)) {
                if (!currMergeHelper(splitFile, splitPointCommit
                                .getMappedFiles().get(splitFile),
                        splitCommitSHA, currCommit.getMappedFiles()
                                .get(splitFile),
                        currCommitSHA)) {
                    goodMerge = false;
                }
            } else if (!currCommitFiles.contains(splitFile)
                    && gCommitFiles.contains(splitFile)) {
                if (!givenMergeHelper(splitFile, splitPointCommit
                                .getMappedFiles().get(splitFile),
                        splitCommitSHA, gCommit.getMappedFiles()
                                .get(splitFile), gCommitSHA)) {
                    goodMerge = false;
                }
            }
        }
        for (String givenFiles : gCommitFiles) {
            if (!splitPointFiles.contains(givenFiles)
                    && !currCommitFiles.contains(givenFiles)) {
                byte[] fileContents = Utils.readContents(new File(gCommit
                        .getMappedFiles().get(givenFiles)));
                File tmpFile = new File(givenFiles);
                Utils.writeContents(tmpFile, fileContents);
                stagedFiles.add(givenFiles);
            } else if (!splitPointFiles.contains(givenFiles)
                    && currCommitFiles.contains(givenFiles)) {
                conflictedFileMessage(new File(gCommit.getMappedFiles()
                                .get(givenFiles)),
                        new File(currCommit.getMappedFiles().get(givenFiles)),
                        splitCommitSHA, givenFiles);
                goodMerge = false;
            }
        }
        return goodMerge;
    }

    /** second helper method for merge.
     *
     * @param splitFileName the split file name.
     * @param splitFile the split file.
     * @param splitSHA the split file sha.
     * @param currFile the current file.
     * @param currSHA the current file sha.
     * @return a boolean.
     */
    public boolean currMergeHelper(String splitFileName,
                                   String splitFile, String splitSHA,
                                   String currFile, String currSHA) {
        String splitFileContents =
                new String(Utils.readContents(new File(splitFile)));
        String currFileContents =
                new String(Utils.readContents(new File(currFile)));
        if (Utils.sha1(splitFileContents)
                .equals(Utils.sha1(currFileContents))) {
            remove(splitFileName);
            return true;
        } else {
            conflictedFileMessage(null, new File(currFile),
                    currSHA, splitFileName);
            return false;
        }
    }

    /** merge helper.
     *
     * @param splitFileName the split file name.
     * @param splitFile the split file.
     * @param splitSHA the split file sha.
     * @param givenFile the given file.
     * @param givenSHA the given file sha.
     * @return a boolean.
     */
    public boolean givenMergeHelper(String splitFileName,
                                    String splitFile, String splitSHA,
                                    String givenFile, String givenSHA) {
        byte[] splitFileContents = Utils.readContents(new File(splitFile));
        byte[] givenFileContents = Utils.readContents(new File(givenFile));
        if (Utils.sha1(splitFileContents)
                .equals(Utils.sha1(givenFileContents))) {
            return true;
        } else {
            conflictedFileMessage(new File(givenFile), null,
                    givenSHA, splitFileName);
            return false;
        }
    }

    /** the mergehelper method.
     *
     * @param splitFileName the split file name.
     * @param splitFile the split file.
     * @param splitSHA the split file sha.
     * @param givenFile the given file.
     * @param givenSHA the given file sha.
     * @param currFile the current file.
     * @param currSHA the current file sha.
     * @return a boolean.
     */
    public boolean mergeHelper(String splitFileName,
                               String splitFile, String splitSHA,
                               String givenFile, String givenSHA,
                               String currFile, String currSHA) {
        byte[] splitFileContents =
                Utils.readContents(new File(splitFile));
        byte[] givenFileContents =
                Utils.readContents(new File(givenFile));
        byte[] currFileContents =
                Utils.readContents(new File(currFile));
        if (Utils.sha1(splitFileContents)
                .equals(Utils.sha1(currFileContents))
                && !Utils.sha1(splitFileContents)
                .equals(Utils.sha1(givenFileContents))) {
            File tmpFile = new File(splitFileName);
            Utils.writeContents(tmpFile, givenFileContents);
            stagedFiles.add(splitFileName);
        } else if (!Utils.sha1(splitFileContents)
                .equals(Utils.sha1(currFileContents))
                && Utils.sha1(splitFileContents)
                .equals(Utils.sha1(givenFileContents))) {
            return true;
        } else if (!Utils.sha1(splitFileContents)
                .equals(Utils.sha1(currFileContents))
                && !Utils.sha1(splitFileContents)
                .equals(Utils.sha1(givenFileContents))) {
            conflictedFileMessage(new File(givenFile),
                    new File(currFile), splitSHA, splitFileName);
            return false;
        }
        return true;
    }

    /** the method to find a split point.
     *
     * @param commit1 the first commit.
     * @param commit2 the second commit.
     * @return the split point commit.
     */
    public Commit findSplitPoint(Commit commit1, Commit commit2) {
        HashMap<String, Commit> gCommitParents = new HashMap<>();
        Commit splitCommit = null;
        Commit nextCommit1 = commit1;
        Commit nextCommit2 = commit2;
        if (nextCommit2.getParentSHA() == null) {
            gCommitParents.put(nextCommit2.getCommitSHA(), nextCommit2);
        } else {
            while (nextCommit2.getParentSHA() != null) {
                gCommitParents.put(nextCommit2.getCommitSHA(), nextCommit2);
                nextCommit2 = commitTree.get(nextCommit2.getParentSHA());
            }
            gCommitParents.put(nextCommit2.getCommitSHA(), nextCommit2);
        }
        if (nextCommit1.getParentSHA() == null) {
            if (gCommitParents.containsKey(nextCommit1.getCommitSHA())) {
                splitCommit = nextCommit1;
            }
        } else {
            while (nextCommit1.getParentSHA() != null) {
                if (gCommitParents.containsKey(nextCommit1.getCommitSHA())) {
                    splitCommit = nextCommit1;
                    break;
                }
                nextCommit1 = commitTree.get(nextCommit1.getParentSHA());
            }
        }
        return splitCommit;
    }

    /**
     * the conflicted file message method.
     *
     * @param givenName the given name of the file.
     * @param currentName the current file name.
     * @param sha the sha you want to use.
     * @param fileName the name of the file.
     */
    public void conflictedFileMessage(File givenName, File currentName,
                                      String sha, String fileName) {
        String readCurrentFile;
        String readGivenFile;
        if (givenName == null) {
            readGivenFile = "";
            readCurrentFile = new String(Utils.readContents(currentName));
        } else if (currentName == null) {
            readCurrentFile = "";
            readGivenFile = new String(Utils.readContents(givenName));
        } else {
            readCurrentFile = new String(Utils.readContents(currentName));
            readGivenFile = new String(Utils.readContents(givenName));
        }
        String newContents = "<<<<<<< HEAD\n";
        newContents += readCurrentFile;
        newContents += "=======\n";
        newContents += readGivenFile;
        newContents += ">>>>>>>\n";
        byte[] result = newContents.getBytes();
        File file = new File(fileName);
        Utils.writeContents(file, result);
    }
}
