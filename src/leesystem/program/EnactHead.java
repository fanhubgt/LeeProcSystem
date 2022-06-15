/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leesystem.program;

/**
 *
 * @author appiah
 */
public class EnactHead {

    private String action;
    private String location;
    private String time;

    public EnactHead() {
    }

    public EnactHead(String action, String location, String time) {
        this.action = action;
        this.location = location;
        this.time = time;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    //discount, ban, reputation, bigspender, clientale, 
    @Override
    public String toString() {
      return "\n enact::action a=\""+action+"\";\n enact::location loc=\""+location+"\";\n enact::time tt=\""+time+"\";\n";
    }
    
}
