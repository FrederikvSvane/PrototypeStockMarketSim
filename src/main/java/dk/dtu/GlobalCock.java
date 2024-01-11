package dk.dtu;

import org.jspace.*;

import java.io.IOException;


/**
 * The reason d'terre of the Global Clock class is to provide temporal information relating to what time it is now
 * and when the host program started.
 *
 * @apiNote All time units are denoted in UNIX time and in milliseconds!
 */
public class GlobalCock
{

    private static String timeStartSpaceName = "timeStartUnix";



    public static void initialize(SpaceRepository hostRepo, long startTimeUnix)
    {
        startTimeUnix = startTimeUnix;

        Space timeStartSpace = new RandomSpace();

        try {
            timeStartSpace.put(startTimeUnix);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        hostRepo.add(timeStartSpaceName, new SequentialSpace());
    }


    public static long getTimeNow()
    {
        return (long) System.currentTimeMillis() / 1000L;
    }

    //Returns the time that has passed since the host ran its application in format UNIX in millis units
    public static long getTimePassed()
    {

        try {
            long timeNow = getTimeNow();

            //TODO: Maybe create a subclass whose sole purpose is to run a thread that does this exact thing? In case there is a delay in our query?
            Object[] timeStartQuery = (new RemoteSpace(ClientUtil.getHostUri(timeStartSpaceName))).query(new FormalField(long.class));
            long timeStartUnix = (long) timeStartQuery[0];
            return timeNow - timeStartUnix;

            }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

