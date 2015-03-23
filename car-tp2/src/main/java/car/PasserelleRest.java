package car;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

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
        String urlRoot = "http://localhost:8080/rest/api/rest/list/";
        ServerSocket connData = null;
        Socket socketData = null;
        /**
         * Initialise une connection client avec le ftp.
         * @throws IOException Si la connection avec le serveur n'a pas pu etre faite.
         */
        public PasserelleRest() throws IOException {
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
            
        }
        
        /**
         * Permet de télécharger un fichier à partir du ftp sur le client
         * @param une URL qui finit en *.*
         * @return true si le fichier existe false si il n'existe as
         * @throws FileNotFoundException
         * @throws IOException 
         */
        @GET
        @Path("list/{name: .*\\..+}")
	@Produces("application/octet-stream")
    	public Response downloadFile( @PathParam("name") String name ) throws FileNotFoundException, IOException {
           ServerSocket serv = new ServerSocket(60000);
           ftp.port(InetAddress.getLocalHost(), 60000);
            String tmp = this.currentDirectory + "/" + name;
            String[] nameOfFile = name.split("\\/");
           Socket socket = serv.accept();
           
           int reply = ftp.retr(nameOfFile[nameOfFile.length - 1]);
            System.out.println(reply);
           if(reply != 150) {
               serv.close();
               return Response.ok("ca merde").build();
           }
           Response resp = Response.ok(socket.getInputStream()).build();
           serv.close();
        return resp;
        }   


        /**
         * Gère l'affichage des éléments présent dans tous les autres dossiers
         * @param name le chemin de la ressource
         * @return la liste des fichiers/dossiers présent ainsi que leur url
         * @throws FileNotFoundException
         * @throws IOException 
         */
	@GET
        @Path("list/{name: [^\\.]*}")
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
                
                /* on gere l'upload des fichiers */
                res += "<form method='POST' action='http://localhost:8080/rest/api/rest/upload' enctype='multipart/form-data'>\n" +
                "  <input type='file' name='file'><br> nom de la destination : <input type='text' name='name'/>\n" +
                "  <input type='submit' value='Submit'>\n" +
                "</form> ";
                
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
         * 
         * @param fichier Le stream a envoyer sur le serveur
         * @param name Le nom de fichier a utiliser sur le serveur
         * @return Un message indiquant à l'utilisateur si le fichier a bien été téléversé sur le ftp.
         * @throws IOException 
         */
        @POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	//@Produces("text/html")
	@Path("/upload")
	public String up( @Multipart("file") InputStream fichier,
					  @Multipart("name") String name) throws IOException {
                String back = "<input action='action' type='button' value='Back' onclick='history.go(-1);' />";
		this.ftp.storeFile(name, fichier); 
		fichier.close();
                int reply = this.ftp.getReplyCode();
                if(reply == 450)
                    back += "<h1>Erreur 450</h1><pUn fichier avec ce nom existe déjà.</p>";
		//this.ftp.stor(file);
                else
                    back += "<h1>Le fichier " + name + " a bien été téléversé sur le serveur</h1>";
		return back;
	}



	 @GET
	 @Path("{var: .*}/stuff")
	 public String getStuff( @PathParam("var") String stuff ) {
		 return "Stuff: "+stuff;
	 }
}

