package ru.anylogic.model;

import java.util.ArrayList;

public class Tickets {
    private ArrayList<TicketUndoChange> tickets;

    public Tickets() {
    }

    public Tickets(ArrayList<TicketUndoChange> tickets) {
        this.tickets = tickets;
    }

    public ArrayList<TicketUndoChange> getTickets() {
        return tickets;
    }

    public void setTickets(ArrayList<TicketUndoChange> tickets) {
        this.tickets = tickets;
    }

    @Override
    public String toString() {
        return "Tickets{" +
                "tickets=" + tickets +
                '}';
    }
}
