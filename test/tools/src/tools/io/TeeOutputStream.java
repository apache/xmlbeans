package tools.io;

import java.io.OutputStream;

/**
 * Date: May 26, 2004
 */

public class TeeOutputStream extends OutputStream
{
    OutputStream out1;
    OutputStream out2;

    public TeeOutputStream(OutputStream out1, OutputStream out2)
    {
        this.out1 = out1;
        this.out2 = out2;
    }

    // Override methods of OutputStream
    public void close()
        throws java.io.IOException
    {
        out1.close();
        out2.close();
    }

    public void flush()
            throws java.io.IOException
    {
        out1.flush();
        out2.flush();
    }

    // Implementation of Outputstream's abstract method
    public void write(int b)
            throws java.io.IOException
    {
        out1.write(b);
        out2.write(b);
    }

}
