import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

public class RoomHandler {
    private static RoomHandler handler;
    private Hashtable<Integer, Room> mRoomList;
    private ArrayList<Integer> mWaitList;
    private Random mRandom = new Random();

    public static RoomHandler getInstance() {
        if(handler == null) {
            handler = new RoomHandler();
        }

        return handler;
    }

    public RoomHandler() {
        mRoomList = new Hashtable<Integer, Room>();
        mWaitList = new ArrayList<Integer>();
        mRoomList.put(0, new Room());
    }

    // 방에 들어가기
    public void joinUser(User _user) {
        // 이미 방에 들어가있다면 반환
        if (_user.getRoom() != 0)
            return;

        getRoom(0).removeUser(_user);
        // 대기 리스트에 방이 없을 경우
        if (mWaitList.isEmpty()) {
            // 방을 만들자
            Room room = new Room(_user, getRoomIndex());
            mRoomList.put(room.getID(), room);
            mWaitList.add(room.getID());
            String packet = Packet.notify("접속자를 기다리는 중입니다..");
            _user.getSocket().send(packet);
        } else {
            // 방이 있을 경우 들어감
            Integer roomId = mWaitList.get(mRandom.nextInt(mWaitList.size()));
            Room room = mRoomList.get(roomId);
            // 대기 리스트에서 지움
            if (room.addUser(_user)) {
                mWaitList.remove(roomId);
                String packet = Packet.loginMessage(_user.getID(), 0);
                room.sendToAll(packet);
            }
        }
    }

    // 방에서 나가기
    public void exitUser(User _user) {
        if (_user.getRoom() == 0) {
            mRoomList.get(0).removeUser(_user);
            return;
        }

        // 해당 유저는 전체 방으로 가야지
        Integer beforeRoom = _user.getRoom();
        String packet = Packet.notify("방에서 나왔습니다.");
        _user.getSocket().send(packet);
        getRoom(0).addUser(_user);

        // 대기 목록에 있다면
        if (mWaitList.contains(beforeRoom)) {
            mWaitList.remove(beforeRoom);
            mRoomList.remove(beforeRoom);
            return;
        }

        // 대화 중이었다면
        Room myRoom = mRoomList.getOrDefault(beforeRoom, null);
        if (myRoom == null)
            return;

        // 방에서 나갔음을 알림
        myRoom.removeUser(_user);
        packet = Packet.notify("상대방이 나갔습니다. 다음 상대를 기다리는 중입니다..");
        myRoom.sendToAll(packet);

        // 대화 상대가 없으면 대기 리스트에 추가
        if (myRoom.isWait())
            mWaitList.add(myRoom.getID());
    }

    public Room getRoom(int _roomID) {
        if (!mRoomList.containsKey(_roomID))
            return null;

        return mRoomList.get(_roomID);
    }

    public int getRoomIndex() {
        for (int i = 1; i < 10000; i++) {
            if (!mRoomList.containsKey(i))
                return i;
        }

        return -1;
    }
}
