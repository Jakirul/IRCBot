import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static String bot_nickname;
    private static String bot_Username;
    private static String bot_actualname;
    private static PrintWriter bot_output;
    private static Scanner bot_input;
    private static String bot_ipaddr;
    private static int bot_port;
    private static String channel;
    private static String yes;

    public static void main(String[] args) throws IOException {
        //initialising the scanner. scanner is a way of reading inputs
        Scanner scanningtext = new Scanner(System.in);

        //it asks the user to enter their nickname
        System.out.print("Enter a nickname: ");
        bot_nickname = scanningtext.nextLine();
        //asks the user to enter a username
        System.out.print("Enter a username: ");
        bot_Username = scanningtext.nextLine();
        //asks the user to enter their full name
        System.out.print("Enter full name here: ");
        bot_actualname = scanningtext.nextLine();
        //asks the user to enter the channel they want to enter
        System.out.print("Please enter the channel you want to join with a # (e.g. #help): ");
        channel = scanningtext.nextLine();
        //asks the user to enter the IRC ip address or server address
        System.out.print("Please enter the IP Address or Server Address: ");
        bot_ipaddr = scanningtext.nextLine();
        //asks the user to enter the port number
        System.out.print("Please enter the port number: ");
        bot_port = scanningtext.nextInt();


        //this is the connection of the IRC. it takes in the ip address and also the port.
        Socket networkConn = new Socket(bot_ipaddr, bot_port);

        //printwriter is a way of outputting text and send it to the server
        bot_output = new PrintWriter(networkConn.getOutputStream(),true);
        //scanner is a way of reading inputs. scanner has an input stream
        bot_input = new Scanner(networkConn.getInputStream());

        //this sets the nickname of the user with the NICK IRC command
        commands("NICK", bot_nickname);
        //this sets the username of the user and shows their full name as well
        commands("USER", bot_Username + " 8 * :" + bot_actualname);
        //JOIN allows for the user to join the channel they wrote
        commands("JOIN", channel);
        /* PRIVMSG is a way of sending messages in a channel. this sends a message saying "joined server" when
        the user joins the channel */
        commands("PRIVMSG " + channel, " :Joined server.\r\n");


        //hasnext returns true if this scanner has another token in its input.
        while (bot_input.hasNext()) {
            //allows for inputs
            String servmsg = bot_input.nextLine();
            //prints the output with an arrow facing left and the message itself
            System.out.println("<---- " + servmsg);

            //if a PING shows up, then a PONG will be returned.
            if (servmsg.startsWith("PING")) {
                String pings = servmsg.split(" ", 2)[1];
                commands("PONG", pings);
            }

            //!hello returns a message saying "Hello there!"
            if (servmsg.contains("!hello")) {
                commands("PRIVMSG " + channel, " :Hello there!\r\n");
            }

            //Welcomes a new user. I did 2 copies as the city IRC channel has a colon after the join
            if (servmsg.contains("JOIN :" + channel) ) {
                if (!servmsg.contains(bot_nickname)) {
                    commands("PRIVMSG " + channel, " :Welcome to the " + channel + " channel!");
                }
            }

            //Welcomes a new user
            if (servmsg.contains("JOIN " + channel) ) {
                if (!servmsg.contains(bot_nickname)) {
                    commands("PRIVMSG " + channel, " :Welcome to the " + channel + " channel!");
                }
            }

            //!help returns a few commands to start the user off
            if (servmsg.contains("!help")) {
                commands("PRIVMSG " + channel, " :Just type in commands starting with ! such as !hello, !commands and !help \r\n");
            }

            //you can flip a coin
            if(servmsg.contains("!flip")) {
                if (Math.random() < 0.5){
                    commands("PRIVMSG " + channel, " :Heads");
                }else{
                    commands("PRIVMSG " + channel, " :Tails");
                }
            }

            //!commands returns all the commands available
            if (servmsg.contains("!commands")) {
                commands("PRIVMSG " + channel, " :Commands available:");
                commands("PRIVMSG " + channel, " :!hello - returned 'Hello there!'");
                commands("PRIVMSG " + channel, " :!commands - returns all the commands ");
                commands("PRIVMSG " + channel, " :!help - provides help");
                commands("PRIVMSG " + channel, " :!timeis - current time");
                commands("PRIVMSG " + channel, " :!nameis - all users of the system");
                commands("PRIVMSG " + channel, " :!exitchannel - exits the current channel");
                commands("PRIVMSG " + channel, " :!dateis - current date ");
                commands("PRIVMSG " + channel, " :!textfile - returns text file");
                commands("PRIVMSG " + channel, " :!disconnbot - disconnects the bot");
                commands("PRIVMSG " + channel, " :!myname - your full name");
                commands("PRIVMSG " + channel, " :!flip - you can flip a coin");

            }

            //!timeis returns the current local time
            if (servmsg.contains("!timeis")) {
                commands("TIME", bot_nickname);

            }

            //!exitchannel exits the current channel
            if (servmsg.contains("!exitchannel")) {
                commands("PART", channel);

            }


            //returns all the users of the channel
            if (servmsg.contains("!names")) {
                commands("NAMES ", channel);

            }

            //!dateis returns the current date
            if (servmsg.contains("!dateis")) {
                LocalDate date = java.time.LocalDate.now();
                commands("PRIVMSG " + channel, " :The date is now: " + date);
            }


            //takes in the text file
            BufferedReader bfrRedr = new BufferedReader(new FileReader("src/textfile.txt"));
            try {
                StringBuilder stringBuild = new StringBuilder();
                String strngLine = bfrRedr.readLine();

                while (strngLine != null) {
                    stringBuild.append(strngLine);
                    stringBuild.append(System.lineSeparator());
                    strngLine = bfrRedr.readLine();
                }
                String outputTxt = stringBuild.toString();

                //!textfile returns the text file from src/textfile.txt
                if (servmsg.contains("!textfile")) {
                    commands("PRIVMSG " + channel, " :" + outputTxt);
                }
            } finally {
                //closes the buffer reader
                bfrRedr.close();
            }

            //!disconnbot disconnects the bot from the server
            if (servmsg.contains("!disconnbot")) {
                //uses the PRIVMSG IRC command to print the goodbye message
                commands("PRIVMSG " + channel, " :Bot is disconnecting, goodbye.");
                //uses the QUIT IRC command for the bot to leave the server.
                commands("QUIT", channel);
            }

            //shows the users name
            if (servmsg.contains("!myname")) {
                commands("PRIVMSG " + channel, " :Your name is: " + bot_actualname);
            }

        }


        //closes the input streams
        bot_input.close();
        //closes the output streams
        bot_output.close();
        //closes the socket connection
        networkConn.close();
        //prints "Disconnected Successfully" into the console once you've disconnected
        System.out.println("Disconnected Successfully.");
    }


    //this is to add the messages as shown above with the "commands(....)" tag
    private static void commands(String theCommand, String theMessage) {
        //this is the message with the command, a space in between and the message itself
        String msg = theCommand + " " + theMessage;
        //adds an arrow next to the message
        System.out.println("----> " + msg);
        //prints the output with the message and a new line
        bot_output.print(msg + "\r\n");
        //flushes the output
        bot_output.flush();
    }

}