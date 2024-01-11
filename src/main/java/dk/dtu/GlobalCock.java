package dk.dtu;

import org.jspace.*;

import java.io.IOException;


/**
 * The reason d'terre of the Global Clock class is to provide temporal information relating to what time it is now
 * and when the host program started.
 *
 * @apiNote All time units are denoted in UNIX time and in milliseconds!
 */
public class GlobalCock //uWu what is this GlobalCOCK?!
{

    private static String timeStartSpaceName = "timeStartUnix";



    public static void initialize(SpaceRepository hostRepo, long startTimeUnix)
    {

        Space timeStartSpace = new RandomSpace();

        try {
            timeStartSpace.put(startTimeUnix);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        hostRepo.add(timeStartSpaceName, timeStartSpace);
    }


    public static long getTimeNow()
    {
        return (long) System.currentTimeMillis();
    }

    //Returns the time that has passed since the host ran its application in format UNIX in millis units
    public static long getTimePassed()
    {

        try {
            long timeNow = getTimeNow();
            String uri = ClientUtil.getHostUri(timeStartSpaceName);

            Space timeStartSpace = new RemoteSpace(ClientUtil.setConnectType(uri,"keep"));

;

            //TODO: Maybe create a subclass whose sole purpose is to run a thread that does this exact thing? In case there is a delay in our query?
            Object[] timeStartQuery = timeStartSpace.query(new FormalField(Long.class));


            long timeStartUnix = (long) timeStartQuery[0];
            return timeNow - timeStartUnix;

            }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

