package nl.martijndwars.webpush;

public class Subscription {
    public String endpoint;

    public Keys keys;

    public Subscription() {
        // No-args constructor
    }

    public Subscription(String endpoint, Keys keys) {
        this.endpoint = endpoint;
        this.keys = keys;
    }

    public class Keys {
        public String p256dh;

        public String auth;

        public Keys() {
            // No-args constructor
        }

        public Keys(String p256dh, String auth) {
            this.p256dh = p256dh;
            this.auth = auth;
        }
    }
}
