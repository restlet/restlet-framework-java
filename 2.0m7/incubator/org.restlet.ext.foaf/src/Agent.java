import org.restlet.data.Reference;

/**
 * An agent (eg. person, group, software or physical artifact).
 * 
 * @author Jerome Louvel
 */
public class Agent {

    public final static String GENDER_MALE = "male";

    public final static String GENDER_FEMALE = "female";

    private Reference mbox;

    private String mboxSha1Sum;

    private String gender;

    public String getGender() {
        return gender;
    }

    public Reference getMbox() {
        return mbox;
    }

    public String getMboxSha1Sum() {
        return mboxSha1Sum;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setMbox(Reference mbox) {
        this.mbox = mbox;
    }

    public void setMboxSha1Sum(String mboxSha1Sum) {
        this.mboxSha1Sum = mboxSha1Sum;
    }
}
