import java.net.InetAddress;
import java.util.Objects;

public class Client {
    protected String username;
    protected InetAddress ip;
    protected int port;

    public Client(String username, InetAddress ip, int port) {
        this.username = username;
        this.ip = ip;
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getLunghezzaUser() {
        String s=getUsername();
        return s.length();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return username.equals(client.username) &&
                ip.equals(client.ip) &&
                port==(client.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, ip, port);
    }

    @Override
    public String toString() {
        return "Client{" + "username=" + username + ", ip=" + ip + ", port=" + port + "}\n";
    }
}
