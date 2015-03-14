package car;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Exemple de ressource REST accessible a l'adresse :
 * 
 * 		http://localhost:8080/rest/api/rest
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@univ-lille1.fr>
 */
@Path("/rest")
public class PasserelleRest {
        FTPClient ftp = new FTPClient();
        FTPClientConfig config = new FTPClientConfig();
        int reply;
        String currentDirectory = null;
        String rootDirectory = null;
        String urlRoot = "http://localhost:8080/rest/api/rest";
        /**
         * Initialise une connection client avec le ftp.
         * @throws IOException Si la connection avec le serveur n'a pas pu etre faite.
         */
        public PasserelleRest() throws IOException {
            this.currentDirectory = new String();
            this.rootDirectory = new String();
            
            ftp.configure(config);
            ftp.connect("127.0.0.1", 4000);

            
            /* we make sure we are in passive mode */
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
            this.rootDirectory = this.currentDirectory;
            System.out.println("current directory = " + this.currentDirectory);
            
        }
        
        /**
         * Gère l'affichage des éléments présent dans le dossier racine
         * @return la liste des fichiers/dossiers présent ainsi que leur url
         * @throws IOException 
         */
	@GET
	@Produces("text/html")
	public String listRoot() throws IOException {
            String res = new String();
            ftp.changeWorkingDirectory(this.rootDirectory);

            FTPFile[] files = ftp.listFiles(this.currentDirectory);
            System.out.println("buffer size " + ftp.getBufferSize());
            System.out.println(files.length);
            for (FTPFile file : files) {
                res += "<a href=\"" + this.urlRoot + file.getName() +"\">" + file.getName() + "</a><br />";
            }
            
            return res;
	}
        /**
         * Gère l'affichage des éléments présent dans tous les autres dossiers
         * @param name le chemin de la ressource
         * @return la liste des fichiers/dossiers présent ainsi que leur url
         * @throws FileNotFoundException
         * @throws IOException 
         */
	@GET
        @Path("/{name: .*}")
	@Produces("text/html")
	 public String listDirectory( @PathParam("name") String name ) throws FileNotFoundException, IOException {
            String res = new String();
            System.out.println(name);
            ftp.changeWorkingDirectory(this.currentDirectory + "/" + name);
            FTPFile[] files = ftp.listFiles(this.currentDirectory + "/" + name);
            for (FTPFile file : files) {
                res += "<a href=\"" + this.urlRoot + "/" + name + "/" + file.getName() +"\">" + file.getName() + "</a><br />";
            }
            
            return res; 
	 }

	 @POST
	 @Path("/{name}")
	 public String getBook( @PathParam("name") String name ) throws FileNotFoundException, IOException {
//            File f = new File("/home/rkouere/fac/S2/car/CAR_TP1/Ftp/src/ftpRoot/aaa.txt");
//            BufferedInputStream  fin = new BufferedInputStream(new FileInputStream(f));
//            ftp.storeFile("src/ftpRoot/aa.txt", fin);
            return "Book: "+name;		 
	 }

	 @GET
	 @Path("{var: .*}/stuff")
	 public String getStuff( @PathParam("var") String stuff ) {
		 return "Stuff: "+stuff;
	 }
}

