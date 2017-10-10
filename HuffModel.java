
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

// -------------------------------------------------------------------------
/**
 * Compress and uncompress a file using Heap 
 *
 * @author Nimesh Joseph Monson(nij419@lehigh.edu)
 * @author Zhenyu Li(zhl218@lehigh.edu)
 * @version Apr 11, 2016
 */
public class HuffModel
    implements IHuffModel
{
    // -----------------------------
    // Data Fields
    // -----------------------------
    /**
     * counts the number of each occurrences
     */
    private CharCounter     count        = new CharCounter();
    private int             size;
    private HuffTree        tree;
    /**
     * a MinHeap for building a big tree
     */
    private MinHeap         Hheap;
    /**
     * for every char, create a HuffTree
     */
    private HuffTree[]      forest       = null;
    /**
     * a stack for printing out the codes
     */
    private Stack<String>   stack        = new Stack<String>();
    /**
     * a 2D array to store the letters corresponding the codes
     */
    private String[][]      info         = null;
    private BitOutputStream out          = null;
    /**
     * an int to track the position
     */
    int                     indexOfArray = 0;


    // -----------------------------
    // Part One
    // -----------------------------
    @Override
    public void initialize(InputStream stream)
    {
        size = sizeOfMinHeap();
        tree = buildTree();
        info = new String[size][2];
        try
        {
            count.countAll(stream);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void showCounts()
    {
        System.out.println("Counts:");
        for (int i = 0; i < 256; i++)
        {
            if (count.getCount(i) > 0)
            {
                System.out.println((char)i + ":" + count.getCount(i));
                size++;
            }
        }
    }


    // -----------------------------
    // Part Two
    // -----------------------------
    /**
     * to get the non zero counts
     *
     * @return size - the size for the Minheap
     */
    public int sizeOfMinHeap()
    {
        size = 0;
        for (int i = 0; i < 256; i++)
        {
            if (count.getCount(i) > 0)
            {
                size++;
            }
        }
        return size + 1;
    }


    /**
     * to form the forest into a big tree
     *
     * @return tmp3 - the final tree
     */
    public HuffTree buildTree()
    {

        forest = buildForest();

        Hheap = new MinHeap(forest, size, size);
        HuffTree tmp1, tmp2, tmp3 = null;
        while (Hheap.heapsize() > 1)
        { // While two items left
            tmp1 = (HuffTree)Hheap.removemin();
            tmp2 = (HuffTree)Hheap.removemin();
            tmp3 = new HuffTree(
                tmp1.root(),
                tmp2.root(),
                tmp1.weight() + tmp2.weight());
            Hheap.insert(tmp3); // Return new tree to heap
        }
        return tmp3; // Return the tree
    }


    /**
     * for every non zero count, create a huffTree
     *
     * @return forest- an array of huffTree
     */
    public HuffTree[] buildForest()
    {
        int j = 0;
        forest = new HuffTree[size];
        for (int i = 0; i < 256; i++)
        {
            if (count.getCount(i) > 0)
            {
                forest[j] = new HuffTree((char)i, count.getCount(i));
                j++;
            }
        }
        forest[j] = new HuffTree((char)PSEUDO_EOF, 1);
        return forest;
    }


    @Override
    public void showCodings()
    {
        tree = buildTree();
        info = new String[size][2];
        traverse(tree.root());
        System.out.println("\nEncodings: ");
        for (int i = 0; i < info.length; i++)
        {
            System.out.println(info[i][0] + " : " + info[i][1]);
        }
    }


    /**
     * to traverse all the nodes using preorder
     *
     * @param root
     *            - the roots of a HuffTree
     * @param string
     *            - passing an empty string
     */
    private void traverse(HuffBaseNode root)
    {
        // base case
        if (root == null)
        {
            return;
        }
        // else traverse the left then right
        String str = new String("");
        if (root.isLeaf())
        {
            for (int i = 0; i < stack.size(); i++)
            {
                str += stack.get(i);
            }

            info[indexOfArray][0] = "" + ((HuffLeafNode)root).element();
            info[indexOfArray][1] = str;
            indexOfArray++;
            stack.pop();
        }
        else
        {
            stack.push("0");
            traverse(((HuffInternalNode)root).left());
            stack.push("1");
            traverse(((HuffInternalNode)root).right());
            if (stack.size() > 0)
            {
                stack.pop();
            }
        }
    }


    // -----------------------------
    // Part Three
    // -----------------------------
    @Override
    public void write(InputStream stream, String file, boolean force)
    {
        out = new BitOutputStream(file);
        out.write(BITS_PER_INT, MAGIC_NUMBER);
        boolean check = force;
        if (force == false)
        {
            check = checkToCompare();
        }

        if (check == true)
        {
            traverseForWriting(tree.root(), out);
            int inbits;
            char[] temp;
            try
            {
                BitInputStream bitInputStream = new BitInputStream(stream);
                while ((inbits = bitInputStream.read(BITS_PER_WORD)) != -1)
                {

                    temp = findEncoding(inbits).toCharArray();

                    for (int i = 0; i < temp.length; i++)
                    {
                        out.write(1, temp[i]);
                    }
                }
                temp = findEncoding(256).toCharArray();
                for (int i = 0; i < temp.length; i++)
                {
                    out.write(1, temp[i]);
                }
                bitInputStream.close();
                out.close();
                System.out.println("Compression Complete.");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {

            System.out.println(
                "Expected Size after compression: " + expectedTotalSize()
                    + " Bits");
            System.out.println(
                "Original Size before compression:" + originalSize() + " bits");
            System.out.println(
                "Expected size after compression is larger than original file size.");
            System.out.println("Compression not performed.");
        }
    }


    /**
     * to write the bits
     *
     * @param root
     *            - as input
     * @param x
     *            - the output stream
     */
    public void traverseForWriting(HuffBaseNode root, BitOutputStream x)
    {
        // base case
        if (root == null)
        {
            return;
        }
        if (root.isLeaf())
        {
            x.write(1, 1);
            x.write(9, ((HuffLeafNode)root).element());
        }
        else
        {
            x.write(1, 0);
            traverseForWriting(((HuffInternalNode)root).left(), x);
            traverseForWriting(((HuffInternalNode)root).right(), x);
        }
    }


    // ----------------------------------------------------------
    /**
     * finds encoding for the bits received
     *
     * @param x
     *            - the received bits
     * @return the encoding
     */
    public String findEncoding(int x)
    {
        int temp = 0;
        for (int i = 0; i < size; i++)
        {
            if (info[i][0].charAt(0) == (char)x)
            {
                temp = i;
            }
        }
        return info[temp][1];
    }


    // -----------------------------
    // Part Four
    // -----------------------------

    @Override
    public void uncompress(InputStream in, OutputStream out1)
    {
        BitInputStream input = new BitInputStream(in);
        BitOutputStream output = (BitOutputStream)out1;
        int magic;
        try
        {
            magic = input.read(BITS_PER_INT);
            if (magic == -1)
            {
                System.out.println("The argument is not a compressed file.");
                System.out.println("Unhuff not happening");
            }
            else if (magic != MAGIC_NUMBER)
            {
                input.close();
                throw new IOException("magic number not right");
            }
            else
            {
                HuffBaseNode node = buildHuffTree(input);
                buildFile(input, output, node);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * to build a huffman tree base on the input for uncompressing
     *
     * @param in
     *            - the input stream
     * @return node - includes the left and right subtree
     */
    public HuffBaseNode buildHuffTree(BitInputStream in)
    {
        int inbits;
        HuffBaseNode node = null;
        try
        {
            inbits = in.read(1);
            if ((inbits & 1) == 0)
            {
                node = new HuffInternalNode(null, null, 0);
                ((HuffInternalNode)node).setLeft(buildHuffTree(in));
                ((HuffInternalNode)node).setRight(buildHuffTree(in));
            }
            else
            {
                inbits = in.read(9);
                if (inbits == 256)
                {
                    node = new HuffLeafNode((char)PSEUDO_EOF, 0);
                    return node;
                }
                else
                {
                    node =
                        new HuffLeafNode((char)inbits, count.getCount(inbits));
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return node;
    }


    /**
     * build a file base on the HuffTree
     *
     * @param in
     *            - bitInputStream
     * @param out1
     *            - bitOutputStream
     * @param node
     *            - takes on the node (with internal node aka a tree)
     */
    public void buildFile(
        BitInputStream in,
        BitOutputStream out1,
        HuffBaseNode node)
    {
        HuffBaseNode root = node;
        HuffBaseNode x = node;
        int inbits = 0;
        try
        {
            while (true)
            {
                inbits = in.read(1);
                if (inbits == -1)
                {
                    in.close();
                    out1.close();
                    throw new IOException("unexpected end of input file");
                }
                else
                {
                    if (inbits == 0)
                    {
                        x = ((HuffInternalNode)x).left();
                    }
                    else if (inbits == 1)
                    {
                        x = ((HuffInternalNode)x).right();
                    }
                    if (x.isLeaf())
                    {
                        if (((HuffLeafNode)x).element() == PSEUDO_EOF)
                        {
                            break;
                        }
                        else
                        {
                            out1.write(((HuffLeafNode)x).element());
                        }
                        x = root;
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        in.close();
        out1.close();
    }


    // -----------------------------
    // Part Five
    // -----------------------------
    /**
     * to get the expected size MAGIC_NUMBER + size of the huffmantree + size of
     * the compressed content
     *
     * @param root
     *            is the root of the node to be formed
     * @return the tree size
     */
    public int expectedTreeSize(HuffBaseNode root)
    {
        if (root.isLeaf())
        {
            return 10;
        }
        else
        {
            return (1 + expectedTreeSize(((HuffInternalNode)root).left())

                + expectedTreeSize(((HuffInternalNode)root).right()));
        }
    }


    // ----------------------------------------------------------
    /**
     * calculates the size of the encoding that is written to the compressed
     * file
     *
     * @param root
     *            is the root of the tree
     * @return the encoding size
     */
    public int expectedContentSize(HuffBaseNode root)
    {
        if (root.isLeaf())
        {
            String str = findEncoding(((HuffLeafNode)root).element());
            return ((HuffLeafNode)root).weight() * str.length();
        }
        else
        {
            return expectedContentSize(((HuffInternalNode)root).left())
                + expectedContentSize(((HuffInternalNode)root).right());

        }
    }


    // ----------------------------------------------------------
    /**
     * Calculates the total size for the compressed file
     *
     * @return the size of the compressed file
     */
    public int expectedTotalSize()
    {
        int count1 = 0;
        count1 = expectedContentSize(tree.root())
            + expectedTreeSize(tree.root()) + 32;
        return count1;
    }


    // ----------------------------------------------------------
    /**
     * Returns the original size of the file
     *
     * @return the size
     */
    public int originalSize()
    {
        int ogCount = 0;
        for (int i = 0; i < 256; i++)
        {
            ogCount += count.getCount(i);
        }
        return ogCount * 8;
    }


    // ----------------------------------------------------------
    /**
     * Checks if expected size is less than original size
     *
     * @return true or false
     */
    public boolean checkToCompare()
    {
        int origSize = originalSize();
        int expSize = expectedTotalSize();
        return (expSize < origSize);
    }

}
