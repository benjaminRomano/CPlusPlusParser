package org.bromano.cplusplusparser;
import org.bromano.cplusplusparser.parser.Parser;
import org.bromano.cplusplusparser.parser.ParserException;
import org.bromano.cplusplusparser.parser.SimpleParser;
import org.bromano.cplusplusparser.scanner.*;
import org.bromano.cplusplusparser.scanner.Scanner;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {


        if(args.length >= 1) {
            run(loadFile(args[0]));
            return;
        }

        run(readFromSystemIn());
    }

    public static void run(String text) {
        Scanner scanner = new SimpleScanner(text);

        List<Token> tokens = null;
        boolean success = false;

        try {
            tokens = scanner.lex();
            success = true;
        } catch (ScannerException exception) {
            if(scanner.getTokens() == null) {
                return;
            }

            scanner.getTokens().forEach(System.out::println);

            exception.printStackTrace(System.out);
        }

        if (!success) {
            return;
        }

        printTokens(tokens);

        Parser parser = new SimpleParser(tokens);

        try {
            Stack<String> parseTree = parser.parse();
            printStack(parseTree);
        } catch (ParserException exception) {

            if(parser.getTree() != null) {
                printStack(parser.getTree());
            }

            exception.printStackTrace(System.out);
        }
    }

    public static void printStack(Stack<String> stack) {
        while(!stack.isEmpty()) System.out.println(stack.pop());
    }

    public static String readFromSystemIn() {
        java.util.Scanner systemInScanner = new java.util.Scanner(System.in);

        StringBuilder inputStringBuilder = new StringBuilder();
        while(systemInScanner.hasNext()) {
            inputStringBuilder.append(systemInScanner.nextLine());
        }

        return inputStringBuilder.toString();

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
            int c;
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
