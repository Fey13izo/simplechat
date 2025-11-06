// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.ui;

import java.io.*;
import java.util.Scanner;

import edu.seg2105.client.backend.ChatClient;
import edu.seg2105.client.common.ChatIF;

/**
 * This class constructs the UI for a chat client.  It implements ChatIF so
 * it can display messages that arrive from the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Laganière
 * @author François Bélanger
 */
public class ClientConsole implements ChatIF 
{
  //Class variables *************************************************
  
  /**
   * The default port to connect on.
   */
  final public static int DEFAULT_PORT = 5555;

  //Instance variables **********************************************

  /**
   * The client controlled by this interface
   */
  ChatClient client;

  /**
   * Scanner to read from the console
   */
  Scanner fromConsole;

  
  //Constructors ****************************************************

  /**
   * Constructs an instance of the ClientConsole UI.
   *
   * @param loginID The login id for the client.
   * @param host The host to connect to.
   * @param port The port to connect on.
   */
  public ClientConsole(String loginID, String host, int port)
  {
    try
    {
      client = new ChatClient(loginID, host, port, this);
    }
    catch(IOException ex)
    {
      System.out.println("ERROR - Could not connect!  Terminating client.");
      System.exit(1);
    }

    // Create scanner object to read from console
    fromConsole = new Scanner(System.in);

    // Listening for console input
    acceptConsoleInput();

  }

  
  //Instance methods ************************************************

  /**
   * This method waits for console input and sends it to the client's
   * message handler.
   */
  public void acceptConsoleInput()
  {
    try
    {

      BufferedReader reader =
        new BufferedReader(new InputStreamReader(System.in));

      String message;

      while (true)
      {
        message = reader.readLine();
        client.handleMessageFromClientUI(message);
      }
    }
    catch (Exception ex)
    {
      System.out.println
        ("Unexpected error while reading from console!");
    }
  }

  /**
   * This method is required by the ChatIF interface but is
   * used in the case that the server is running as a console
   * application.
   *
   * @param message The message to be displayed.
   */
  public void display(String message)
  {
    System.out.println("> " + message);
  }

  
  //Class methods ***************************************************

  /**
   * This method is responsible for the creation of the Client UI.
   *
   * @param args[0] The login id. Mandatory.
   * @param args[1] The host to connect to.  Optional, defaults to 'localhost'.
   * @param args[2] The port to connect on.  Optional, defaults to 5555.
   */
  public static void main(String[] args) 
  {
    String loginID = null;
    String host = "localhost";
    int port = DEFAULT_PORT;

    try
    {
      loginID = args[0]; // Login ID is mandatory
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
      System.out.println("ERROR - No login ID specified.  Terminating client.");
      System.exit(1);
    }

    try
    {
      host = args[1]; // Host is optional
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
      // Use default
    }

    try
    {
      port = Integer.parseInt(args[2]); // Port is optional
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
      // Use default
    }
    catch(NumberFormatException e)
    {
      System.out.println("ERROR - Invalid port number.  Using default.");
    }

    ClientConsole chat = new ClientConsole(loginID, host, port);
  }
}
//End of ClientConsole class

