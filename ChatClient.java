// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

import ocsf.client.*;

import java.io.*;

import edu.seg2105.client.common.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 */
public class ChatClient extends AbstractClient
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI;
  
  /**
   * The login id for this client.
   */
  String loginID;

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param loginID The login id for the client.
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  
  public ChatClient(String loginID, String host, int port, ChatIF clientUI) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
    this.loginID = loginID;
    openConnection();
    sendToServer("#login " + loginID);
  }

  
  //Instance methods ************************************************
    
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) 
  {
    clientUI.display(msg.toString());
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  public void handleMessageFromClientUI(String message)
  {
    // Check if the message is a command (starts with #)
    if (message.startsWith("#"))
    {
      handleCommand(message);
    }
    else
    {
      try
      {
        sendToServer(message);
      }
      catch(IOException e)
      {
        clientUI.display
          ("Could not send message to server.  Terminating client.");
        quit();
      }
    }
  }
  
  /**
   * This method handles commands from the client UI.
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
          quit();
          break;
          
        case "#logoff":
          if (isConnected())
          {
            closeConnection();
          }
          else
          {
            clientUI.display("Error: Not connected.");
          }
          break;
          
        case "#sethost":
          if (!isConnected())
          {
            if (parts.length > 1)
            {
              setHost(parts[1]);
              clientUI.display("Host set to: " + parts[1]);
            }
            else
            {
              clientUI.display("Error: No host specified.");
            }
          }
          else
          {
            clientUI.display("Error: Must log off before changing host.");
          }
          break;
          
        case "#setport":
          if (!isConnected())
          {
            if (parts.length > 1)
            {
              try
              {
                int port = Integer.parseInt(parts[1]);
                setPort(port);
                clientUI.display("Port set to: " + port);
              }
              catch(NumberFormatException e)
              {
                clientUI.display("Error: Invalid port number.");
              }
            }
            else
            {
              clientUI.display("Error: No port specified.");
            }
          }
          else
          {
            clientUI.display("Error: Must log off before changing port.");
          }
          break;
          
        case "#login":
          if (!isConnected())
          {
            openConnection();
            sendToServer("#login " + loginID);
          }
          else
          {
            clientUI.display("Error: Already connected.");
          }
          break;
          
        case "#gethost":
          clientUI.display("Current host: " + getHost());
          break;
          
        case "#getport":
          clientUI.display("Current port: " + getPort());
          break;
          
        default:
          clientUI.display("Error: Unknown command.");
          break;
      }
    }
    catch(IOException e)
    {
      clientUI.display("Error executing command: " + e.getMessage());
    }
  }
  
  /**
   * Hook method called after the connection has been closed.
   */
  protected void connectionClosed()
  {
    clientUI.display("Connection closed.");
  }
  
  /**
   * Hook method called each time an exception is thrown by the client's
   * thread that is waiting for messages from the server.
   *
   * @param exception the exception raised.
   */
  protected void connectionException(Exception exception)
  {
    clientUI.display("The server has shut down.");
    quit();
  }
  
  /**
   * This method terminates the client.
   */
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
}
//End of ChatClient class
