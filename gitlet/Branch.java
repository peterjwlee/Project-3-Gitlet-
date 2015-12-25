package gitlet;

import java.io.Serializable;
import java.util.Map;

/**
 * @author wesleywan and peterlee on 12/2/15.
 */
public class Branch implements Serializable {
    /** The commit of this branch. */
    private Commit _commit;

    /** The name of this branch. */
    private String _branchName;

    /** Branch constructor.
     * @param branchName the name of the branch.
     * @param commit the commit of this branch.
     * */
    public Branch(String branchName, Commit commit) {
        _branchName = branchName;
        _commit = commit;

    }

    /** getter for mapped files.
     * @return the mapped files from the commit.
     * */
    public Map<String, String> getMappedFiles() {
        return _commit.getMappedFiles();
    }

    /** getter for commit.
     * @return the commit of this branch.
     * */
    public Commit getCommit() {
        return _commit;
    }

    /** getter for branch name.
     * @return the branch name.
     * */
    public String getBranchName() {
        return _branchName;
    }

    /** updates the branch.
     * @param commit the commit of this branch.
     * */
    public void updateHead(Commit commit) {
        _commit = commit;
    }

    /** overloaded method to update head.
     *
     * @param commit the commit to update
     * @param branchName the name of the branch
     */
    public void updateHead(Commit commit, String branchName) {
        _commit = commit;
        _branchName = branchName;
    }
}
