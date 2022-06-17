package be.uantwerpen.fti.nodeone.domain;

import jade.core.AID;
import jade.core.Agent;
import lombok.Getter;
import lombok.Setter;


// https://jade.tilab.com/doc/tutorials/JADEProgramming-Tutorial-for-beginners.pdf
@Getter
@Setter
public class FailureAgent extends Agent {
    private AID failureAgentID = new AID("FailureAgent", AID.ISLOCALNAME);


    protected void setup(){

    }
}

