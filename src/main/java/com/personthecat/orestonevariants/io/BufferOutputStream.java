package com.personthecat.orestonevariants.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BufferOutputStream extends ByteArrayOutputStream {
    public synchronized ByteArrayInputStream toInputStream() {
        return new ByteArrayInputStream(buf, 0, size());
    }
}