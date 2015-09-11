package com.cinnober.msgcodec.test.upgrade;

import com.cinnober.msgcodec.MsgObject;
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
public class UpgradeWithInheritedGroups2 {
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
        testMessages.put("RemovedGrandChild",
                new PairedTestProtocols.PairedMessages(new Car(60,"Volvo"),
                        new VehicleV2(60)));
        return testMessages;
    }

    public static Collection<Class<?>> getOriginalSchemaClasses() {
        return Arrays.asList(Vehicle.class, Car.class);

    }

    public static Collection<Class<?>> getUpgradedSchemaClasses() {
        return Arrays.asList(VehicleV2.class);
    }



    @Id(8001)
    @Name("VehicleB")
    public static class Vehicle extends MsgObject {
        public int maxSpeed;

        public Vehicle(int maxSpeed) { this.maxSpeed = maxSpeed; }

        public Vehicle() {}
    }


    @Id(8002)
    @Name("CarB")
    public static class Car extends Vehicle {
        public String brand;

        public Car(int maxSpeed, String brand) { this.maxSpeed = maxSpeed; this.brand = brand; }

        public Car() {}
    }


    @Id(8001)
    @Name("VehicleB")
    public static class VehicleV2 extends MsgObject {
        public int maxSpeed;

        public VehicleV2(int maxSpeed) { this.maxSpeed = maxSpeed; }

        public VehicleV2() {}
    }

}
