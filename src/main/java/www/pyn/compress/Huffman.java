package www.pyn.compress;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Huffman {
    private int[] _weight;
    public List<Integer> offsets = new ArrayList<Integer>();
    private Bitset body = new Bitset();
    private TreeNode root;

    public Huffman() {
        _weight = new int[32768];
        for (int i = 0; i < 32768; i++) {
            _weight[i] = 0;
        }
    }

    public TreeNode buildTree() {
        TreeNodeHeap queue = new TreeNodeHeap();
        for (int i = 0; i < 32768; i++) {
            if (_weight[i] > 0) {
                queue.push(new TreeNode((short)i, _weight[i]));
            }
        }
        while (queue.size() > 1) {
            TreeNode node1 = queue.pop();
            TreeNode node2 = queue.pop();
            queue.push(new TreeNode((short)0, node1.w + node2.w, node1, node2));
        }
        TreeNode root = queue.pop();
        return root;
    }

    public void decode(String filePath) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + filePath + "!!!!!!!!!!!!!!!!");
        FileInputStream file = null;
        try {
            file = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < 32768; i++) {
            _weight[i] = FileHelper.readInt32(file);
        }
        root = buildTree();

        int offsetSize = FileHelper.readInt32(file);
        for (int i = 0; i < offsetSize; i++) {
            offsets.add(FileHelper.readInt32(file));
        }

        int bodySize = FileHelper.readInt32(file);
        body._data = new byte[bodySize];

        try {
            file.read(body._data, 0, bodySize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getLine(int id) {
        TreeNode p = root;
        List<Integer> line = new ArrayList<Integer>();
        body._rpos = offsets.get(id);
        while (body._rpos < offsets.get(id + 1)) {
            if (body.read() == 0) {
                p = p.left;
            }
            else {
                p = p.right;
            }
            if (p.left == null && p.right == null) {
                int right = (int)p.n;
                if(line.size() == 0) {
                    line.add(right);
                } else {
                    int len = line.size();
                    int left = line.get(len-1);
                    if(right > left) {
                        line.add(right);
                    } else {
                        line.remove(len-1);
                        for(int j = right; j <= left; j++) {
                            line.add(j);
                        }
                    }
                }
                p = root;
            }
        }
        return line;
    }
}
