import org.java_websocket.WebSocket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class User {
    private static ArrayList<String> headNames = new ArrayList<String>();
    private static ArrayList<String> tailNames = new ArrayList<String>();
    private Random r = new Random();

    private WebSocket mSocket;
    private String mID;
    private int mRoom;
    private boolean mAdmin = false;
    private boolean mIsolate = false;

    public User(WebSocket _socket) {
        mSocket = _socket;
        ArrayList<String> userIDs = Server.getUserIDs();

        do {
            mID = headNames.get(r.nextInt(headNames.size())) + " " + tailNames.get(r.nextInt(tailNames.size()));
        } while (userIDs.contains(mID));
    }

    public WebSocket getSocket() {
        return mSocket;
    }

    public String getID() {
        return mID;
    }

    public int getRoom() {
        return mRoom;
    }

    public void setRoom(int value) {
        mRoom = value;
    }

    public void setAdmin(boolean val) {
        mAdmin = val;
    }

    public boolean isAdmin() {
        return mAdmin;
    }

    public void setIsolate(boolean val) {
        mIsolate = val;
    }

    public boolean isIsolate() {
        return mIsolate;
    }

    public static void makeNamePool() {
        try {
            String s;

            BufferedReader heads = new BufferedReader(new FileReader("./Data/headNames.txt"));
            while ((s = heads.readLine()) != null)
                headNames.add(s);
            heads.close();

            BufferedReader tails = new BufferedReader(new FileReader("./Data/tailNames.txt"));
            while ((s = tails.readLine()) != null)
                tailNames.add(s);
            tails.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
