/*
Authors: Xavier Begerow and Sara Dokter
Creation Date: 1/30/22
Modification Date: 2/8/22
Purpose: Purpose of this program is to allow the user to access the MatheMagic server
to solve and utilize various functions. These functions include: login, solve, list, logout
and shutdown. 
*/

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.FileWriter;
import java.util.Scanner;


public class SingleServerJPB1 {
    
    private static final int SERVER_PORT = 8765;//port address
    public static int sentinel = 1; //allows the server to keep accepting clients until it is shutdown

    
    public static void main(String[] args) {
        try{
            File solutionfile = new File("root_solutions.txt");
            solutionfile.createNewFile();
            
            solutionfile = new File("john_solutions.txt");
            solutionfile.createNewFile();
            solutionfile = new File("sally_solutions.txt");
            solutionfile.createNewFile();
            solutionfile = new File("qiang_solutions.txt");
            solutionfile.createNewFile();
            solutionfile = new File("logins.txt");
            solutionfile.createNewFile();
            FileWriter mywriter = new FileWriter(solutionfile);
            mywriter.write("root root22\n" +
"john john22\n" +
"sally sally22\n" +
"qiang qiang22");
            mywriter.close();
        }
        catch(Exception e)
        {
            //sad
        }
        
        
        while(sentinel == 1){
            
            createCommunicationLoop();//activate!
        }
        
        try{
            System.out.println("deleting");
            File solutionfile = new File("root_solutions.txt");
            solutionfile.delete();
            System.out.println("deleted files");
            solutionfile = new File("john_solutions.txt");
            solutionfile.delete();
            solutionfile = new File("sally_solutions.txt");
            solutionfile.delete();
            solutionfile = new File("qiang_solutions.txt");
            solutionfile.delete();
            solutionfile = new File("logins.txt");
            solutionfile.delete();
            
        }
        catch(Exception e)
        {
            System.out.println("failed");//sad
        }
    }//end main
    
    public static void createCommunicationLoop() {
        try {
            String loggedinas = null;//keeps the name of the user for future reference
    
            //create server socket
            ServerSocket serverSocket = 
                    new ServerSocket(SERVER_PORT);//magically creates the server socket
            
            System.out.println("Server started at " +
                    new Date() + "\n");//documents activation time
            //listen for a connection
            //using a regular *client* socket
            Socket socket = serverSocket.accept();
            
            //now, prepare to send and receive data
            //on output streams
            DataInputStream inputFromClient = 
                    new DataInputStream(socket.getInputStream());
            
            DataOutputStream outputToClient =
                    new DataOutputStream(socket.getOutputStream());// classes used for giving and recieving data
            
            String strReceived;// this is going to be the messages recieved from the client
            
            while(loggedinas == null)//loop to prevent any use before logging in
            {
                strReceived = inputFromClient.readUTF();
                System.out.println(strReceived);//takes the user input and prints it in the server
                String[] arr =strReceived.split(" "); // turns the commands into pieces for checking
                if(arr[0].equalsIgnoreCase("login"))
                {
                    try{
                        try{//login takes modifers after the command so check if they have extra
                            arr[3] = "test";
                            outputToClient.writeUTF("301 message format error");//notify that these are illegal modifiers
                        }
                        catch(Exception e)
                        {
                            loggedinas = login(arr[1]+ " " +arr[2], outputToClient);//perform the login function
                        }
                        
                    }
                    catch(Exception e){
                        outputToClient.writeUTF("301 message format error");//this is if they had too few modifiers
                    } 
                }
                else
                {
                    outputToClient.writeUTF("300 invalid command. Please login first");//for if they try to issue commands other than login
               
                }
            }
            
            
            
            
            //FileWriter myWriter = new FileWriter( loggedinas + "_solutions.txt");
            //keep track of the solutions asked for and the answer
            
            
            //server loop listening for the client 
            //and responding
            while(true) {
                FileWriter myWriter = new FileWriter(loggedinas + "_solutions.txt", true);
                strReceived = inputFromClient.readUTF();
                System.out.println(strReceived);//server must display everything the client says
                String[] arr =strReceived.split(" ");//shutdown and logout arent moddable so they dont need to check if theres extra mods
                //solve and list check for extra mods
                
                if(arr[0].equalsIgnoreCase("solve")) {
                    myWriter.write(strReceived + "\n");//puts the inquiry into the file associated with the user
                   
                    solve(strReceived, outputToClient, myWriter);
                     
                    
                }
                else if(arr[0].equalsIgnoreCase("list")) {
                    list(strReceived, loggedinas, outputToClient);//calls the list function
                }
                else if(strReceived.equalsIgnoreCase("shutdown")) {
                    System.out.println("Shutting down server...");
                    outputToClient.writeUTF("200 OK");//tells client it succeeded
                    serverSocket.close();// close client
                    socket.close();//close the server socket. still not sure why we named it this way but baugh made this so i trust him
                    
                    sentinel = 0;
                    myWriter.close();
                    break;  //get out of loop
                }
                else if(strReceived.equalsIgnoreCase("logout")) {
                    System.out.println("Shutting down client...");
                    outputToClient.writeUTF("200 OK");
                    serverSocket.close();// we only close the client socket, "even though its named server socket, i dont know ask baugh why"
                    
                    break;  //get out of loop
                }
                else {
                    System.out.println("Unknown command received: " 
                        + strReceived);
                    outputToClient.writeUTF("300 invalid command  "
                            + "Please try again.");//these commands are kinda boring i wish i could have gotten fun with them but i dont wanna lose points and the rubric said to use these phrases 
                    
                }
                myWriter.close();
            }//end server loop
            
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch
    }//end createCommunicationLoop
    
    
    //login takes the userinput and client 
//it compares it to the login file line by line
//returns the name of the user it is currently logged in as
//tells the user yay or nay
    public static String login(String userinput, DataOutputStream outputToClient){
    try{//attempts to open the login file
        File file = new File("logins.txt");
        Scanner st = new Scanner(file);//reads file
        String checker = new String();//stores a line from the file
    
        while(st.hasNextLine()){//while there are still lines to be read, check each one against the users inputted login
            checker = st.nextLine();//read the line
            
            if(checker.equals(userinput)){//if they have given the right input
                String[] arr =checker.split(" ");//split the string into an array of words  
                outputToClient.writeUTF("SUCCESS");
                st.close();
                return arr[0];//return the username only
            }

        }
        st.close();
    }
    catch(Exception e){//if it failed to open files then tell the user
        System.out.println("associated files are missing");//honestly if something else goes wrong just restart it and try again i dont wanna know
    }
    
                try{//its so anoying every message to the client has to be in a try catch they cant just trust me
                    outputToClient.writeUTF("FAILURE: Please provide correct username and password.  Try again. ");
                }// this is for if none of the username/passwords match the input, aka they wasted the computers time
                catch(Exception e){
                    e.printStackTrace();
                }
return null;// keep the user name null so it doesnt advance
}
    
    
    
    //list takes the username, the input and the client 
//it checks if the user gave a good command and if it did which one
//creates a message of all the lines from the files that the user has access to and sends them to client
//finished
    public static void list(String userinput, String loggedinas, DataOutputStream outputToClient){ // lists the inputs and responses associated with that users account
    //it took me so long to figure out why the client would only recieve one line of this code and i finally realized
    // its because after it sends one message the client wants a turn to talk and waits for an input
    //but then the server recieves that input while the still trying to send the rest of this stuff line by line '
    // and it goes almost as crazy as i was trying to figure it out
    // plus i had to change every single output line like 4 times trying to fix that and finally go it all to turn into 
    //just one super long message sent at the end hallelujah
        String output = new String();//collects the megalong message
        if(userinput.equalsIgnoreCase("LIST -all")){ 
        if(loggedinas.equalsIgnoreCase("root")){//verifies root
            try{// will output the info if the files are there
                File solutionfile = new File(loggedinas + "_solutions.txt");//opens associated file
                Scanner st = new Scanner(solutionfile); //reads the file
                output+=("\n" +loggedinas);//add whos file it is to the message
                
                if(!st.hasNextLine())//check if theyve done anything
                {
                    output+=("\n    No interactions yet");//keeps spacing
                }
                
                while(st.hasNextLine()){//read contents of files
                    output+=("\n    " +st.nextLine()); //adds the next line and keeps spacing
                }
                st.close();
                st = new Scanner(solutionfile = new File("john_solutions.txt"));//gets the file for the other users
                output+=("\njohn"); // im not doing this for every single iteration of the same thing
                if(!st.hasNextLine())
                {
                    output+=("\n    No interactions yet");
                }
                
                while(st.hasNextLine()){//read contents of files
                    output+=("\n    " +st.nextLine()); //prints the nex
                }
                st.close();
                st = new Scanner(solutionfile = new File("sally_solutions.txt"));
               output+=("\nsally");
                if(!st.hasNextLine())
                {
                    output+=("\n    No interactions yet");
                }
                
                while(st.hasNextLine()){//read contents of files
                    output+=("\n    " +st.nextLine()); //prints the nex
                }
                st.close();
                st = new Scanner(solutionfile = new File("qiang_solutions.txt"));
                output+=("\nqiang");
                if(!st.hasNextLine())
                {
                    output+=("\n    No interactions yet");
                }
                
                while(st.hasNextLine()){//read contents of files
                    output+=("\n    " +st.nextLine()); //prints the nex
                }
                st.close();
                outputToClient.writeUTF(output);// gives the mega message to the client in one go like a good noodle
            }
            catch(Exception e){ //if the files aren't there
                System.out.println("associated files are missing");
            }
        }
        else{ // if the user is not logged in under root
            try{
                outputToClient.writeUTF("Error: you are not the root user");
            }
            catch(Exception e)
            {
                // wah wah wah something went wrong and i didnt tell you, deal with it
                //unless the client dies nothing will go wrong and if they did we have bigger problems
            }
        }
    }
    else if(userinput.equalsIgnoreCase("LIST")){ // regular list command
            try{ // attempt to open the files
                File solutionfile = new File(loggedinas + "_solutions.txt");
                Scanner st = new Scanner(solutionfile);//reads the file
                System.out.println("sending " + loggedinas+ "'s solution history.");
                
                if(!st.hasNextLine())// i already said im not doing this again
                {
                    output+=("\n    No interactions yet");
                }
                
                while(st.hasNextLine()){//read contents of files
                    output+=("\n    " +st.nextLine()); //prints the nex
                }
                st.close();
                outputToClient.writeUTF(loggedinas +output);
            }
            catch(Exception e){ //if file is not in the right location or doesnt exist
                System.out.println("associated files are missing");
            }
    }
    else{
        try{
            outputToClient.writeUTF("301 message format error");// if they gave list with illegal modifiers let those bad boys know
        }
        catch(Exception e){ 
                
            }
    }
}

    
      /*Function for user to use solve funtion. This function alllows the user to input any given 
    information to find the area and circumference of a circle or to find the perimeter
    and area of a rectangle. If they input anything incorrectly, the server will give them an error statement*/
    public static void solve (String userInput, DataOutputStream outputToClient, FileWriter myWriter)
    {
        
        String[] arr = userInput.split(" ");
       
        if (arr.length < 3)
        {
            //This will catch the error if the client doesn't put anything in after typing solve
            if (arr.length < 2)
            {
                try 
                {
                    outputToClient.writeUTF("301 message format error"); //Ouputting error statement
                    myWriter.write("301 message format error"+ "\n");

                    return;
                }
                catch (Exception e)
                {
                    
                }
                
            }
           //This will catch the error if the user does not input any values to solve the equations
            else 
            {
                //Catches the error if the person does not put any values for the rectangle
                if (arr[1].equalsIgnoreCase("-r"))
                {
                    try 
                    {
                        //Prints out error statement to client
                        outputToClient.writeUTF ("Error: No sides found.");
                        //Prints error statement to file
                        myWriter.write("Error: No sides found."+ "\n");
                        return;
                    }
                    
                    catch (Exception e)
                    {
                        
                    }
                }
                //Catches the error if the client doesn't put a value for the radius
                else if (arr[1].equalsIgnoreCase("-c"))
                {
                    try
                    {
                        //Prints error statement to client
                        outputToClient.writeUTF("Error: No radius found.");
                        //Prints error statement to file
                        myWriter.write("Error: No radius found."+ "\n");
                        return;
                    }
                    
                    catch (Exception e)
                    {
                        
                    }
                }
                //If the client types anything that does not match with the given commands,
                //it will output this error
                else 
                {
                    try 
                    {
                        //Prints error statement to client
                        outputToClient.writeUTF("300 invalid command. Please try again.");
                        myWriter.write("300 invalid command. Please try again. "+ "\n");
                        return;
                    }
                    
                    catch (Exception e)
                    {
                        
                    }
                }
            }
        }
        //Initialized to zero to help get the proper width before pulling the variables
        double width = 0;
        
        //User will input information and it will utilize the solve function
        if (arr[1].equalsIgnoreCase ("-r"))
        {
            if(arr.length > 4)
            {
             try 
                {
                    outputToClient.writeUTF("301 message format error"); //Ouputting error statement
                    myWriter.write("301 message format error"+ "\n");

                    return;
                }
                catch (Exception e)
                {
                    
                }   
            }
            double length = 0;
          //This will pull the variable from the array and make sure its a number
            try{
                 length = Double.parseDouble(arr[2]);
            }
            catch(Exception e)
            {
                try{
                    
                
                outputToClient.writeUTF("300 invalid command. Please try again.");
                        myWriter.write("300 invalid command. Please try again. "+ "\n");
                        return;
                }
                catch(Exception ex)
                {
                    
                }
            }
          
          
          try 
          {
             //This will pull the value of the width if the user inputs two values
              width = Double.parseDouble(arr[3]);
          }
          catch(NumberFormatException f)
          {
              try{
              outputToClient.writeUTF("300 invalid command. Please try again.");
                        myWriter.write("300 invalid command. Please try again. "+ "\n");
                        return;
              }
              catch(Exception fa)
              {
                  
              }
          }
          catch (Exception ex)
          {
             //If the user inputs only one value, it will count the samae value as its width
              width = Double.parseDouble(arr[2]);
          }
          
          
          double perimeter = (length * 2) + (width * 2) ; //perimeter of rectangle
          double recArea = (length * width); //area of rectangle
          
          //This will print the output statements
          try 
          {
              //Output to client
            outputToClient.writeUTF("The rectangle's perimeter is " + String.valueOf(perimeter) +
                " and the area is " + String.valueOf(recArea));
            //Print to file
            myWriter.write("The rectangle's perimeter is " + String.valueOf(perimeter) +
                " and the area is " + String.valueOf(recArea) + "\n");
            
          }
          
          catch (Exception e)
          {
              
          }
         
        }
        
        else if (arr[1].equalsIgnoreCase ("-c"))
        {
            
            if(arr.length > 3)
            {
             try 
                {
                    outputToClient.writeUTF("301 message format error"); //Ouputting error statement
                    myWriter.write("301 message format error"+ "\n");

                    return;
                }
                catch (Exception e)
                {
                    
                }   
            }
            double radius = 0;
            try{
                 radius = Double.parseDouble(arr[2]);
            }
            catch(Exception e)
            {
                try{
                    
                
                outputToClient.writeUTF("300 invalid command. Please try again.");
                        myWriter.write("300 invalid command. Please try again. "+ "\n");
                        return;
                }
                catch(Exception ex)
                {
                    
                }
            }
            
            
            double circumference;
            double areaCircle; 
           
            circumference = 2 * (3.14) * radius;
            areaCircle = 3.14 * (radius * radius);
            
            //This will pring the output statements
            try
            {
                //Print to client screen
                outputToClient.writeUTF("The circle's circumference is " + String.valueOf(circumference) 
                   + " and the area is " + String.valueOf(areaCircle) );
                //Print to the file
                myWriter.write("The circle's circumference is " + String.valueOf(circumference) 
                   + " and the area is " + String.valueOf(areaCircle) + "\n");
            }
            
            catch (Exception e)
            {
                
            }
        }
        
        
    }


}