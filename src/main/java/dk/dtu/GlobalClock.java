package dk.dtu;

import org.jspace.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;


/**
 * The reason d'terre of the Global Clock class is to provide temporal information relating to what time it is now
 * and when the host program started.
 *
 * @apiNote Due to the way the simulation is structured it is not possible to change simulatedStartDateTime or irlStartDateTime mid-simulation.
 */

//TODO: Create a way to convert UNIX time to a date and vise versa
public class GlobalClock //uWu what is this
{

    private static String globalClockSpaceName = "globalClockSpace";
    private static int speedFactor = 0;
    private static LocalDateTime simulatedStartDateTime;
    private static LocalDateTime irlStartDateTime;


    public static void main(String[] args)
    {
        System.out.println("Glock");
    }

    /**
     *
     * @param hostRepo The repo of the host
     * @param simulatedStartDateTime The in game start date and time
     * @param speedFactor How much we speed up/down the in game time that has passed relative to the IRL time that has passed
     *                    I.g if speedFactor = 2, then a duration of 12 hours IRL is 1 day in game.
     */
    public static void initialize(SpaceRepository hostRepo, LocalDateTime simulatedStartDateTime, int speedFactor)
    {
        Space globalClockSpace = new RandomSpace();
        LocalDateTime irlStartDateTime = LocalDateTime.now();

        try {
            globalClockSpace.put("IRLStartDateTime",irlStartDateTime);
            globalClockSpace.put("simulatedStartDateTime",simulatedStartDateTime);
            globalClockSpace.put("speedFactor",speedFactor);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        hostRepo.add(globalClockSpaceName, globalClockSpace);
    }


    public static LocalDateTime getIRLDateTimeNow()
    {
        return LocalDateTime.now();
    }

    /**
     *
     *      The method is used for calculating how much time has passed in real life, which we in turn use to calculate the in game time that has passed.
     *
     * @param globalClockSpace The space wherein we store our settings related to the global clock
     * @return The real life start dateTime of the computer that acts as the host.
     */
    public static LocalDateTime getIRLStartDateTime(Space globalClockSpace)
    {
        try {
            Object[] startSpaceQuery = globalClockSpace.query(new ActualField("IRLStartDateTime"), new FormalField(LocalDateTime.class));
            return (LocalDateTime) startSpaceQuery[1];
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static LocalDateTime getIRLStartDateTime()
    {
        try {
            RemoteSpace globalClockSpace = getGlobalClockSpace();
            Object[] startSpaceQuery = globalClockSpace.query(new ActualField("IRLStartDateTime"), new FormalField(LocalDateTime.class));
            System.out.println(startSpaceQuery[1]);
            return (LocalDateTime) startSpaceQuery[1];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static RemoteSpace getGlobalClockSpace()
    {
        try {
            return new RemoteSpace(ClientUtil.setConnectType(ClientUtil.getHostUri(globalClockSpaceName),"keep"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalDateTime getSimulatedStartDateTime(Space globalClockSpace)
    {
        try {
            Object[] startSpaceQuery = globalClockSpace.query(new ActualField("simulatedStartDateTime"), new FormalField(LocalDateTime.class));
            return (LocalDateTime) startSpaceQuery[1];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
    We've overloaded this and the other get start date, in case we need them somewhere where we havne't already initialized the globalClockSpace.
    Otherwise, it would just be an unnecessary overload of the host to initialize the globalClockSpace multiple times within this object.
     */
    public static LocalDateTime getSimulatedStartDateTime()
    {
        try {
            RemoteSpace globalClockSpace = getGlobalClockSpace();
            Object[] startSpaceQuery = globalClockSpace.query(new ActualField("simulatedStartDateTime"), new FormalField(LocalDateTime.class));
            return (LocalDateTime) startSpaceQuery[1];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getSpeedFactor(Space globalClockSpace)
    {
        try {
            Object[] startSpaceQuery = globalClockSpace.query(new ActualField("speedFactor"), new FormalField(Integer.class));
            return (int) startSpaceQuery[1];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getSpeedFactor()
    {
        try {
            RemoteSpace globalClockSpace = getGlobalClockSpace();
            Object[] startSpaceQuery = globalClockSpace.query(new ActualField("speedFactor"), new FormalField(Integer.class));
            return (int) startSpaceQuery[1];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalDateTime getSimulatedDateTimeNow()
    {

        //We get the IRL date first as to ensure, we get as close to the date time this method was called as possible
        LocalDateTime irlDateTimeNow = getIRLDateTimeNow();

        //We do not want to overload the host, so we will only get these values once and then store them locally for future use.
        if(speedFactor == 0)
        {
            speedFactor = getSpeedFactor();
        }

        if(irlStartDateTime == null)
        {
            irlStartDateTime = getIRLStartDateTime();
        }

        if(simulatedStartDateTime == null)
        {
            simulatedStartDateTime = getSimulatedStartDateTime();
        }


        //We get the IRL difference in seconds
        Duration deltaT = Duration.between(irlStartDateTime,irlDateTimeNow);
        long simulatedDeltaTSeconds = deltaT.getSeconds()*speedFactor;
        LocalDateTime simulatedDateTimeNow = simulatedStartDateTime.plusSeconds(simulatedDeltaTSeconds);

        return simulatedDateTimeNow;
    }



}

//TODO: Investigate or implement a date object, so we can compare dates

