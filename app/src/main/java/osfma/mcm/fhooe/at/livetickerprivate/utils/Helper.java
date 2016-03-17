package osfma.mcm.fhooe.at.livetickerprivate.utils;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import osfma.mcm.fhooe.at.livetickerprivate.R;
import osfma.mcm.fhooe.at.livetickerprivate.model.Game;
import osfma.mcm.fhooe.at.livetickerprivate.model.User;

/**
 * Created by Tob0t on 24.02.2016.
 */
public class Helper {
    private static final String LOG_TAG = Helper.class.getSimpleName();

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yyyy", Locale.GERMAN);
    public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm", Locale.GERMAN);
    public static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.GERMAN);


    /**
    * Encode user email to use it as a Firebase key (Firebase does not allow "." in the key name)
    * Encoded email is also used as "userEmail", list and item "owner" value
    */

    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }
    public static String decodeEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }

    public static boolean checkIfOwner(Game game, String currentUserEmail) {
        return (game.getOwner() != null && game.getOwner().equals(currentUserEmail));
    }

    public static String checkGameType(Constants.GameType _gameType) {
        String gameType = Constants.FIREBASE_URL_PUBLIC_GAMES;
        if(_gameType == Constants.GameType.PRIVATE){
            gameType = Constants.FIREBASE_URL_PRIVATE_GAMES;
        }
        return gameType;
    }
    // Using reflection for the filter parameters
    public static Map<Method,Boolean> addFilter(Map<Method, Boolean> hashMap, Class className, String methodName, boolean valueParam) {

        Method methodParam = null;
        try {
            methodParam = className.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        hashMap.put(methodParam,valueParam);
        return hashMap;
    }

    // iterate over all Filter Methods, if they are all true than inflate an icon
    public static boolean checkAllCondtionsTrue(Object object, Map<Method, Boolean> mFilter) {
        boolean allConditionsTrue = true;

        for(Map.Entry<Method, Boolean> entry: mFilter.entrySet()) {
            Method methodParam = entry.getKey();
            boolean valueParam = entry.getValue();
            try {
                if((boolean) methodParam.invoke(object) != valueParam){
                    allConditionsTrue = false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return allConditionsTrue;
    }

    /*public static HashMap<String, Object> updateGameSet
     (final String listId,
     final String owner, HashMap<String, Object> mapToUpdate,
     String propertyToUpdate, Object valueToUpdate) {

     mapToUpdate.put("/" + "/" + owner + "/"
     + listId + "/" + propertyToUpdate, valueToUpdate);

     return mapToUpdate;
     }*/
}
