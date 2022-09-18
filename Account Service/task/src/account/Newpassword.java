package account;

public class Newpassword {
    public Newpassword(String new_password) {
        this.new_password = new_password;
    }

    public Newpassword() {
    }

    public String getNew_password() {
        return new_password;
    }

    public void setNew_password(String new_password) {
        this.new_password = new_password;
    }

    private String new_password;
}
