import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class CommitTree implements Serializable {

    private HashMap<String, Node> branches = new HashMap<String, Node>();
    private HashMap<Long, Node> idToNode = new HashMap<Long, Node>();
    private String currentBranch;
    private Node root;
    private Node globalLogPointer;

    /** Constructs a commit tree **/
    public CommitTree() {
        root = new Node("initial commit");
        idToNode.put(root.versionID, root);
        globalLogPointer = root;
        branches.put("master", root);
        currentBranch = "master";
    }

    /** Stores file names and maps it to it's file path **/
    public void putFiles(String fileName, String filePath) {
        root.filePaths.put(fileName, filePath);
    }

    /** Stores id and connects it to a node. **/
    public void putID(Long i) {
        idToNode.put(i, root);
    }

    /** Check if file name is in the current node's mappings **/
    public boolean containsFile(String str) {
        return root.filePaths.containsKey(str);
    }

    /** Check if an id is in the commit tree. **/
    public boolean containsID(long i) {
        return idToNode.containsKey(i);
    }

    /** Using the id it returns the node connected to it **/
    public Node getNode(long i) {
        return idToNode.get(i);
    }

    /** Deletes file from mapping **/
    public void deleteFile(String str) {
        root.filePaths.remove(str);
    }

    /** Gets another pointer to the current head **/
    public Node getPointer() {
        Node n = root;
        return n;
    }

    /** Gets the current head's id **/
    public Long getID() {
        return root.versionID;
    }

    /** Check if head is empty **/
    public boolean isEmpty() {
        return (root == null);
    }

    /** Gets the current branch **/
    public String getCurrentBranch() {
        return currentBranch;
    }

    /** Gets a set of the branch names **/
    public Set<String> getBranches() {
        return branches.keySet();
    }

    /** Adds a branch to the hash map that connects branch head to branch name **/
    public void createBranch(String str) {
        branches.put(str, root);
    }

    /** Removes branch from above hash map **/
    public void removeBranch(String str) {
        branches.remove(str);
    }

    /** Gets the head of the branch **/
    public Node getBranchHead(String str) {
        return branches.get(str);
    }

    /** Changes the current branch **/
    public void changeBranch(String str) {
        root = branches.get(str);
        currentBranch = str;
    }

    /** Changes the head of the current branch **/
    public void changeRoot(Node n) {
        root = n;
    }

    /** Makes global log easier as it points to the initial commit node **/
    public Node getGlobalLogPointer() {
        return globalLogPointer;
    }

    /** Creates a node **/
    public Node createNode(String str) {
        Node n = new Node(str);
        return n;
    }

    /** Gets a file path of a file name **/
    public String getFilePath(String str) {
        return root.filePaths.get(str);
    }

    /** Gets the set of file names **/
    public Set<String> getFilePaths() {
        return root.filePaths.keySet();
    }

    /** Adds a new node on the commit tree **/
    public void addNode(Node n) {
        root.children.add(n);
        n.parent = root;
        root = n;
        for (String s : root.parent.filePaths.keySet()) {
            root.filePaths.put(s, root.parent.filePaths.get(s));
        }
        branches.put(currentBranch, root);
    }

    /** Nested node class **/
    public class Node implements Serializable {

        private String message;
        private Long versionID;
        private String currentDate;
        private Node parent;
        private List<Node> children;
        private HashMap<String, String> filePaths;

        /** Node constructor. Takes in all the values a node needs **/
        public Node(String str) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            message = str;
            currentDate = dateFormat.format(date);
            long hash = date.getTime();
            versionID = hash;
            children = new ArrayList<Node>();
            parent = null;
            filePaths = new HashMap<String, String>();
        }

        /** Gets the id of a node. **/
        public long getID() {
            return versionID;
        }

        /** Gets the date of a node. **/
        public String getDate() {
            return currentDate;
        }

        /** Gets the message of a node. **/
        public String getMessage() {
            return message;
        }

        /** Sets the message of a node. **/
        public void setMessage(String str) {
            message = str;
        }

        /** Gets the parent of a node. **/
        public Node getParent() {
            return parent;
        }

        /** Gets the children of a node. **/
        public ArrayList<Node> getChildren() {
            return (ArrayList<Node>) children;
        }

        /** Gets the list of file names of a node. **/
        public Set<String> getFilePaths() {
            return filePaths.keySet();
        }

        /** Gets the file path of a file. **/
        public String getFilePath(String str) {
            return filePaths.get(str);
        }

        /** Check if file is contained in the node. **/
        public boolean containsFile(String str) {
            return filePaths.containsKey(str);
        }

        /** Puts the file and filepaths. **/
        public void putFilePath(String str1, String str2) {
            filePaths.put(str1, str2);
        }

    }

}
