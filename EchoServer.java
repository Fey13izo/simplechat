package edu.seg2105.server.backend;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 


import ocsf.server.*;
import edu.seg2105.server.ui.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  
  /**
   * The server console.
   */
  ServerConsole serverUI;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   * @param serverUI The server console interface.
   */
  public EchoServer(int port, ServerConsole serverUI) 
  {
    super(port);
    this.serverUI = serverUI;
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
  {
    String message = msg.toString();
    
    // Check if this is a login command
    if (message.startsWith("#login"))
    {
      handleLogin(message, client);
    }
    else
    {
      // Check if client has logged in
      if (client.getInfo("loginID") == null)
      {
        try
        {
          client.sendToClient("Error: Must login first.");
          client.close();
        }
        catch(IOException e)
        {
          serverUI.display("Error closing client connection.");
        }
      }
      else
      {
        // Prefix message with login ID
        String loginID = (String) client.getInfo("loginID");
        String prefixedMessage = loginID + ": " + message;
        
        System.out.println("Message received: " + prefixedMessage + " from " + client);
        this.sendToAllClients(prefixedMessage);
      }
    }
  }
  
  /**
   * This method handles the login command from a client.
   *
   * @param message The login message from the client.
   * @param client The connection from which the message originated.
   */
  private void handleLogin(String message, ConnectionToClient client)
  {
    // Check if client is already logged in
    if (client.getInfo("loginID") != null)
    {
      try
      {
        client.sendToClient("Error: Already logged in.");
        client.close();
      }
      catch(IOException e)
      {
        serverUI.display("Error closing client connection.");
      }
    }
    else
    {
      String[] parts = message.split(" ", 2);
      if (parts.length > 1)
      {
        String loginID = parts[1];
        client.setInfo("loginID", loginID);
        serverUI.display(loginID + " has logged in.");
      }
      else
      {
        try
        {
          client.sendToClient("Error: Invalid login format.");
          client.close();
        }
        catch(IOException e)
        {
          serverUI.display("Error closing client connection.");
        }
      }
    }
  }
  
  /**
   * This method handles messages from the server console.
   *
   * @param message The message from the server console.
   */
  public void handleMessageFromServerUI(String message)
  {
    if (message.startsWith("#"))
    {
      handleCommand(message);
    }
    else
    {
      String prefixedMessage = "SERVER MSG> " + message;
      System.out.println(prefixedMessage);
      this.sendToAllClients(prefixedMessage);
    }
  }
  
  /**
   * This method handles commands from the server console.
   *
   * @param command The command to handle.
   */
  private void handleCommand(String command)
  {
    String[] parts = command.split(" ", 2);
    String cmd = parts[0].toLowerCase();
    
    try
    {
      switch(cmd)
      {
        case "#quit":
          if (isListening())
          {
            stopListening();
          }
          
          try
          {
            Thread.sleep(500); // Give clients time to disconnect
          }
          catch(InterruptedException e) {}
          
          // Close all existing client connections
          Thread[] clientThreads = getClientConnections();
          for (int i = 0; i < clientThreads.length; i++)
          {
            try
            {
              ((ConnectionToClient)clientThreads[i]).close();
            }
            catch(IOException e) {}
          }
          
          System.exit(0);
          break;
          
        case "#stop":
          if (isListening())
          {
            stopListening();
            serverUI.display("Server has stopped listening for new clients.");
          }
          else
          {
            serverUI.display("Server is not currently listening.");
          }
          break;
          
        case "#close":
          if (isListening())
          {
            stopListening();
          }
          
          // Close all existing client connections
          clientThreads = getClientConnections();
          for (int i = 0; i < clientThreads.length; i++)
          {
            try
            {
              ((ConnectionToClient)clientThreads[i]).close();
            }
            catch(IOException e) {}
          }
          
          serverUI.display("Server closed all client connections.");
          break;
          
        case "#setport":
          if (!isListening() && getNumberOfClients() == 0)
          {
            if (parts.length > 1)
            {
              try
              {
                int port = Integer.parseInt(parts[1]);
                setPort(port);
                serverUI.display("Port set to: " + port);
              }
              catch(NumberFormatException e)
              {
                serverUI.display("Error: Invalid port number.");
              }
            }
            else
            {
              serverUI.display("Error: No port specified.");
            }
          }
          else
          {
            serverUI.display("Error: Server must be closed to change port.");
          }
          break;
          
        case "#start":
          if (!isListening())
          {
            try
            {
              listen();
              serverUI.display("Server is now listening for new clients.");
            }
            catch(IOException e)
            {
              serverUI.display("Error: Could not start listening.");
            }
          }
          else
          {
            serverUI.display("Server is already listening.");
          }
          break;
          
        case "#getport":
          serverUI.display("Current port: " + getPort());
          break;
          
        default:
          serverUI.display("Error: Unknown command.");
          break;
      }
    }
    catch(Exception e)
    {
      serverUI.display("Error executing command: " + e.getMessage());
    }
  }
  
  /**
   * Hook method called after a client connects.
   *
   * @param client The connection to the client.
   */
  protected void clientConnected(ConnectionToClient client)
  {
    serverUI.display("A new client has connected.");
  }
  
  /**
   * Hook method called after a client disconnects.
   *
   * @param client The connection to the client.
   */
  protected void clientDisconnected(ConnectionToClient client)
  {
    String loginID = (String) client.getInfo("loginID");
    if (loginID != null)
    {
      serverUI.display(loginID + " has disconnected.");
    }
    else
    {
      serverUI.display("An unnamed client has disconnected.");
    }
  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }
  
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void main(String[] args) 
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    ServerConsole sv = new ServerConsole(port);
  }
}
//End of EchoServer class


