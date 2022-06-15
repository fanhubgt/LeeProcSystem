/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leesystem.program;

import leesystem.program.EnactBody;
import leesystem.program.EnactHead;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author appiah
 */
public class EnactRule {

    private EnactBody body;
    private EnactHead head;
    private List<EnactBody> enactBodys=new ArrayList<EnactBody>();
    private List<EnactHead> enactHeads=new ArrayList<EnactHead>();
    
    public EnactRule() {
    }

    public EnactRule(EnactBody body, EnactHead head) {
        this.body = body;
        this.head = head;
    }

    public EnactBody getBody() {
        return body;
    }

    public void setBody(EnactBody body) {
        this.body = body;
    }

    public EnactHead getHead() {
        return head;
    }

    public void setHead(EnactHead head) {
        this.head = head;
    }
    
    public void addRule(EnactHead eh, EnactBody eb){
        enactBodys.add(eb);
        enactHeads.add(eh);
    }
}
