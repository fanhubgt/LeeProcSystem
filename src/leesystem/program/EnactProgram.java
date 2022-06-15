/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leesystem.program;

/**
 *
 * @author appiah
 */
public class EnactProgram {

    private EnactFact enactFacts = new EnactFact();

    public EnactProgram() {
    }

    public EnactFact getEnactFacts() {
        return enactFacts;
    }

    public void setEnactFacts(EnactFact enactFacts) {
        this.enactFacts = enactFacts;
    }

    public void addEnactFact(EnactPremise ep) {
        enactFacts.getEnactList().add(ep);
    }

    public boolean removeEnactFact(EnactPremise ep) {
        return enactFacts.getEnactList().remove(ep);
    }

    @Override
    public String toString() {
        return super.toString();
    }
    
}
