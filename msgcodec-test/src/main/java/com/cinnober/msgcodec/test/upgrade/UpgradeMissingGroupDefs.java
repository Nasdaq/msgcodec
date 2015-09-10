package com.cinnober.msgcodec.test.upgrade;

import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Codec upgrade test messages for basic type widening.
 *
 * @author Morgan Johansson, Cinnober Financial Technology North AB
 */
public class UpgradeMissingGroupDefs {
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
        testMessages.put("GroupDefAdded", new PairedTestProtocols.PairedMessages(new BaseGroupV1(89), new BaseGroupV2(89)));
        testMessages.put("DynamicGroupDefAdded", new PairedTestProtocols.PairedMessages(new Base2GroupV1(892), new Base2GroupV2(892)));
        testMessages.put("GroupDefAddedInArray", new PairedTestProtocols.PairedMessages(new Base3GroupV1(893), new Base3GroupV2(893)));
        testMessages.put("GroupDefRemoved", new PairedTestProtocols.PairedMessages(new Base4GroupV1(891), new Base4GroupV2(891)));
        return testMessages;
    }

    public static Collection<Class<?>> getOriginalSchemaClasses() {
        return Arrays.asList(BaseGroupV1.class, Base2GroupV1.class, Base3GroupV1.class, Base4GroupV1.class,
                AnotherSubGroup.class);

    }

    public static Collection<Class<?>> getUpgradedSchemaClasses() {
        return Arrays.asList(BaseGroupV2.class, SubGroup.class, Base2GroupV2.class, SubGroup2.class,
                SubGroup3.class, SubGroup4.class, Base4GroupV2.class, Base3GroupV2.class);
    }

    public abstract static class IdComparisonMixin {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof IdComparisonMixin && ((IdComparisonMixin)obj).getId() == getId();
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(getId());
        }
        
        public abstract int getId();
    }
    
    @Name("GroupDefTestBase")
    @Id(6001)
    public static class BaseGroupV1  extends IdComparisonMixin {
        public int id;

        public BaseGroupV1(int id) {
            this.id = id;
        }

        public BaseGroupV1() {
        }

        @Override
        public int getId() { return id; }
    }

    @Name("GroupDefTestBase")
    @Id(6001)
    public static class BaseGroupV2 extends IdComparisonMixin {
        public int id;
        public SubGroup sub;

        public BaseGroupV2(int id) {
            this.id = id;
            this.sub = new SubGroup();
            this.sub.text = "TEXT"+id;
        }

        public BaseGroupV2()  {
        }

        @Override
        public int getId() { return id; }
    }

    @Name("GroupDefTestBase2")
    @Id(6002)
    public static class Base2GroupV1 extends IdComparisonMixin {
        public int id;

        public Base2GroupV1(int id) {
            this.id = id;
        }

        public Base2GroupV1() {
        }


        @Override
        public int getId() { return id; }
    }

    @Name("GroupDefTestBase2")
    @Id(6002)
    public static class Base2GroupV2 extends IdComparisonMixin {
        public int id;
        @Dynamic
        public SubGroup2 sub;

        public Base2GroupV2(int id) {
            this.id = id;
            sub = new SubGroup3();
            sub.text = "FUNTEXT";
        }

        public Base2GroupV2() {
        }


        @Override
        public int getId() { return id; }
    }

    @Name("GroupDefTestBase3")
    @Id(6003)
    public static class Base3GroupV1 extends IdComparisonMixin {
        public int id;

        public Base3GroupV1(int id) {
            this.id = id;
        }

        public Base3GroupV1() {
        }

        @Override
        public int getId() { return id; }
    }

    @Name("GroupDefTestBase3")
    @Id(6003)
    public static class Base3GroupV2 extends IdComparisonMixin {
        public int id;
        public SubGroup[] manySubs;

        public Base3GroupV2(int id) {
            this.id = id;
            manySubs = new SubGroup[id%10];
            for (int i=0;i<manySubs.length;i++) {
                manySubs[i] = new SubGroup();
                manySubs[i].text = "TEXT "+(i+id);
            }
        }

        public Base3GroupV2() {
        }


        @Override
        public int getId() { return id; }
    }

    @Name("GroupDefTestBase4")
    @Id(6010)
    public static class Base4GroupV1 extends IdComparisonMixin {
        public int id;
        public AnotherSubGroup[] manySubs;

        public Base4GroupV1(int id) {
            this.id = id;
            manySubs = new AnotherSubGroup[id%10];
            for (int i=0;i<manySubs.length;i++) {
                manySubs[i] = new AnotherSubGroup();
                manySubs[i].text = "TEXT "+(i+id);
            }
        }

        public Base4GroupV1() {
        }


        @Override
        public int getId() { return id; }
    }

    @Name("GroupDefTestBase4")
    @Id(6010)
    public static class Base4GroupV2 extends IdComparisonMixin {
        public int id;

        public Base4GroupV2(int id) {
            this.id = id;
        }

        public Base4GroupV2() {
        }


        @Override
        public int getId() { return id; }
    }


    @Name("GroupDefTestSUB")
    @Id(6004)
    public static class SubGroup  {
        public String text;
    }

    @Name("GroupDefTestASUB")
    @Id(6007)
    public static class AnotherSubGroup  {
        public String text;
    }

    @Name("GroupDefTestSUB2")
    @Id(6009)
    public static class SubGroup2  {
        public String text;
    }

    @Name("GroupDefTestSUB3")
    @Id(6005)
    public static class SubGroup3 extends SubGroup2 {
        public boolean flag;
    }

    @Name("GroupDefTestSUB4")
    @Id(6006)
    public static class SubGroup4 extends SubGroup2 {
        public float value;
    }

}
