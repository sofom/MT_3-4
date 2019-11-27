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
import main.java.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;

public class NavigatorAgent extends Agent {
    public static final String START = "start";
    public static final String WAMPUS = "wampus";
    public static final String PIT = "pit";
    public static final String BREEZE = "breeze";
    public static final String STENCH = "stench";
    public static final String SCREAM = "scream";
    public static final String GOLD = "gold";
    public static final String BUMP = "bump";

    public static int Cell_STATUS_TRUE = 1;
    public static int Cell_STATUS_FALSE = 2;
    public static int Cell_STATUS_POSSIBLE = 3;
    public static int Cell_STATUS_NO_GOLD_WAY = 4;
    public static int Cell_STATUS_NO_STATUS = -1;


    private static final String SERVICE_DESCRIPTION = "NAVIGATOR_AGENT";
    String nickname = "main.java.agents.NavigatorAgent";
    AID id = new AID(nickname, AID.ISLOCALNAME);
    private Hashtable<AID, Position> agents_coords;
    private Hashtable<AID, LinkedList<int[]>> agentsWayStory;

    private boolean moveCell = false;
    private int agentX;
    private int agentY;

    WumpusWorld world;

    @Override
    protected void setup() {
        world = new WumpusWorld();
        agentsWayStory = new Hashtable<>();
        agents_coords = new Hashtable<>();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SpeleologistAgent.NAVIGATOR_AGENT_TYPE);
        sd.setName(SERVICE_DESCRIPTION);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new LocationRequestsServer());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Navigator-agent " + getAID().getName() + " terminating.");
    }

    private class LocationRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                AID request_agent = msg.getSender();
                if (agentsWayStory.get(request_agent) == null) {
                    LinkedList<int[]> agentWay = new LinkedList<>();
                    agentsWayStory.put(request_agent, agentWay);
                }
                Position request_agent_position = agents_coords.get(request_agent);
                if (request_agent_position == null) {
                    request_agent_position = new Position();
                    agents_coords.put(request_agent, request_agent_position);
                }
                String location = msg.getContent();
                location = location.substring(1, location.length() - 1);
                String[] Cell_info = location.split(", ");
                System.out.println("utils.Cell INFO: " + Arrays.toString(Cell_info));
                System.out.println("AGENT INFO: " + request_agent_position.getX() + " " + request_agent_position.getY());
                String[] actions = get_actions(request_agent, request_agent_position, Cell_info);
                ACLMessage reply = msg.createReply();

                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(Arrays.toString(actions));
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private String[] get_actions(AID request_agent, Position request_agent_position, String[] Cell_info) {
        System.out.println("Agent pos before: " + request_agent_position.getX() + " | " + request_agent_position.getY());
        int[] actions;
        Cell checking_Cell = world.getWorldGrid().get(request_agent_position);
        if (checking_Cell == null) {
            checking_Cell = new Cell();
            world.getWorldGrid().put(request_agent_position, checking_Cell);
        }

        if (!Arrays.asList(Cell_info).contains(BUMP)) {
            LinkedList<int[]> agentStory = agentsWayStory.get(request_agent);
            agentStory.add(new int[]{request_agent_position.getX(), request_agent_position.getY()});
            request_agent_position.setX(agentX);
            request_agent_position.setY(agentY);
            if (world.getWorldGrid().get(request_agent_position).getExist() != NavigatorAgent.Cell_STATUS_TRUE) {
                world.getWorldGrid().get(request_agent_position).setExist(NavigatorAgent.Cell_STATUS_TRUE);
                System.out.println("MARKED THE EXISTENCE");
            }
            moveCell = false;
        } else {
            Position helpPosition = new Position(agentX, agentY);
            world.getWorldGrid().get(helpPosition).setExist(NavigatorAgent.Cell_STATUS_FALSE);
        }
        checking_Cell = world.getWorldGrid().get(request_agent_position);
        if (checking_Cell == null) {
            checking_Cell = new Cell();
            world.getWorldGrid().put(request_agent_position, checking_Cell);
        }

        if (checking_Cell.getOk() != NavigatorAgent.Cell_STATUS_TRUE) {
            checking_Cell.setOk(NavigatorAgent.Cell_STATUS_TRUE);
        }
        for (String event : Cell_info) {
            checking_Cell.addEvent(event);
        }
        updateNeighbors(request_agent_position);
        if (world.isWampusAlive() && world.getWampusCellCount() > 2) {
            Position wampusPosition = world.getWampusCoords();
            actions = getNextCellAction(request_agent_position, wampusPosition, SpeleologistAgent.SHOOT_ARROW);
        } else {
            Position[] nextOkCells = getOkNeighbors(request_agent, request_agent_position);
            // TODO: Нужно еще отсечь тех, у кого нет пути к золоту
            int best_candidate = -1;
            int candidate_status = -1;
            for (int i = 0; i < nextOkCells.length; ++i) {
                Position candidate_Cell = nextOkCells[i];
                System.out.println("CANDIDATE CHECKING: " + candidate_Cell.getX() + " " + candidate_Cell.getY());
                System.out.println("AGENT CHECKING: " + request_agent_position.getX() + " " + request_agent_position.getY());
                if (candidate_Cell.getX() > request_agent_position.getX()) {
                    best_candidate = i;
                    System.out.println("1");
                    break;
                } else if (candidate_Cell.getY() > request_agent_position.getY()) {
                    if (candidate_status < 3) {
                        System.out.println("2");
                        candidate_status = 3;
                    } else continue;
                } else if (candidate_Cell.getX() < request_agent_position.getX()) { // влево
                    if (candidate_status < 2) {
                        System.out.println("3");
                        candidate_status = 2;
                    } else continue;
                } else { // вниз
                    if (candidate_status < 1) {
                        System.out.println("4");
                        candidate_status = 1;
                    } else continue;
                }
                best_candidate = i;
            }
            System.out.println("OK CellS COUNT IS: " + nextOkCells.length);
            System.out.println("ADVICE POSITION IS: " + nextOkCells[best_candidate].getX() + " | " + nextOkCells[best_candidate].getY());
            actions = getNextCellAction(request_agent_position, nextOkCells[best_candidate], SpeleologistAgent.MOVE);
            System.out.println("ADVICE ACTIONS IS: " + Arrays.toString(actions));
        }

        String[] language_actions = new String[actions.length];
        for (int i = 0; i < actions.length; ++i) {
            language_actions[i] = SpeleologistAgent.actionCodes.get(actions[i]);
        }
        return language_actions;
    }

    private int[] getNextCellAction(Position request_agent_position, Position nextOkCell, int action) {
        agentX = request_agent_position.getX();
        agentY = request_agent_position.getY();
        int look;
        if (request_agent_position.getY() < nextOkCell.getY()) {
            agentY += 1;
            look = SpeleologistAgent.LOOK_UP;
        } else if (request_agent_position.getY() > nextOkCell.getY()) {
            agentY -= 1;
            look = SpeleologistAgent.LOOK_DOWN;
        } else if (request_agent_position.getX() < nextOkCell.getX()) {
            agentX += 1;
            look = SpeleologistAgent.LOOK_RIGHT;
        } else {
            agentX -= 1;
            look = SpeleologistAgent.LOOK_LEFT;
        }
        moveCell = true;

        return new int[]{look, action};
    }

    private Position[] getOkNeighbors(AID request_agent, Position request_agent_position) {
        Position[] okNeighbors = getNeighborsPosition(request_agent_position);
        ArrayList<Position> okPositions = new ArrayList<>();
        for (Position position : okNeighbors) {
            this.world.getWorldGrid().putIfAbsent(position, new Cell()); // если комнаты
            // не существует - добавляем новую комнату на карте
            if ((this.world.getWorldGrid().get(position).getOk() == NavigatorAgent.Cell_STATUS_TRUE
                    && this.world.getWorldGrid().get(position).getNoWay() != NavigatorAgent.Cell_STATUS_TRUE
                    && this.world.getWorldGrid().get(position).getExist() != NavigatorAgent.Cell_STATUS_FALSE
            ) ||
                    this.world.getWorldGrid().get(position).getOk() == NavigatorAgent.Cell_STATUS_NO_STATUS) {
                okPositions.add(position);
            }
        }
        if (okPositions.size() == 0) {
            int x = agentsWayStory.get(request_agent).getLast()[0];
            int y = agentsWayStory.get(request_agent).getLast()[1];
            okPositions.add(new Position(x, y));
            this.world.getWorldGrid().get(request_agent_position).setNoWay(Cell_STATUS_TRUE);
        }
        return okPositions.toArray(new Position[0]);
    }

    private Cell[] getNeighborsImaginaryCell(Position request_agent_position) {
        Position rightNeighbor = new Position(request_agent_position.getX() + 1, request_agent_position.getY());
        Position upNeighbor = new Position(request_agent_position.getX(), request_agent_position.getY() + 1);
        Position leftNeighbor = new Position(request_agent_position.getX() - 1, request_agent_position.getY());
        Position bottomNeighbor = new Position(request_agent_position.getX(), request_agent_position.getY() - 1);
        Cell rightCell = world.getWorldGrid().get(rightNeighbor);
        if (rightCell == null) {
            rightCell = new Cell();
            world.getWorldGrid().put(rightNeighbor, rightCell);
        }
        Cell upCell = world.getWorldGrid().get(upNeighbor);
        if (upCell == null) {
            upCell = new Cell();
            world.getWorldGrid().put(rightNeighbor, upCell);
        }
        Cell leftCell = world.getWorldGrid().get(leftNeighbor);
        if (leftCell == null) {
            leftCell = new Cell();
            world.getWorldGrid().put(rightNeighbor, leftCell);
        }
        Cell bottomCell = world.getWorldGrid().get(bottomNeighbor);
        if (bottomCell == null) {
            bottomCell = new Cell();
            world.getWorldGrid().put(rightNeighbor, bottomCell);
        }
        Cell[] Cells = new Cell[]{rightCell, upCell, leftCell, bottomCell};
        return Cells;
    }

    private Position[] getNeighborsPosition(Position request_agent_position) {
        Position rightNeighbor = new Position(request_agent_position.getX() + 1, request_agent_position.getY());
        Position upNeighbor = new Position(request_agent_position.getX(), request_agent_position.getY() + 1);
        Position leftNeighbor = new Position(request_agent_position.getX() - 1, request_agent_position.getY());
        Position bottomNeighbor = new Position(request_agent_position.getX(), request_agent_position.getY() - 1);
        ;
        return new Position[]{rightNeighbor, upNeighbor, leftNeighbor, bottomNeighbor};
    }

    private void updateNeighbors(Position request_agent_position) {
        Cell currentCell = world.getWorldGrid().get(request_agent_position);
        Cell[] CellList = getNeighborsImaginaryCell(request_agent_position);

        if (currentCell.getStench() == NavigatorAgent.Cell_STATUS_TRUE) {
            world.setWampusCellCount(world.getWampusCellCount() + 1);
            for (Cell Cell : CellList) {
                if (Cell.getWampus() == NavigatorAgent.Cell_STATUS_NO_STATUS) {
                    Cell.setOk(NavigatorAgent.Cell_STATUS_POSSIBLE);
                    Cell.setWampus(NavigatorAgent.Cell_STATUS_POSSIBLE);
                }
            }
        }
        if (currentCell.getBreeze() == NavigatorAgent.Cell_STATUS_TRUE) {
            for (Cell Cell : CellList) {
                if (Cell.getPit() == NavigatorAgent.Cell_STATUS_NO_STATUS) {
                    Cell.setOk(NavigatorAgent.Cell_STATUS_POSSIBLE);
                    Cell.setPit(NavigatorAgent.Cell_STATUS_POSSIBLE);
                }
            }
        }
        if (currentCell.getBreeze() == NavigatorAgent.Cell_STATUS_FALSE && currentCell.getStench() == NavigatorAgent.Cell_STATUS_FALSE) {
            for (Cell Cell : CellList) {
                Cell.setOk(NavigatorAgent.Cell_STATUS_TRUE);
                Cell.setWampus(NavigatorAgent.Cell_STATUS_FALSE);
                Cell.setPit(NavigatorAgent.Cell_STATUS_FALSE);
            }
        }
    }

}

