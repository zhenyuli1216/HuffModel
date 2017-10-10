import java.io.FileInputStream;
import java.io.FileNotFoundException;

// -------------------------------------------------------------------------
/**
 * to run compression
 *
 * @author Zhenyu li (zhl218)
 * @author Nimesh Joseph Monson(nij419@lehigh.edu)
 * @version May 8, 2016
 */
public class Huff
{
    /**
     * main method
     *
     * @param args
     *            - takes two arguments
     */
    public static void main(String args[])
    {
        BitInputStream bits = null;
        HuffModel toHuff = new HuffModel();
        boolean force = true;
        try
        {
            bits = new BitInputStream(new FileInputStream(args[1]));
        }
        catch (FileNotFoundException e)
        {
            System.out.println(e.getMessage());
        }

        if (args[0].equals("false"))
        {
            force = false;
        }

        toHuff.initialize(bits);
        toHuff.showCounts();
        toHuff.showCodings();
        try
        {
            toHuff.write(new FileInputStream(args[1]), "mytext.huff", force);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
