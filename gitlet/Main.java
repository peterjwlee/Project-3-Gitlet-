package gitlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;



/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Wesley wan and Peter Lee
 */

public class Main implements Serializable {

    /** The gitlet object. */
    private static Gitlet gitlet;

    /** The main method.
     *
     * @param args the arguments put in the method.
     */
    public static void main(String... args) {
        Path serialize = Paths.get(".gitlet", "gitlet.ser");
        if (Files.exists(serialize)) {
            gitlet = unserialize(serialize);
        } else {
            gitlet = new Gitlet();
        }
        String commands = args[0];
        if (commands == null) {
            System.out.println("Please enter a command.");
            return;
        }
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
        }
        if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
        }
        callCommands(args);
        serialize(gitlet);
    }


    /** Helper for the main.
     *
     * @param args the rest of the args.
     */
    public static void callCommands(String... args) {
        String commands = args[0];
        switch (commands) {
            case "init":
                gitlet.init();
                break;
            case "add":
                gitlet.add(args[1]);
                break;
            case "rm":
                gitlet.remove(args[1]);
                break;
            case "commit":
                gitlet.commit(args[1]);
                break;
            case "log":
                gitlet.log();
                break;
            case "global-log":
                gitlet.globalLog();
                break;
            case "find":
                gitlet.find(args[1]);
                break;
            case "branch":
                gitlet.branch(args[1]);
                break;
            case "rm-branch":
                gitlet.removeBranch(args[1]);
                break;
            case "status":
                gitlet.status();
                break;
            case "reset":
                gitlet.reset(args[1]);
                break;
            case "merge":
                gitlet.merge(args[1]);
                break;
            case "checkout":
                if (args.length == 3) {
                    gitlet.checkout(args[2]);
                } else if (args.length == 4) {
                    gitlet.checkout(args[1], args[3]);
                } else {
                    gitlet.checkout(args[1]);
                }
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }

    /** This executes serialization of files.
     * @param myGitlet the gitlet object. */
    public static void serialize(Gitlet myGitlet) {
        try {
            File myGitletFile = new File(Paths.get(".gitlet", "gitlet.ser").
                    toAbsolutePath().toString());
            FileOutputStream fileOut = new FileOutputStream(myGitletFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(myGitlet);
            objectOut.close();
            fileOut.close();
        } catch (IOException e) {
            String msg = "IOException while saving gitlet.";
            System.out.println(msg);
        }
    }

    /** This executes de-serialization of files.
     * @param serialize the path to serialize.
     * @return gitlet object. */
    private static Gitlet unserialize(Path serialize) {
        Gitlet myGitlet = null;
        try {
            FileInputStream fileIn =
                    new FileInputStream(serialize.
                            toAbsolutePath().toString());
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            myGitlet = (Gitlet) objectIn.readObject();
            objectIn.close();
            fileIn.close();
        } catch (IOException e) {
            System.out.println("IOException while loading gitlet.");
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException while"
                    + " loading gitlet.");
        }
        return myGitlet;
    }
}
