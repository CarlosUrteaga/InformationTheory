
import java.io.*;

public class HuffmanDemo {
	public static void main(String args[]) throws IOException {
		if (args.length < 1)
			usage();

		try {
			if (args[0].equals("enc"))
				doEncode(args);
			else if (args[0].equals("dec"))
				doDeocde(args);
			else
				usage();
		} catch (FileNotFoundException err) {
			System.err.println("Error: " + err.toString());
			usage();
		}

		System.exit(0);
	}

	public static void doEncode(String[] args) throws IOException {
		if (args.length < 3)
			usage();

		File inFile = new File(args[1]);
		File outFile = new File(args[2]);
		InputStream in = new FileInputStream(inFile);
		HuffmanOutputStream hout = new HuffmanOutputStream(
				new FileOutputStream(outFile));
		byte buf[] = new byte[4096];
		int len;

		while ((len = in.read(buf)) != -1)
			hout.write(buf, 0, len);

		in.close();
		hout.close();

		System.out.println("Compresión: Lista");
		System.out.println("Tamaño inicial:     " + inFile.length());
		System.out.println("Tamaño final:   " + outFile.length());
		System.out.print("Eficiencia de Compresion: ");
		if (inFile.length() > outFile.length()) {
			System.out.format("%.2f%%\n",
				(100.0 - (((double) outFile.length() / (double) inFile.length()) * 100)));
		}
		else
			System.out.println("ninguna");
	}

	public static void doDeocde(String[] args) throws IOException {
		if (args.length < 3)
			usage();

		File inFile = new File(args[1]);
		File outFile = new File(args[2]);
		HuffmanInputStream hin = new HuffmanInputStream(new FileInputStream(
				inFile));
		OutputStream out = new FileOutputStream(outFile);
		byte buf[] = new byte[4096];
		int len;

		while ((len = hin.read(buf)) != -1)
			out.write(buf, 0, len);

		hin.close();
		out.close();
		System.out.println("Desompresión: Lista");
		System.out.println("Tamaño inicial:     " + inFile.length());
		System.out.println("Tamaño final: " + outFile.length());
	}


	public static void usage() {
		System.err.println("Uso: HuffmanDemo enc|dec");
		System.err.println("       enc <input-file> <output-file>: " +
				"");
		System.err.println("                        " +
				"Error con el archivo de salida");
		System.err.println("       dec <input-file> <output-file>: " +
				"decode input file and save");
		System.err.println("                        " +
				"Error con el archivo de salida");
		System.exit(1);
	}
}
