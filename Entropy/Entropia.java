import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

class Entropia {
    public static void main(String[] args) {
	
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String filepath=null;

	try {
	 System.out.print("Ingrese la ruta del archivo: ");
         filepath = br.readLine();
      	} catch (IOException ioe) {
         System.out.println("IO error trying to read filepath!");
         System.exit(1);
      	}

        File file = new File(filepath);
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            byte fileContent[] = new byte[(int)file.length()];
            
            // Read data into the byte array
            fin.read(fileContent);

	    // create array to keep track of frequency of bytes
        int []frequency_array = new int[256];
        int []frequency_arrayNible = new int[16];
        int []frequency_arrayDodec = new int[4096];

	    int fileContentLength = fileContent.length-1;

	    // count frequency of occuring bytes
        for(int i=0; i<fileContentLength; i++) {
		byte byteValue=fileContent[i];
		frequency_array[byteValue]++;

        //int val = byteValue;
        //nibble
        int lo = byteValue & 0xF;//mask
        int hi = byteValue >> 4;//corrimiento
        frequency_arrayNible[lo]++;
        frequency_arrayNible[hi]++;

        
	    }

	    double entropy = 0;
        double entropyNibble = 0;
        System.out.println("Byte");
        System.out.println("Simbolo\tVeces\t Prob.\tInformacion");
	    for(int i=0;i<frequency_array.length;i++) {
	        if(frequency_array[i]!=0) {
		    // calculate the probability of a particular byte occuring
		    double probabilityOfByte=(double)frequency_array[i]/(double)fileContentLength;

		    // calculate the next value to sum to previous entropy calculation
		    double value = probabilityOfByte * (Math.log(probabilityOfByte) / Math.log(2));
            	    entropy = entropy + value;

            System.out.println(i+"\t"+frequency_array[i]+"\t"+probabilityOfByte+"\t"+value*-1);
		}
		else {}
            }
            entropy *= -1;
	    //nibble
        System.out.println(""+fileContentLength);
        System.out.println("Nibble");
        System.out.println("Simbolo\tVeces\t Prob.\tInformacion");

        for(int i=0;i<frequency_arrayNible.length;i++) {
            if(frequency_arrayNible[i]!=0) {
            // calculate the probability of a particular byte occuring
            double probabilityOfByte=(double)frequency_arrayNible[i]/((double)fileContentLength*2);

            // calculate the next value to sum to previous entropy calculation
            double value = probabilityOfByte * (Math.log(probabilityOfByte) / Math.log(2));
                    entropyNibble = entropyNibble + value;

            System.out.println(i+"\t"+frequency_arrayNible[i]+"\t"+probabilityOfByte+"\t"+value*-1);
        }
        else {}
            }
            entropyNibble *= -1;

	    // output the entropy calculated
	    DecimalFormat df = new DecimalFormat("##.#####");
        System.out.println("Entropia es: " + df.format(entropy) + " bits por byte");
        System.out.println("Entropia es: " + df.format(entropyNibble) + " bits por nibble");
        }

        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }

        catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        }

        finally {
            // close the streams using close method
            try {
                if (fin != null) {
                    fin.close();
                }
            }

            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
    }
}