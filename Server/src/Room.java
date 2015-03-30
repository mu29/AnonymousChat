import java.util.ArrayList;

public class Room {
    private int mID;
    private int mMaxUser;
    private ArrayList<User> mUsers;

    public Room() {
        mID = 0;
        mMaxUser = 1000;
        mUsers = new ArrayList<>();
    }

    public Room(User _user, int _id) {
        mID = _id;
        mMaxUser = 2;
        mUsers = new ArrayList<>();
        mUsers.add(_user);
        _user.setRoom(mID);
    }

    public int getMaxUser() {
        return mMaxUser;
    }

    public void setMaxUser(int _value) {
        mMaxUser = _value;
    }

    public Integer getID() {
        return mID;
    }

    public boolean addUser(User _user) {
        if (mUsers.contains(_user))
            return false;

        if (mUsers.size() == mMaxUser)
            return false;

        mUsers.add(_user);
        _user.setRoom(mID);
        return true;
    }

    public boolean removeUser(User _user) {
        if (!mUsers.contains(_user))
            return false;

        mUsers.remove(_user);
        return true;
    }

    public boolean isWait() {
        return mUsers.size() == 1;
    }

    public void sendToAll(String _packet) {
        for (User user : mUsers)
            if (user.getSocket().isOpen() || user.getSocket().isConnecting())
                user.getSocket().send(_packet);
    }

    public void sendToOthers(String _packet, User _user) {
        for (User user : mUsers) {
            if (user.equals(_user))
                continue;

            if (user.getSocket().isOpen() || user.getSocket().isConnecting())
                user.getSocket().send(_packet);
        }
    }
}
