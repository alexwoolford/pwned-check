package io.woolford;


public class LookupRecord {

    private String sha1;
    private String password;
    private Boolean hashExists;

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getHashExists() {
        return hashExists;
    }

    public void setHashExists(Boolean hashExists) {
        this.hashExists = hashExists;
    }

    @Override
    public String toString() {
        return "LookupRecord{" +
                "sha1='" + sha1 + '\'' +
                ", password='" + password + '\'' +
                ", hashExists=" + hashExists +
                '}';
    }

}
