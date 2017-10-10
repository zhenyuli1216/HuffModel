import java.io.FileInputStream;
import java.io.FileNotFoundException;

// -------------------------------------------------------------------------
/**
 * The Unhuff method uncompresses the file that was compressed by Huff
 *
 * @author Nimesh Joseph Monson(nij419@lehigh.edu)
 * @author Zhenyu Li(zhl218@lehigh.edu)
 * @version Apr 11, 2016
 */
public class Unhuff
{
    // ----------------------------------------------------------
    /**
     * The main method of Unhuff
     *
     * @param args
     *            is the arguments passed
     */
    public static void main(String[] args)
    {
        HuffModel what = new HuffModel();
        try
        {

            what.uncompress(
                new FileInputStream(args[0]),
                new BitOutputStream("uncompressed.unhuff"));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        System.out.println("Uncompression Completed");
    }
}
