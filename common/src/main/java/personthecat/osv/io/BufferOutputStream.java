package personthecat.osv.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BufferOutputStream extends ByteArrayOutputStream {
    public synchronized ByteArrayInputStream toInputStream() {
        return new ByteArrayInputStream(this.buf, 0, size());
    }
}