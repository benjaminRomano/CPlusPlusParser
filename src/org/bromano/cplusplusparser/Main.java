package org.bromano.cplusplusparser;
import org.bromano.cplusplusparser.scanner.*;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new SimpleScanner();

//        if(args.length >= 1) {
//            scanner.setText(loadFile(args[0]));
//            printTokens(scanner.lex());
//        }

        scanner.setText("test");
        printTokens(scanner.lex());
    }

    public static void printTokens(List<Token> tokens) {
        if(tokens == null) {
            return;
        }

        for(Token t : tokens) {
            System.out.println(t);
        }
    }


    //Reads in file preserving special characters
    public static String loadFile(String filePath) {

        StringBuilder sb = new StringBuilder(512);
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(filePath));
            Reader r = new InputStreamReader(stream, "UTF-8");
            int c = 0;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
