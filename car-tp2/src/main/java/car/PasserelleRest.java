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
            System.out.println(this.currentDirectory);
            
        }
        
	@GET
	@Produces("text/html")
	public String listFiles() throws IOException {
            String res = new String();
            FTPFile[] files = ftp.listDirectories();
            System.out.println(files.length);
            for (FTPFile file : files) {
                System.out.println("yoyo" + file.getName());
                res += file.getName();
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

