package be.uantwerpen.fti.nodeone.domain;

import be.uantwerpen.fti.nodeone.component.ReplicationComponent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

// https://www.iro.umontreal.ca/~vaucher/Agents/Jade/primer2.html
@Getter
@Setter
public class SyncAgent extends Agent implements Runnable, Serializable {
    private AID syncAgentID = new AID("SyncAgent", AID.ISLOCALNAME);
    private int cool= 0;
    private Set<FileStructure> filesList;
    private ReplicationComponent replicationComponent;
    public SyncAgent(){
    }

    @Override
    protected void setup(){
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                System.out.println("hello " + cool);
                cool++;
            }
        });
    }





}
