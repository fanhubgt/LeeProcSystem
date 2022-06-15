/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leesystem.program;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author appiah
 */
public class EnactFact implements Serializable{

    private List<EnactPremise> enactList = new ArrayList<EnactPremise>();

    public EnactFact() {
    }

    public List<EnactPremise> getEnactList() {
        return enactList;
    }

    public void setEnactList(List<EnactPremise> enactList) {
        this.enactList = enactList;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
}
