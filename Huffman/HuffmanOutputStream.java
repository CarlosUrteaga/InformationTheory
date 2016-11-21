import java.io.*;

/**
 * This is a Huffman output stream class, it's a successor
 * of FilterOutputStream. The data passing through the huffman
 * output stream is transparently encoded and written down to
 * the parent stream.
 * 
 * Huffman algorithm requires data to be read twice, the first
 * time for calculating frequencies of symbols and the second
 * time for encoding the data using known frequencies. To accomplish
 * this encoder splits data to chunks. It allocates a buffer big
 * enough to make huffman efficient, when buffer overflows, it
 * encodes buffered data, writes buffer header to the stream (to
 * let decoder know about length of segment and the content of
 * frequencies table) followed by the sequence of binary huffman
 * codes. Then new segment starts.
 */
public class HuffmanOutputStream extends FilterOutputStream {
	protected byte[] segment;
	protected int bytesWritten;

	/**
	 * Initialize a huffman output stream.
	 * Note: default segment size will be used for buffer
	 * @param out	parent output stream
	 * @see FilterOutputStream
	 * @see HuffmanConsts
	 */
	public HuffmanOutputStream(OutputStream out) {
		super(out);
		constructHuffman(HuffmanConsts.DEFAULT_SEGMENT_SIZE_KB);
	}

	/**
	 * Initialize huffman output stream with the given segment
	 * size.
	 * @param out	parent output stream
	 * @param bufsizeKB	segment size in KBs.
	 * @see FilterOutputStream
	 */
	public HuffmanOutputStream(OutputStream out, int bufsizeKB) {
		super(out);
		constructHuffman(bufsizeKB);
	}
	
	private void constructHuffman(int segmentSizeKb) {
		segment = new byte[segmentSizeKb * 1024];
		bytesWritten = 0;
	}
	
	/**
	 * @return internal huffer size (in bytes) of HuffmanOutputStream
	 */
	public int getBufSize() {
		return segment.length;
	}
	
	@Override
	public void write(int b) throws IOException {
		segment[bytesWritten++] = (byte) b;
		if (bytesWritten == segment.length)
			writeSegment();
	}
	
	@Override
	public void flush() throws IOException {
		writeSegment();
	}
	
	protected void writeSegment() throws IOException {
		if (bytesWritten == 0)
			return;

		// either buffer is overflowed or flush() is called
		// calculate frequencies of buffered data
		HFreqTable freqTable = new HFreqTable();
		for (int i = 0; i < bytesWritten; i++)
			freqTable.add(segment[i]);

		// write segment header at first
		DataOutputStream dataOut = new DataOutputStream(out);
		dataOut.writeInt(HuffmanConsts.HUFFMAN_MAGIC1);
		dataOut.writeInt(bytesWritten);
		freqTable.save(dataOut);
		dataOut.writeInt(HuffmanConsts.HUFFMAN_MAGIC2);
		
		// then write the encoded data
		BitOutputStream bitout = new BitOutputStream(out);
		HuffmanEncoder enc = new HuffmanEncoder(freqTable);
		for (int i = 0; i < bytesWritten; i++) {
			Pair<Integer, Integer> bstr = enc.encode(segment[i]);
			bitout.write(bstr.getFirst(), bstr.getSecond());
		}

		bitout.flush();
		bytesWritten = 0;
	}
}

class BitOutputStream {
	protected OutputStream out;
	protected int bitsWritten;
	protected int buf;

	/**
	 * The constructor of BitOutputStream is very
	 * similar to FilteredOutputStream.
	 * @see FilteredOutputStream
	 */
	public BitOutputStream(OutputStream out) {
		this.out = out;
		bitsWritten = 0;
		buf = 0;
	}
	
	/**
	 * Write specified number of bits from the word to
	 * the stream. This method just calls writeBit multiple times.
	 * 
	 * @param word	a string of bits
	 * @param nbits	number of bits to write
	 * @see writeBit
	 */
	public void write(int word, int nbits) throws IOException {
		while (nbits-- > 0) {
			writeBit(word);
			word >>= 1;
		}
	}

	/**
	 * Write one bit to the stream.
	 * @param bit 	a bit to write
	 */
	public void writeBit(int bit) throws IOException {
		buf |= (bit & 0x1) << bitsWritten;
		bitsWritten++;
		if (bitsWritten == 8)
			flushBitBuffer();
	}
	
	/**
	 * Flushe bits in the buffer (if any) to the
	 * parent stream. Note that if buffer is not
	 * empty, 8 bits will be flushed.
	 */
	public void flush() throws IOException {
		if (bitsWritten > 0)
			flushBitBuffer();

		out.flush();
	}
	
	/**
	 * Flush the buffer and close the parent stream.
	 * @see OutputStream.close
	 */
	public void close() throws IOException {
		flush();
		out.close();
	}
	
	protected void flushBitBuffer() throws IOException {
		out.write(buf);
		buf = 0;
		bitsWritten = 0;
	}
}
