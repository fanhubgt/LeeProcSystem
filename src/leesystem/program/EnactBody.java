/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leesystem.program;

/**
 *
 * @author appiah
 */
public class EnactBody extends EnactHead {

    private String interest;
    private String interest2;

    public EnactBody() {
    }

    public EnactBody(String interest, String interest2) {
        this.interest = interest;
        this.interest2 = interest2;
    }

    public EnactBody(String interest, String interest2, String location) {
        this.interest = interest;
        this.interest2 = interest2;
        setLocation(location);
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getInterest2() {
        return interest2;
    }

    public void setInterest2(String interest2) {
        this.interest2 = interest2;
    }

    @Override
    public String toString() {
        return "\n enact::interest ia=\"" + interest + "\", \nenact::interest ia1=\"" + interest2 + "\";" +
                "\n enact::rank r=\"" + getLocation() + "\";\n";
    }
}
