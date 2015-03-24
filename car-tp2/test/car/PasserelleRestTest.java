/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package car;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import javax.ws.rs.core.Response;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rkouere
 */
public class PasserelleRestTest {
    String form = "<form method='POST' action='http://localhost:8080/rest/api/rest/upload' enctype='multipart/form-data'>\n" +
                "<input type='file' name='file'><br> nom de la destination : <input type='text' name='name' /><br />\n" +
                "<input type='submit' value='Téléverse'>\n" +
                "</form> " + 
                "<form method='POST' action='http://localhost:8080/rest/api/rest/delete'><input type='text' name='name' />" + 
                "<input type='submit' value='Delete'></form>";
    
    /* besoin pour le client ftp */
    FTPClient ftp = new FTPClient();
    FTPClientConfig config = new FTPClientConfig();
    String currentDirectory = null;
    FTPFile[] files = null;

    public PasserelleRestTest() throws IOException {
        this.currentDirectory = new String();

        ftp.configure(config);
        ftp.connect("127.0.0.1", 4000);
        /* we make sure we are inputStream passive mode */
        //ftp.enterLocalPassiveMode();
        /* we check the connection is OK */
        int replyCode = ftp.getReplyCode();
        if(!FTPReply.isPositiveCompletion(replyCode)) {
            System.out.println("[passerelle] : Connection failed");
            return;
        }
        else {
            System.out.println("[passerelle] : Connection success");
        }

        boolean login = ftp.login("nico", "password");
        if (!login) {
            ftp.disconnect();
            System.out.println("Could not login to the server");
            return;
        }
        else {
            System.out.print("[passerelle]: " + ftp.getReplyString());
        }
        /* we set the current directory */
        this.currentDirectory = ftp.printWorkingDirectory();
        System.out.println("current directory = " + this.currentDirectory);
        this.files = ftp.listFiles(this.currentDirectory);
    }
    
    @BeforeClass
    public static void setUpClass() {
 
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    private String sendRequest(String url) throws MalformedURLException, IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // make sure it is a get
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        /* le réponse est renvoyé sous forme de stream */
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        /* var temporaire */
        String inputLine;
        StringBuffer response = new StringBuffer();
        
        /* recuperation du flux de données */
        while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
        }
        in.close();

        //print result
        return response.toString();
    }
   /**
     * test de la méthode list directory
     * Test que tous les fichiers présent dans le dossier du ftp sont eux aussi présent
     * 
     */
    @Test
    public void listDirectory() throws Exception {
        /* verifie que le formulaire ets bien présent */
        String resp = this.sendRequest("http://localhost:8080/rest/api/rest/list/");

        /* on recupere les fichiers présent dans le dossier et on verifie qu'ils sont tous renvoyé par la paserelle */
        for (FTPFile file : this.files) {
            if(!file.getName().equals("."))
                if(!file.getName().equals(".."))
                    assertTrue(resp.contains(file.getName()));
        }
        /* on verifie que les réponses ne sont pas toujours oui */
        assertFalse(resp.contains("grrr.txt"));
        
    }

    /**
     * Test of downloadFile method, of class PasserelleRest.
     */
    @Test
    public void testDownloadFileOK() throws Exception {
        URL obj = new URL("http://localhost:8080/rest/api/rest/list/" + this.files[2].getName());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // make sure it is a get
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        assertTrue(con.getResponseCode() == 200);
        con.disconnect();
        
    }
    
    @Test
    public void testDownloadFileKO() throws Exception {
        URL obj = new URL("http://localhost:8080/rest/api/rest/list/greterssfet.lkjsh");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // make sure it is a get
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        assertTrue(con.getResponseCode() == 404);
        con.disconnect();
 
    }


 
    
}
