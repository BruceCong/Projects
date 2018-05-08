import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * 
 * @author Bruce Cong
 */

public class Gitlet implements Serializable {
    private HashSet<String> adds = new HashSet<String>();
    private HashSet<String> removes = new HashSet<String>();
    private HashMap<String, ArrayList<Long>> messageToID = new HashMap<String, ArrayList<Long>>();
    private CommitTree ct;

    /**
     * This is the Gitlet constructor. It constructs a CommitTree and adds in
     * the initial commit message and ID.
     **/
    public Gitlet() {
        ct = new CommitTree();
        String message = ct.getPointer().getMessage();
        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(ct.getID());
        messageToID.put(message, ids);
    }

    /**
     * The main method that takes in all the commands and does what it needs to
     * do.
     */
    public static void main(String[] args) {
        Gitlet myGitlet = deserialize();
        if (args.length == 0) {
            System.out.println("Please enter a command!");
        } else if (checkArgs(args)) {
            System.out.println("Not enough arguments!");
        } else {
            String command = args[0];
            switch (command) {
                case "init":
                    init();
                    myGitlet = new Gitlet();
                    break;
                case "add":
                    myGitlet.add(args[1]);
                    break;
                case "commit":
                    if (args.length == 1) {
                        System.out.println("Please enter a commit message.");
                    } else {    
                        myGitlet.commit(args[1]);
                    }
                    break;
                case "rm":
                    myGitlet.remove(args[1]);
                    break;
                case "log":
                    myGitlet.log();
                    break;
                case "global-log":
                    CommitTree.Node n = myGitlet.ct.getGlobalLogPointer();
                    myGitlet.globalLog(n);
                    break;
                case "find":
                    myGitlet.find(args[1]);
                    break;
                case "status":
                    myGitlet.status();
                    break;
                case "checkout":
                    if (args.length == 1) {
                        System.out.println("Need something after checkout.");
                    } else if (args.length == 2) {
                        myGitlet.checkout(args[1]);
                    } else {
                        myGitlet.checkout2(args[1], args[2]);
                    }
                    break;
                case "branch":
                    myGitlet.createBranch(args[1]);
                    break;
                case "rm-branch":
                    myGitlet.removeBranch(args[1]);
                    break;
                case "reset":
                    myGitlet.reset(args[1]);
                    break;
                case "merge":
                    myGitlet.merge(args[1]);
                    break;
                case "rebase":
                    myGitlet.rebase(args[1]);
                    break;
                case "i-rebase":
                    myGitlet.interactiveRebase(args[1]);
                    break;
                default:
                    System.out.println("Not a valid command!");
                    break;
            }
        }
        serialize(myGitlet);
    }
    
    private static boolean checkArgs(String[] str) {
        String command = str[0];
        if ((command.equals("add") || command.equals("rm") || command.equals("find") 
            || command.equals("branch") || command.equals("rm-branch")
                || command.equals("reset") || command.equals("merge") || command.equals("rebase")
                    || command.equals("rebase") || command.equals("i-rebase")) && str.length == 1) {
            return true;
        }
        return false;
    }

    /** Initializes Gitlet and starts everything up **/
    private static void init() {
        File dir = new File(".gitlet");
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            System.out.println("A gitlet version control system"
                    + " already exists in the current directory.");
        }
    }

    /** Stages the file that is inputed. **/
    private void add(String str) {
        if (str != null) {
            File file1 = new File(str);
            if (!file1.exists()) {
                System.out.println("File does not exist.");
            } else if (!ct.containsFile(str)) {
                adds.add(str);
            } else {
                try {
                    File file2 = new File(ct.getFilePath(str));
                    FileInputStream f1 = new FileInputStream(file1);
                    FileInputStream f2 = new FileInputStream(file2);
                    int f1Byte = f1.read();
                    while (f1Byte != -1) {
                        int f2Byte = f2.read();
                        if (f1Byte != f2Byte) {
                            adds.add(str);
                            f1.close();
                            f2.close();
                            return;
                        }
                        f1Byte = f1.read();
                    }
                    int f2Byte = f2.read();
                    if (f2Byte == -1) {
                        f1.close();
                        f2.close();
                        System.out
                                .println("File has not been modified since the last commit.");
                    } else {
                        adds.add(str);
                        f1.close();
                        f2.close();
                    }
                } catch (IOException e) {
                    System.out.println("There's an IOException!");
                }
            }
        }
    }

    /** Commits the staged files and creates a new node to the commit tree. **/
    private void commit(String str) {
        if (str != null) {
            if (adds.isEmpty()) {
                System.out.println("No changes added to the commit.");
            }
            ct.addNode(ct.createNode(str));
            if (messageToID.containsKey(str)) {
                messageToID.get(str).add(ct.getID());
            } else {
                ArrayList<Long> ids = new ArrayList<Long>();
                ids.add(ct.getID());
                messageToID.put(str, ids);
            }
            ct.putID(ct.getID());
            File dir = new File(".gitlet/commit" + ct.getID());
            if (!dir.exists()) {
                dir.mkdir();
            }
            for (String file : adds) {
                String filePath = ".gitlet/commit" + ct.getID() + "/";
                String[] fileBackSlash = file.split("/");
                for (String i : fileBackSlash) {
                    String lastOne = fileBackSlash[fileBackSlash.length - 1];
                    if (i.equals(lastOne)) {
                        filePath += i;
                    } else {
                        filePath = filePath + i + "/";
                        File directories = new File(filePath);
                        if (!directories.exists()) {
                            directories.mkdir();
                        }
                    }
                }
                ct.putFiles(file, filePath);
                File f1 = new File(file);
                File f1Path = new File(filePath);
                try {
                    Files.copy(f1.toPath(), f1Path.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("IOException while commiting!");
                }
            }
            for (String file : removes) {
                ct.deleteFile(file);
            }
            adds.clear();
        }
    }

    /**
     * First it removes the file from files to be staged, and then it doesn't
     * commit that file from the previous commit
     **/
    private void remove(String str) {
        if (adds.contains(str)) {
            adds.remove(str);
        } else if (ct.containsFile(str)) {
            removes.add(str);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    /**
     * Provides a log from the current head going backwards and gives the commit
     * id, date and commit message.
     */
    private void log() {
        CommitTree.Node n = ct.getPointer();
        while (n != null) {
            System.out.println("====");
            System.out.println("Commit " + n.getID());
            System.out.println(n.getDate());
            System.out.println(n.getMessage());
            System.out.println(" ");
            n = n.getParent();
        }
    }

    /**
     * Provides a log for every commit across all branches. Information same as
     * log.
     **/
    private void globalLog(CommitTree.Node n) {
        if (n == null) {
            return;
        } else {
            System.out.println("====");
            System.out.println("Commit " + n.getID());
            System.out.println(n.getDate());
            System.out.println(n.getMessage());
            System.out.println(" ");
            for (CommitTree.Node i : n.getChildren()) {
                globalLog(i);
            }
        }
    }

    /** Takes in a commit message and returns the id **/
    private void find(String str) {
        if (str != null) {
            if (messageToID.get(str) == null) {
                System.out.println("Found no commit with that message.");
            } else {
                for (Long i : messageToID.get(str)) {
                    System.out.println(i);
                }
            }
        }
    }

    /** Gives the staged files, branches, and files to be removed. **/
    private void status() {
        System.out.println("=== Branches ===");
        System.out.println("*" + ct.getCurrentBranch());
        for (String s : ct.getBranches()) {
            if (!s.equals(ct.getCurrentBranch())) {
                System.out.println(s);
            }
        }
        System.out.println(" ");
        System.out.println("=== Staged Files ===");
        for (String i : adds) {
            System.out.println(i);
        }
        System.out.println(" ");
        System.out.println("=== Files Marked for Removal ===");
        for (String k : removes) {
            System.out.println(k);
        }

    }

    /** Creates a new branch. Does not transfer to that branch. **/
    private void createBranch(String str) {
        if (ct.getBranches().contains(str)) {
            System.out.println("A branch with that name already exists.");
        }
        ct.createBranch(str);
    }

    /** Removes a branch. **/
    private void removeBranch(String str) {
        if (!ct.getBranches().contains(str)) {
            System.out.println("A branch with that name does not exist.");
        } else if (str.equals(ct.getCurrentBranch())) {
            System.out.println("Cannot remove the current branch.");
        } else {
            ct.removeBranch(str);
        }
    }

    /**
     * One argument checkout that either takes in a branch or a file name.
     * Reverts file(s) to the branch or current head's file.
     **/
    private void checkout(String str1) {
        System.out
                .println("Warning: The command you entered may "
                        + "alter the files in your working directory. "
                        + "Uncommited changes may be lost. "
                        + "Are you sure you want to continue? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine();
        if (answer.equals("yes")) {
            scanner.close();
            checkoutErrorHandling1(str1);
            if (ct.getBranches().contains(str1)) {
                ct.changeBranch(str1);
                for (String s : ct.getFilePaths()) {
                    String filePath = ct.getFilePath(s);
                    File newFile = new File(filePath);
                    File oldFile = new File(s);
                    try {
                        Files.copy(newFile.toPath(), oldFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.out.println("IOException on branch checkout!");
                    }
                }
            } else if (ct.containsFile(str1)) {
                String filePath = ct.getFilePath(str1);
                File newFile = new File(filePath);
                File oldFile = new File(str1);
                try {
                    Files.copy(newFile.toPath(), oldFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("IOException on file checkout!");
                }
            }
        } else {
            scanner.close();
            return;
        }
    }

    /**
     * Two argument checkout that takes in an id and file name and reverts the
     * file back to it's state at the commit id
     **/
    private void checkout2(String str1, String str2) {
        System.out
                .println("Warning: The command you entered may "
                        + "alter the files in your working directory. "
                        + "Uncommited changes may be lost. "
                        + "Are you sure you want to continue? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine();
        if (answer.equals("yes")) {
            scanner.close();
            Long iD = Long.parseLong(str1);
            CommitTree.Node n = ct.getNode(iD);
            if (!ct.containsID(iD)) {
                System.out.println("No commit with that id exists.");
            } else if (!n.containsFile(str2)) {
                System.out.println("File does not exist in that commit.");
            } else {
                String filePath = n.getFilePath(str2);
                File newFile = new File(filePath);
                File oldFile = new File(str2);
                try {
                    Files.copy(newFile.toPath(), oldFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("IOException while resetting!");
                }
            }
        } else {
            scanner.close();
            return;
        }
    }

    /** Helper function that cheaks the error cases for checkout function **/
    private void checkoutErrorHandling1(String str) {
        if (!ct.getBranches().contains(str) && !ct.containsFile(str)) {
            System.out.println("File does not exist in the most"
                    + " recent commit, or no such branch exists.");
        } else if (str.equals(ct.getCurrentBranch())) {
            System.out.println("No need to checkout the current branch.");
        }
    }

    /** Resets to the id given and then head moves to that id **/
    private void reset(String str) {
        System.out
                .println("Warning: The command you entered may "
                        + "alter the files in your working directory. "
                        + "Uncommited changes may be lost. "
                        + "Are you sure you want to continue? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine();
        if (answer.equals("yes")) {
            scanner.close();
            Long iD = Long.parseLong(str);
            if (!ct.containsID(iD)) {
                System.out.println("No commit with that id exists.");
            } else {
                CommitTree.Node n = ct.getNode(iD);
                ct.changeRoot(n);
                for (String s : n.getFilePaths()) {
                    System.out.println(s);
                    String filePath = n.getFilePath(s);
                    System.out.println(filePath);
                    File newFile = new File(filePath);
                    File oldFile = new File(s);
                    try {
                        Files.copy(newFile.toPath(), oldFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.out.println("IOException while resetting!");
                    }
                }
            }
        } else {
            scanner.close();
            return;
        }
    }

    /** Merges with another branch, compromising on which files to change **/
    private void merge(String str) {
        System.out
                .println("Warning: The command you entered may "
                        + "alter the files in your working directory. "
                        + "Uncommited changes may be lost. "
                        + "Are you sure you want to continue? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine();
        if (answer.equals("yes")) {
            scanner.close();
            if (str.equals(ct.getCurrentBranch())) {
                System.out.println("Cannot merge a branch with itself.");
            } else if (!ct.getBranches().contains(str)) {
                System.out.println("A branch with that name does not exist.");
            } else {
                CommitTree.Node splitNode = findSplitNode(str);
                CommitTree.Node headNode = ct.getBranchHead(str);
                CommitTree.Node currentNode = ct.getPointer();
                for (String s : splitNode.getFilePaths()) {
                    if (!splitNode.getFilePath(s).equals(
                            headNode.getFilePath(s))
                            && !splitNode.getFilePath(s).equals(
                                    currentNode.getFilePath(s))) {
                        File filePath = new File(headNode.getFilePath(s));
                        File file = new File(s + ".conflicted");
                        try {
                            Files.copy(filePath.toPath(), file.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            System.out.println("IOException while merging.");
                        }
                    } else if (!splitNode.getFilePath(s).equals(
                            headNode.getFilePath(s))) {
                        File filePath = new File(headNode.getFilePath(s));
                        File file = new File(s);
                        try {
                            Files.copy(filePath.toPath(), file.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            System.out.println("IOException while merging.");
                        }
                    }

                }
            }
        } else {
            scanner.close();
            return;
        }
    }

    /**
     * Helper function that finds the node where it splits between two branches.
     **/
    private CommitTree.Node findSplitNode(String str) {
        CommitTree.Node pointerForCurrentBranch = ct.getPointer();
        CommitTree.Node pointerForNewBranch = ct.getBranchHead(str);
        Set<CommitTree.Node> history = new HashSet<CommitTree.Node>();
        while (pointerForCurrentBranch != null) {
            history.add(pointerForCurrentBranch);
            pointerForCurrentBranch = pointerForCurrentBranch.getParent();
        }
        while (pointerForNewBranch != null) {
            if (history.contains(pointerForNewBranch)) {
                return pointerForNewBranch;
            }
            pointerForNewBranch = pointerForNewBranch.getParent();
        }
        return null;
    }

    /** Current branch splits of and reattaches to the given branch's head. **/
    private void rebase(String str) {
        System.out
                .println("Warning: The command you entered may "
                        + "alter the files in your working directory. "
                        + "Uncommited changes may be lost. "
                        + "Are you sure you want to continue? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine();
        if (answer.equals("yes")) {
            scanner.close();
            if (!ct.getBranches().contains(str)) {
                System.out.println("A branch with that name does not exist.");
            } else if (str.equals(ct.getCurrentBranch())) {
                System.out.println("Cannot rebase a branch onto itself.");
            } else {
                CommitTree.Node currentHead = ct.getPointer();
                while (currentHead != null) {
                    if (currentHead == ct.getBranchHead(str)) {
                        ct.changeRoot(ct.getBranchHead(str));
                        System.out.println("Already up-to-date.");
                        return;
                    }
                    currentHead = currentHead.getParent();
                }
                CommitTree.Node splitHead = findSplitNode(str);
                CommitTree.Node currentHead2 = ct.getPointer();
                CommitTree.Node givenHead = ct.getBranchHead(str);
                ct.changeRoot(givenHead);
                HashMap<String, String> fileToPath = new HashMap<String, String>();
                for (String s : splitHead.getFilePaths()) {
                    if (!splitHead.getFilePath(s).equals(
                            givenHead.getFilePath(s))
                            && splitHead.getFilePath(s).equals(
                                    currentHead2.getFilePath(s))) {
                        File filePath = new File(givenHead.getFilePath(s));
                        fileToPath.put(s, givenHead.getFilePath(s));
                        File file = new File(s);
                        try {
                            Files.copy(filePath.toPath(), file.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            System.out.println("IOException while merging.");
                        }
                    }
                }
                ArrayList<CommitTree.Node> nodes = new ArrayList<CommitTree.Node>();
                while (currentHead2 != splitHead) {
                    String commitMessage = currentHead2.getMessage();
                    CommitTree.Node n = ct.createNode(commitMessage);
                    nodes.add(n);
                    currentHead2 = currentHead2.getParent();
                }
                for (int i = nodes.size() - 1; i >= 0; i--) {
                    ct.addNode(nodes.get(i));
                    for (String s : fileToPath.keySet()) {
                        ct.putFiles(s, fileToPath.get(s));
                    }
                }
            }
        } else {
            scanner.close();
            return;
        }
    }

    /**
     * Same as rebase except allows you to skip commits and change commit
     * messages
     **/
    private void interactiveRebase(String str) {
        System.out
                .println("Warning: The command you entered may "
                        + "alter the files in your working directory. "
                        + "Uncommited changes may be lost. "
                        + "Are you sure you want to continue? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine();
        if (answer.equals("yes")) {
            rebaseError(str);
            CommitTree.Node splitHead = findSplitNode(str);
            CommitTree.Node currentHead2 = ct.getPointer();
            CommitTree.Node givenHead = ct.getBranchHead(str);
            ct.changeRoot(givenHead);
            HashMap<String, String> fileToPath = new HashMap<String, String>();
            for (String s : splitHead.getFilePaths()) {
                if (!splitHead.getFilePath(s).equals(givenHead.getFilePath(s))
                        && splitHead.getFilePath(s).equals(
                                currentHead2.getFilePath(s))) {
                    File filePath = new File(givenHead.getFilePath(s));
                    fileToPath.put(s, givenHead.getFilePath(s));
                    File file = new File(s);
                    try {
                        Files.copy(filePath.toPath(), file.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.out.println("IOException while merging.");
                    }
                }
            }
            ArrayList<CommitTree.Node> nodes = new ArrayList<CommitTree.Node>();
            while (currentHead2 != splitHead) {
                String commitMessage = currentHead2.getMessage();
                CommitTree.Node n = ct.createNode(commitMessage);
                iRebasePrints(n);
                while (true) {
                    String ans = scanner.nextLine();
                    if (ans.equals("s")) {
                        for (String s : n.getFilePaths()) {
                            if (!n.getParent().containsFile(s)) {
                                n.getParent().putFilePath(s, n.getFilePath(s));
                            }
                        }
                        currentHead2 = currentHead2.getParent();
                        break;
                    } else if (ans.equals("c")) {
                        nodes.add(n);
                        currentHead2 = currentHead2.getParent();
                        break;
                    } else if (ans.equals("m")) {
                        System.out
                                .println("Please enter a message for this commit.");
                        String newMessage = scanner.nextLine();
                        n.setMessage(newMessage);
                        nodes.add(n);
                        currentHead2 = currentHead2.getParent();
                        break;
                    } else {
                        System.out.println("Not a valid input. Try again.");
                    }
                }
            }
            scanner.close();
            for (int i = nodes.size() - 1; i >= 0; i--) {
                ct.addNode(nodes.get(i));
                for (String s : fileToPath.keySet()) {
                    ct.putFiles(s, fileToPath.get(s));
                }
            }
        } else {
            scanner.close();
            return;
        }
    }

    /**
     * Rebase error handling **
     */
    private void rebaseError(String str) {
        if (!ct.getBranches().contains(str)) {
            System.out.println("A branch with that name does not exist.");
        } else if (str.equals(ct.getCurrentBranch())) {
            System.out.println("Cannot rebase a branch onto itself.");
        } else {
            CommitTree.Node currentHead = ct.getPointer();
            while (currentHead != null) {
                if (currentHead == ct.getBranchHead(str)) {
                    ct.changeRoot(ct.getBranchHead(str));
                    System.out.println("Already up-to-date.");
                    return;
                }
                currentHead = currentHead.getParent();
            }
        }
    }

    /** Printing the log form of interactive rebase **/
    private void iRebasePrints(CommitTree.Node n) {
        System.out.println("Currently replaying:");
        System.out.println("====");
        System.out.println("Commit " + n.getID());
        System.out.println(n.getDate());
        System.out.println(n.getMessage());
        System.out
                .println("Would you like to (c)ontinue, (s)kip this commit," 
                    + " or change this commit's (m)essage?");
    }

    /** Tries to load Gitlet **/
    private static Gitlet deserialize() {
        Gitlet gitlet = null;
        File gitletFile = new File(".gitlet/gitlet.ser");
        if (gitletFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(gitletFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                gitlet = (Gitlet) objectIn.readObject();
                objectIn.close();
            } catch (IOException e) {
                String msg = "IOException while deserializing gitlet";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while deserializing gitlet";
                System.out.println(msg);
            }
        }
        return gitlet;
    }

    /** Saves Gitlet **/
    private static void serialize(Gitlet gitlet) {
        if (gitlet == null) {
            return;
        }
        try {
            File gitletFile = new File(".gitlet/gitlet.ser");
            FileOutputStream fileOut = new FileOutputStream(gitletFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(gitlet);
            objectOut.close();
        } catch (IOException e) {
            String msg = "IOException while serializing gitlet";
            System.out.println(msg);
        }
    }
}
