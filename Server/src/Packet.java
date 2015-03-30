public class Packet {
    public static String notify(String msg) {
        return "0|" + msg;
    }

    public static String loginMessage(String id, int num) {
        String msg =  "0|" + id + (id.contains("님") ? "이 입장했습니다." : "님이 입장했습니다.");
        if (num > 0)
            msg += " (" + num + "명)";

        return msg;
    }

    public static String myChat(String name, String msg) {
        return "1|" + name + "|" + msg;
    }

    public static String otherChat(String name, String msg) {
        return "2|" + name + "|" + msg;
    }

    public static String notice(String msg) {
        return "3|" + msg;
    }

    public static String help() {
        return "0|<b>채팅창 비우기</b> : @청소, @ㅊㅅ<br/><b>알림 켜고 끄기</b> : @알림<br/><b>접속자 보기</b> : @누구, @ㄴㄱ<br/>" +
                "<b>도움말 보기</b> : @도움말, @ㄷㅇㅁ, @ㄷㅇ<br/><br/>" +
                "Developed by ProjectM, HeXA (이윤재 & 정인중)";
    }
}
