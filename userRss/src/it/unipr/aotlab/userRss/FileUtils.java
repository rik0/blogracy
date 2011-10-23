package it.unipr.aotlab.userRss;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: enrico
 * Date: 10/23/11
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileUtils {
    public static void copyTextualFile(BufferedWriter bw, BufferedReader br) throws IOException {
        String line;
        while((line = br.readLine()) != null) {
            bw.write(line);
            bw.newLine();
        }
    }

}
