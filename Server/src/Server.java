import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;

public class Server extends WebSocketServer {
    private static Server chatServer = null;
    private RoomHandler roomHandler = null;
    private Hashtable<WebSocket, User> users;
    private ArrayList<String> banWords;

    public static void main(String[] args) {
        chatServer = Server.getInstance();

        System.out.println("서버를 시작합니다. (" + chatServer.getPort() + ")");
        Runtime.getRuntime().addShutdownHook(closeThread);
    }

    public static Server getInstance() {
        if(chatServer == null) {
            chatServer = new Server(8818);
            chatServer.start();
        }

        return chatServer;
    }

    public ArrayList<String> getUserIDs() {
        ArrayList<String> userIDs = new ArrayList<>();
        for (User user : users.values()) {
            userIDs.add(user.getID());
        }
        return userIDs;
    }

    public Server(int port) {
        super(new InetSocketAddress(port));
        roomHandler = RoomHandler.getInstance();
        users = new Hashtable<>();
        banWords = new ArrayList<>();

        User.makeNamePool();
        makeBanWords();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        // 유저 추가
        users.put(webSocket, new User(webSocket));
        User loggedUser = users.get(webSocket);
        roomHandler.getRoom(0).addUser(loggedUser);

        // 전체 채팅 중인 유저한테만 보내자
        String packet = Packet.loginMessage(loggedUser.getID(), users.size());
        roomHandler.getRoom(loggedUser.getRoom()).sendToAll(packet);

        // 도움말 보냄
        webSocket.send(Packet.help());

        System.out.println(loggedUser.getID() + " 접속 (" + webSocket.getRemoteSocketAddress().getHostName() + ")");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        User exitedUser = users.get(webSocket);
        roomHandler.exitUser(exitedUser);
        users.remove(webSocket);

        System.out.println(exitedUser.getID() + " 접속 해제 (" + webSocket.getRemoteSocketAddress().getHostName() + ")");
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        try {
            User speakUser = users.get(webSocket);
            s = checkMessage(s, speakUser);
            String[] cmd = s.split(" ");
            cmd[0] = cmd.length == 0 ? s : cmd[0];

            if (speakUser.isIsolate()) {
                speakUser.getSocket().send(Packet.myChat(speakUser.getID(), s));
                return;
            }

            switch (cmd[0]) {
                case "@관리":
                    if (cmd[1].equals("패스워드")) {
                        speakUser.setAdmin(true);
                        String packet = Packet.notify("관리자 모드 가동!");
                        speakUser.getSocket().send(packet);
                    }
                    break;
                case "@공지":
                    String msg = s.replace("@공지 ", "");
                    String notice = Packet.notice(msg);
                    for (User user : users.values())
                        user.getSocket().send(notice);
                    break;
                case "@격리":
                    if (!speakUser.isAdmin())
                        return;

                    for (User user : users.values()) {
                        if (user.getID().equals(cmd[1])) {
                            user.setIsolate(true);
                            break;
                        }
                    }
                    break;
                case "@격리해제":
                    if (!speakUser.isAdmin())
                        return;

                    for (User user : users.values()) {
                        if (user.getID().equals(cmd[1])) {
                            user.setIsolate(false);
                            break;
                        }
                    }
                    break;
                case "@둘이놀자":
                    if (speakUser.getRoom() == 0)
                        roomHandler.joinUser(speakUser);
                    break;
                case "@다같이놀자":
                    if (speakUser.getRoom() != 0)
                        roomHandler.exitUser(speakUser);
                    break;
                case "@ㄴㄱ":
                case "@누구":
                    String userList = "";
                    for (User user : users.values())
                        userList += user.getID() + (user.getRoom() == 0 ? "" : "(1:1)") + ", ";

                    String packet = Packet.myChat("누구누구 있을까요! - (" + users.size() + "명)", userList);
                    webSocket.send(packet);
                    break;
                case "@ㄷㅇ":
                case "@ㄷㅇㅁ":
                case "@도움말":
                    webSocket.send(Packet.help());
                    break;
                default:
                    String myChat = Packet.myChat(speakUser.getID(), s);
                    String otherChat = Packet.otherChat(speakUser.getID(), s);
                    Room room = roomHandler.getRoom(speakUser.getRoom());

                    speakUser.getSocket().send(myChat);
                    room.sendToOthers(otherChat, speakUser);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();
    }

    // 메시지 체크
    private String checkMessage(String _msg, User _user) {
        _msg = _msg.replace("|", "?");
        _msg = _msg.replace("<", "(lshift)");
        _msg = _msg.replace(">", "(rshift)");

        // 금지 단어 뱉으면 격리하자
        for (String word : banWords) {
            if (_msg.contains(word))
                _user.setIsolate(true);
        }

        return _msg;
    }

    private void makeBanWords() {
        try {
            String s;
            BufferedReader words = new BufferedReader(new FileReader("./Data/banWords.txt"));
            while ((s = words.readLine()) != null)
                banWords.add(s);
            words.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Thread closeThread = new Thread() {
        public void run() {
            try {
                chatServer.stop();
                System.out.println("서버를 종료합니다");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
