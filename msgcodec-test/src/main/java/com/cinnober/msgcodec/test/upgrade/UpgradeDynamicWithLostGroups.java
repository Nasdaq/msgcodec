package com.cinnober.msgcodec.test.upgrade;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Codec upgrade test messages for parent/child relations.
 *
 * @author Morgan Johansson, Cinnober Financial Technology North AB
 */
public class UpgradeDynamicWithLostGroups {
    /**
     * Returns message pairs suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "zero" or "border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message pair.
     */
    public static Map<String, PairedTestProtocols.PairedMessages> createMessages() {
        TreeMap<String, PairedTestProtocols.PairedMessages> testMessages = new TreeMap<>();
        //int maxSpeed, String brand, int loadCapacity, Float range, Integer weight, Integer length
        LunchBox box = new LunchBox();
        FishNChips m = new FishNChips();
        m.cals = 579;
        m.fishType = "Goldfish";
        box.meal = m;

        LunchBoxV2 box2 = new LunchBoxV2();
        box2.meal = new MealV2();
        box2.meal.cals = 579;


        testMessages.put("RemovedDynamicChild",
                new PairedTestProtocols.PairedMessages(box, box2));
        return testMessages;
    }

    public static Collection<Class<?>> getOriginalSchemaClasses() {
        return Arrays.asList(LunchBox.class,Meal.class,Hamburger.class,FishNChips.class);

    }

    public static Collection<Class<?>> getUpgradedSchemaClasses() {
        return Arrays.asList(LunchBoxV2.class, MealV2.class, HamburgerV2.class);
    }



    @Name("LunchBox")
    @Id(1501)
    public static class LunchBox extends MsgObject {
        @Dynamic
        Meal meal;

        @Dynamic
        FishNChips extraFood;
    }

    @Name("Meal")
    @Id(1502)
    public static class Meal extends MsgObject {
        int cals;
    }

    @Name("Hamburger")
    @Id(1503)
    public static class Hamburger extends Meal {
        String flavor;
    }

    @Name("FishNChips")
    @Id(1504)
    public static class FishNChips extends Meal {
        String fishType;
    }

    @Name("LunchBox")
    @Id(1501)
    public static class LunchBoxV2 extends MsgObject {
        @Dynamic
        MealV2 meal;
    }

    @Name("Meal")
    @Id(1502)
    public static class MealV2 extends MsgObject {
        int cals;
    }

    @Name("Hamburger")
    @Id(1503)
    public static class HamburgerV2 extends MealV2 {
        String flavor;
    }
}
