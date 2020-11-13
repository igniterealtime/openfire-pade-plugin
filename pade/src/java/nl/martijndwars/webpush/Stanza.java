package nl.martijndwars.webpush;

public class Stanza {
    public String msgType;
    public String msgFrom;
    public String msgBody;
    public String msgNick;
    public String token;
    public String avatar;

    public Stanza() { }

    public Stanza(String msgType, String msgFrom, String msgBody, String msgNick, String token, String avatar) {
        this.msgType = msgType;
        this.msgFrom = msgFrom;
        this.msgBody = msgBody;
        this.msgNick = msgNick;
        this.token = token;
        this.avatar = avatar;
    }
}
