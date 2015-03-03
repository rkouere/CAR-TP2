package car;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Exemple de ressource REST accessible a l'adresse :
 * 
 * 		http://localhost:8080/rest/api/helloworld
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@univ-lille1.fr>
 */
@Path("/rest")
public class PasserelleRest {
        FTPClient ftp = new FTPClient();
        FTPClientConfig config = new FTPClientConfig();
        int reply;
        String currentDirectory = new String();
        
        /**
         * Initialise une connection client avec le ftp.
         * @throws IOException Si la connection avec le serveur n'a pas pu etre faite.
         */
        public PasserelleRest() throws IOException {
            ftp.configure(config );
            ftp.connect("127.0.0.1", 4000);
            System.out.println("[passerelle]: connection to ftp server.");
            System.out.print("[passerelle]: " + ftp.getReplyString());
            ftp.login("nico", "password");
            System.out.print("[passerelle]: " + ftp.getReplyString());
            reply = ftp.getReplyCode();
            /* we set the current directory */
            this.currentDirectory = ftp.printWorkingDirectory();
            /* if we can't connect to the ftp */
            if(!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
                System.exit(1);
            }
        }
        
	@GET
	@Produces("text/html")
	public String sayHello() throws IOException {
            String res = new String();
            FTPFile[] files = ftp.listFiles(this.currentDirectory);
            for (FTPFile file : files) {
                res += file;
            }
            return "[passerelle]: " + res;

		//return "<h1>Hello World</h1>";
	}

	 @GET
	 @Path("/book/{isbn}")
	 public String getBook( @PathParam("isbn") String isbn ) {
		 return "Book: "+isbn;		 
	 }

	 @GET
	 @Path("{var: .*}/stuff")
	 public String getStuff( @PathParam("var") String stuff ) {
		 return "Stuff: "+stuff;
	 }
}

