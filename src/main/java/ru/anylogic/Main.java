package ru.anylogic;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.anylogic.model.Ticket;
import ru.anylogic.model.TicketUndoChange;
import ru.anylogic.model.Tickets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final String ARRIVE = "Владивосток";
    private static final String DESTINATION = "Тель-Авив";
    private static final String FILENAME = "src/main/resources/tickets.json";

    public static void main(String[] args) throws ParseException, IOException {
        File input = new File(FILENAME);
        FileInputStream fis = new FileInputStream(input);
        Scanner scan = new Scanner(fis);
        StringBuilder s = new StringBuilder();
        while (scan.hasNextLine()) {
            s.append(scan.nextLine().trim());
        }
        scan.close();
        s.deleteCharAt(s.indexOf(""));

        Tickets ticketsUndoChange = new ObjectMapper().readValue(s.toString(), Tickets.class);
        ArrayList<Ticket> tickets = new ArrayList<>();

        for (TicketUndoChange ticket : ticketsUndoChange.getTickets()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
            Calendar depDate = Calendar.getInstance();
            Calendar depTime = Calendar.getInstance();
            Calendar arrivalDate = Calendar.getInstance();
            Calendar arrivalTime = Calendar.getInstance();
            depDate.setTime(dateFormat.parse(ticket.getDeparture_date()));
            depTime.setTime(hourFormat.parse(ticket.getDeparture_time()));
            depDate.set(Calendar.HOUR, depTime.get(Calendar.HOUR_OF_DAY));
            depDate.set(Calendar.MINUTE, depTime.get(Calendar.MINUTE));
            arrivalDate.setTime(dateFormat.parse(ticket.getArrival_date()));
            arrivalTime.setTime(hourFormat.parse(ticket.getArrival_time()));
            arrivalDate.set(Calendar.HOUR, arrivalTime.get(Calendar.HOUR_OF_DAY));
            arrivalDate.set(Calendar.MINUTE, arrivalTime.get(Calendar.MINUTE));
            tickets.add(new Ticket(
                            ticket.getOrigin(),
                            ticket.getOrigin_name(),
                            ticket.getDestination(),
                            ticket.getDestination_name(),
                            depDate,
                            arrivalDate,
                            ticket.getCarrier(),
                            ticket.getStops(),
                            ticket.getPrice()
                    )
            );
        }

        List<Ticket> collect = getTickets(tickets);
        long sum = 0;
        int count = 0;
        ArrayList<Long> flyTimes = new ArrayList<>();
        for (Ticket ticket : collect) {
            long flyTime = calculateFlyTime(ticket);
            sum += flyTime;
            flyTimes.add(flyTime);
            count++;
        }
        long resAvgMillisecond = sum / count;
        long percentileMillisecond = percentile(flyTimes, 90);
        double avgFlyHour = resAvgMillisecond / 1000. / 60 / 60;
        double percentileFlyHour = percentileMillisecond / 1000. / 60 / 60;
        System.out.println("Среднее время полета от города " + ARRIVE + " до города " + DESTINATION + " занимает: "
                + (int) avgFlyHour + " часов, "
                + calculateMinutes((int) Math.round((avgFlyHour - (int) avgFlyHour) * 100)) + " минут.");
        System.out.println("90-й процентиль времени полета между городами " + ARRIVE + " и " + DESTINATION
                + " составляет: " + (int) percentileFlyHour + " часов, "
                + calculateMinutes((int) Math.round((percentileFlyHour - (int) percentileFlyHour) * 100)) + " минут.");

    }

    private static int calculateMinutes(int round) {
        double percent = round * (60. / 100);
        return (int) (percent);
    }

    private static List<Ticket> getTickets(ArrayList<Ticket> tickets) {
        return tickets
                .stream()
                .filter(ticket ->
                        ticket.getOriginName().contains(ARRIVE)
                                &&
                                ticket.getDestinationName().contains(DESTINATION))
                .collect(Collectors.toList());
    }

    private static long calculateFlyTime(Ticket ticket) {
        Date depDate = ticket.getDepartureDate().getTime();
        Date arrDate = ticket.getArrivalDate().getTime();
        return arrDate.getTime() - depDate.getTime();
    }

    public static long percentile(List<Long> latencies, double percentile) {
        Collections.sort(latencies);
        int index = (int) Math.ceil(percentile / 100.0 * latencies.size());
        return latencies.get(index - 1);
    }
}
