package car;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

/**
 * Implémentation de la plateforme REST
 * 
 * 		http://localhost:8080/rest/api/rest/list/
 * 
 * @author Nicolas Echallier -  <Lionel.Seinturier@univ-lille1.fr>
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
        
        /* the text present in the form used to delete / upload files */
        String form = new String();
        /**
         * Initialise une connection client avec le ftp.
         * @throws IOException Si la connection avec le serveur n'a pas pu etre faite.
         */
        public PasserelleRest() throws IOException {
            this.currentDirectory = new String();
            this.form = "<div><h1 style='font-size:1.2em; font-family: sans'>Téléverser un fichier</h1><form method='POST' action='http://localhost:8080/rest/api/rest/upload' enctype='multipart/form-data'>\n" +
                "Choisir le fichier<input type='file' name='file'><br> nom de la destination : <input type='text' name='name' /><br />\n" +
                "<input type='submit' value='Téléverser'>\n" +
                "</form> </div>" + 
                "<div><h1 style='font-size:1.2em; font-family: sans'>Supprimer un fichier</h1><form method='POST' action='http://localhost:8080/rest/api/rest/delete'><input type='text' name='name' />" + 
                "<input type='submit' value='Delete'></form></div>";
                    
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
         * @return Le fichier si il existe ou une erreur 404 si il n'existe pas.
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
            if(reply == 550) {
                serv.close();
                return Response.status(NOT_FOUND).entity("uploadFile is called, Uploaded file name : ").build();
            }
            Response resp = Response.ok(socket.getInputStream()).build();
            serv.close();
         return resp;
        }   


        /**
         * Gère l'affichage des éléments présent dans tous les dossiers
         * @param name le chemin de la ressource
         * @return la liste des fichiers/dossiers présent ainsi que leur url
         * @throws FileNotFoundException
         * @throws IOException 
         */
	@GET
        @Path("list/{name: [^\\.]*}")
	@Produces("text/html; charset=UTF-8")
	 public String listDirectory( @PathParam("name") String name ) throws FileNotFoundException, IOException {
            String res = new String();        
           /* on recuper le nom de l'url que nous allon sutiliser piur naviguer dans les dossiers du ftp */
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
                    //urlRootCurrent += "/"; 
                }

                /* a chaque connection, on doit aller dans le dossier correspondant pour pouvoir lister le contenu du dossier */
                /* nous sommes obligé de faire cela car sinon nous pouvons avoir des problèmes a la permiere connection*/
                this.ftp.changeWorkingDirectory(this.currentDirectory + "/" + name);
                /* si le dossier n'existe pas */
                if(ftp.getReplyCode() == 521)
                    return "<div><b>URL inconnu</b></div><div><a href='" + this.urlRoot + "'>Retourner à la page d'acceuil</a></div>";
                /* on recupere le conetnu du dossier */
                FTPFile[] files = ftp.listFiles(this.currentDirectory + "/" + name);
                
                /* on rajoute le formulaire necessaire au down/upload */
                res += this.form;
                
 
                /* on liste tout ce qu'il y a dans le dossier */
                for (FTPFile file : files) {
                    if(!file.getName().equals("."))
                        if(!file.getName().equals(".."))
                            res += "<a href=\"" + this.urlRoot + name + ((this.isRoot == false) ? "/" : "") + file.getName() +"\">" + file.getName() + "</a> <br />";                                   
                }
            }
            return res; 
	 }

         // PUT pour uploader le fichier
         
        /**
         * Téléverse le fichier du client sur le ftp
         * @param fichier Le stream a envoyer sur le serveur
         * @param name Le nom de fichier a utiliser sur le serveur
         * @return Un message indiquant à l'utilisateur si le fichier a bien été téléversé sur le ftp.
         * @throws IOException 
         */
        @POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("text/html; charset=UTF-8")
	@Path("/upload")
	public String upload( @Multipart("file") InputStream fichier, @Multipart("name") String name) throws IOException {
                String back = "<p style='color:red'>Attention, ce boutton ne recharge pas la page, vous ne verrez donc le fichier que lorsque vous l'aurez rechargée.</p>"+
                "<input action='action' type='button' value='Back' onclick='history.go(-1);' />";
                System.out.println(name);
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

        @POST
	@Produces("text/html; charset=UTF-8")
	@Path("/delete")
	public String delete(@Multipart("name") String name) throws IOException {
                String back = "<p style='color:red'>Attention, ce boutton ne recharge pas la page, vous ne verrez donc le fichier que lorsque vous l'aurez rechargée.</p><input action='action' type='button' value='Back' onclick='history.go(-1);' />";
                String[] fileName = name.split("=");
                System.out.println(name);
                /* on verifie que le nom n'a pas un espace (fini par un +)*/
                if(fileName[1].endsWith("+"))
                    name = fileName[1].substring(0, fileName[1].length()-1);
                else
                    name = fileName[1];
                System.out.println(name);
		this.ftp.deleteFile(name); 
                int reply = this.ftp.getReplyCode();
                System.out.println(reply);
                if(reply == 250)
                    back += "<h1>Le fichier " + name + " a bien été supprimé du serveur</h1>";
		//this.ftp.stor(file);
                else
                    back += "<h1>Erreur " + reply + "</h1><p>Le fichier n'a pas pu etre effece</p>";
		return back;
	}


	 @GET
	 @Path("{var: .*}/stuff")
	 public String getStuff( @PathParam("var") String stuff ) {
		 return "Stuff: "+stuff;
	 }
}

