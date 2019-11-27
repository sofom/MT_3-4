package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class WumpusAgent extends Agent {

    public static String SERVICE_DESCRIPTION = "WAMPUS-WORLD";
    private static int START = -1;
    private static int WAMPUS = 1;
    private static int PIT = 2;
    private static int BREEZE = 3;
    private static int STENCH = 4;
    private static int SCREAM = 5;
    private static int GOLD = 6;
    private static int BUMP = 7;
    public static HashMap<Integer, String> CellCodes = new HashMap<Integer, String>() {{
        put(START, NavigatorAgent.START);
        put(WAMPUS, NavigatorAgent.WAMPUS);
        put(PIT, NavigatorAgent.PIT);
        put(BREEZE, NavigatorAgent.BREEZE);
        put(STENCH, NavigatorAgent.STENCH);
        put(SCREAM, NavigatorAgent.SCREAM);
        put(GOLD, NavigatorAgent.GOLD);
        put(BUMP, NavigatorAgent.BUMP);
    }};
    private static int NUM_OF_ROWS = 4;
    private static int NUM_OF_COLUMNS = 4;

    private Cell[][] map = {
            {new Cell(), new Cell(BREEZE), new Cell(PIT), new Cell(BREEZE)},
            {new Cell(STENCH), new Cell(), new Cell(), new Cell(BREEZE)},
            {new Cell(WAMPUS, STENCH), new Cell(BREEZE, STENCH, GOLD), new Cell(PIT), new Cell(BREEZE)},
            {new Cell(STENCH), new Cell(), new Cell(BREEZE), new Cell(PIT)}
    };
    private HashMap<AID, Point> Speleologists;

    String nickname = "WampusWorld";
    AID id = new AID(nickname, AID.ISLOCALNAME);

    @Override
    protected void setup() {
        System.out.println("Hello! WampusWorld-agent " + getAID().getName() + " is ready.");
        Speleologists = new HashMap<>();
        showMap();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SpeleologistAgent.WAMPUS_WORLD_TYPE);
        sd.setName(SERVICE_DESCRIPTION);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new SpeleologistConnectPerformer());
        addBehaviour(new SpeleologistArrowPerformer());
        addBehaviour(new SpeleologistGoldPerformer());
        addBehaviour(new SpeleologistMovePerformer());
    }

    private void showMap() {
        for (int i = 0; i < this.map.length; i++) {
            for (int j = 0; j < this.map[i].length; j++) {
                System.out.println("POSITION: " + i + ", " + j + "; MARKERS: " + map[i][j].events);
            }

        }
    }

    private class Cell {
        ArrayList<String> events = new ArrayList<>();

        Cell(int... args) {
            for (int i : args) {
                events.add(WumpusAgent.CellCodes.get(i));
            }
        }
    }

    private class Point {
        int row = 0;
        int column = 0;

        Point(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }

    private class SpeleologistConnectPerformer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String message = msg.getContent();
                if (Objects.equals(message, SpeleologistAgent.GO_INSIDE)) {
                    AID current_Speleologist = msg.getSender();
                    Point speleologist_point = Speleologists.get(current_Speleologist);
                    if (speleologist_point == null) {
                        Speleologists.put(current_Speleologist, new Point(0, 0));
                    } else {
                        Speleologists.put(current_Speleologist, new Point(0, 0));
                    }
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.CONFIRM);
                    reply.setContent(map[0][0].events.toString());
                    myAgent.send(reply);
                }
//
            } else {
                block();
            }
        }
    }

    private class SpeleologistArrowPerformer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(SpeleologistAgent.SHOOT_ARROW);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(SpeleologistAgent.SHOOT_ARROW);

                String message = msg.getContent();
                AID current_Speleologist = msg.getSender();
                Point speleologist_point = Speleologists.get(current_Speleologist);

                int row = speleologist_point.row;
                int column = speleologist_point.column;
                String answer = "";
                if (message.equals(SpeleologistAgent.actionCodes.get(SpeleologistAgent.LOOK_DOWN))) {
                    for (int i = 0; i < row; ++i) {
                        if (map[i][column].events.contains(WumpusAgent.CellCodes.get(WAMPUS))) {
                            answer = NavigatorAgent.SCREAM;
                        }
                    }
                } else if (message.equals(SpeleologistAgent.actionCodes.get(SpeleologistAgent.LOOK_UP))) {
                    for (int i = row + 1; i < NUM_OF_ROWS; ++i) {
                        if (map[i][column].events.contains(WumpusAgent.CellCodes.get(WAMPUS))) {
                            answer = NavigatorAgent.SCREAM;
                        }
                    }
                } else if (message.equals(SpeleologistAgent.actionCodes.get(SpeleologistAgent.LOOK_LEFT))) {
                    for (int i = 0; i < column; ++i) {
                        if (map[row][i].events.contains(WumpusAgent.CellCodes.get(WAMPUS))) {
                            answer = NavigatorAgent.SCREAM;
                        }
                    }
                } else if (message.equals(SpeleologistAgent.actionCodes.get(SpeleologistAgent.LOOK_RIGHT))) {
                    for (int i = column + 1; i < NUM_OF_COLUMNS; ++i) {
                        if (map[row][i].events.contains(WumpusAgent.CellCodes.get(WAMPUS))) {
                            answer = NavigatorAgent.SCREAM;
                        }
                    }
                }

                reply.setContent(answer);

                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class SpeleologistMovePerformer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(SpeleologistAgent.MOVE);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(SpeleologistAgent.MOVE);

                String message = msg.getContent();
                AID current_Speleologist = msg.getSender();
                Point speleologist_point = Speleologists.get(current_Speleologist);
                System.out.println("World say: Current agent coords: " + speleologist_point.row + " | " + speleologist_point.column);
                if (speleologist_point == null) {
                    Speleologists.put(current_Speleologist, new Point(0, 0));
                    speleologist_point = Speleologists.get(current_Speleologist);
                }
                int row = speleologist_point.row;
                int column = speleologist_point.column;
                if (message.equals(SpeleologistAgent.actionCodes.get(SpeleologistAgent.LOOK_DOWN))) {
                    row -= 1;
                } else if (message.equals(SpeleologistAgent.actionCodes.get(SpeleologistAgent.LOOK_UP))) {
                    row += 1;
                } else if (message.equals(SpeleologistAgent.actionCodes.get(SpeleologistAgent.LOOK_LEFT))) {
                    column -= 1;
                } else if (message.equals(SpeleologistAgent.actionCodes.get(SpeleologistAgent.LOOK_RIGHT))) {
                    column += 1;
                }
                if (row > -1 && column > -1 && row < NUM_OF_ROWS && column < NUM_OF_COLUMNS) {
                    speleologist_point.column = column;
                    speleologist_point.row = row;
                    reply.setContent(map[row][column].events.toString());
                } else {
                    reply.setContent(String.valueOf(new ArrayList<String>() {{
                        add(NavigatorAgent.BUMP);
                    }}));
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class SpeleologistGoldPerformer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(SpeleologistAgent.TAKE_GOLD);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String message = msg.getContent();
                AID current_Speleologist = msg.getSender();
                Point speleologist_point = Speleologists.get(current_Speleologist);
                if (speleologist_point == null) {
                    Speleologists.put(current_Speleologist, new Point(0, 0));
                } else {
                    if (map[speleologist_point.row][speleologist_point.column].events.contains(WumpusAgent.CellCodes.get(GOLD))) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(SpeleologistAgent.TAKE_GOLD);
                        reply.setContent("GOLD");
                        myAgent.send(reply);
                    }
                }
            } else {
                block();
            }
        }
    }
}

