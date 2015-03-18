package car;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
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
        Boolean isRoot = null;
        String urlRoot = "http://localhost:8080/rest/api/rest";
        ServerSocket connData = null;
        /**
         * Initialise une connection client avec le ftp.
         * @throws IOException Si la connection avec le serveur n'a pas pu etre faite.
         */
        public PasserelleRest() throws IOException {
            this.currentDirectory = new String();
            
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
            System.out.println("current directory = " + this.currentDirectory);
            
        }
        
 	@GET
        @Path("{name: .*\\..+}")
	@Produces("application/octet-stream")
    	public Response downloadFile( @PathParam("name") String name ) throws FileNotFoundException, IOException {
            String tmp = this.currentDirectory + "/" + name;
            int indexOf = tmp.lastIndexOf("/");
            System.out.println(tmp);
            System.out.println(indexOf);
            System.out.println(tmp.substring(0, indexOf));
            ftp.changeWorkingDirectory(tmp.substring(0, indexOf));
            //if(!this.connData.isClosed())
                this.connData = new ServerSocket(12002);
            ftp.port(InetAddress.getLocalHost(), 12002);
            Socket socket = connData.accept();

            ftp.retr(name);
//            Response resp = Response.ok(socket.getInputStream()).build();
//            socket.close();
            return Response.ok(socket.getInputStream()).build();
        }   
        /**
         * Gère l'affichage des éléments présent dans tous les autres dossiers
         * @param name le chemin de la ressource
         * @return la liste des fichiers/dossiers présent ainsi que leur url
         * @throws FileNotFoundException
         * @throws IOException 
         */
	@GET
        @Path("{name: [^\\.]*}")
	@Produces("text/html")
	 public String listDirectory( @PathParam("name") String name ) throws FileNotFoundException, IOException {
            String res = new String();
            String urlRootCurrent = this.urlRoot;
            String[] tmp = name.split("\\/");
            
            if(tmp[tmp.length-1].contains(".")) {
                System.out.println(tmp[tmp.length-1]);
            }
            
            else {
                /* on verifie si nous somme à la racine de l'url. Si c'est le cas, on gere le nom du dossier root un peut differement */
                if(name.equals("")) {
                    this.isRoot = true;
                }
                else {
                    this.isRoot = false;
                    urlRootCurrent += "/"; 
                }

                /* a chaque connection, on doit aller dans le dossier correspondant pour pouvoir lister le contenu du dossier */
                ftp.changeWorkingDirectory(this.currentDirectory + "/" + name);
                /* on recupere le conetnu du dossier */
                FTPFile[] files = ftp.listFiles(this.currentDirectory + "/" + name);
                /* on gere la navigation vers le dossier parent */
                if(this.isRoot != true) {
                    String url = urlRootCurrent + name;
                    int lastIndex = url.lastIndexOf("/");
                    res += "<a href=\"" + url.substring(0, lastIndex) +"\">..</a><br />";
                }
                /* on liste tout ce qu'il y a dans le dossier */

                for (FTPFile file : files) {
                    if(!file.getName().equals("."))
                        if(!file.getName().equals(".."))
                            res += "<a href=\"" + urlRootCurrent + name + "/" + file.getName() +"\">" + file.getName() + "</a><br />";
                }
                }
            return res; 
	 }

         // PUT pour uploader le fichier
         
         /**
          * Pour creer un dosseir
          * @param name
          * @return
          * @throws FileNotFoundException
          * @throws IOException 
          */
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

