/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leesystem.program.code;

import leesystem.program.EnactBody;
import leesystem.program.EnactHead;
import leesystem.program.EnactRule;

/**
 *
 * @author appiah
 */
public class EnactTranslator {

    private EnactRule enactRule;

    public EnactTranslator(EnactRule enactRule) {
        this.enactRule = enactRule;
    }

    public EnactTranslator() {
    }

    public EnactRule getEnactRule() {
        return enactRule;
    }

    public void setEnactRule(EnactRule enactRule) {
        this.enactRule = enactRule;
    }

    public String translate(String comment, String author, String version, String runName, String action, String location, String time, String interest, String interest1,String rank, String name) {
        EnactHead head = new EnactHead(action, location, time);
        EnactBody body = new EnactBody(interest, interest1, rank);
        enactRule = new EnactRule(body, head);
        return "//"+comment+"\n@author " + author + "\n" + "@version " + version + "\n@run " + runName + "\n{ \n" +
                head.toString() + "" + body.toString() + "\n name=\""+name+"\";\n}";
    }
    
}
