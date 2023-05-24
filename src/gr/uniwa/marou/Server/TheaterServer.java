package gr.uniwa.marou.Server;

import gr.uniwa.marou.DAO.TheaterDAOImpl;
import gr.uniwa.marou.Service.TheaterRMIService;
import gr.uniwa.marou.Service.TheaterServiceImpl;
import gr.uniwa.marou.model.Theater;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 The TheaterServer class is responsible for creating an RMI server that provides access to the TheaterService.
 The server is created by binding the remote TheaterService object to a URL using the Naming class, and registering it
 with the RMI registry on the specified port.
 */
public class TheaterServer {
    /**
     * The main method is the entry point of the application.
     * It creates an instance of the TheaterService implementation (TheaterServiceImpl), which in turn
     * is initialized with a TheaterDAOImpl object (which is responsible for accessing the underlying data).
     * The TheaterService object is then bound to a URL using the Naming class, and registered with the RMI registry.
     * @param args An array of command-line arguments.
     */
    public static void main(String[] args){
        try {
            LocateRegistry.createRegistry(9999);
            TheaterRMIService lServer = new TheaterServiceImpl(new TheaterDAOImpl(new Theater()));
            String url = "rmi://" + InetAddress.getLocalHost().getHostAddress() + ":9999/TheaterService";
            Naming.rebind(url, lServer);  //create rmi server
            System.out.println("Theater gr.uniwa.marou.Server is ready for operations.");
        } catch (RemoteException e) {
            System.out.println("Trouble: " + e);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
        }
    }
}
