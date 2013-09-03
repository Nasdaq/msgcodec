/*
 * Copyright (c) 2013 Cinnober Financial Technology AB, Stockholm,
 * Sweden. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Cinnober Financial Technology AB, Stockholm, Sweden. You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Cinnober.
 *
 * Cinnober makes no representations or warranties about the suitability
 * of the software, either expressed or implied, including, but not limited
 * to, the implied warranties of merchantibility, fitness for a particular
 * purpose, or non-infringement. Cinnober shall not be liable for any
 * damages suffered by licensee as a result of using, modifying, or
 * distributing this software or its derivatives.
 */
package com.cinnober.msgcodec.tap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.SmallDecimal;
import com.cinnober.msgcodec.anot.Static;

/**
 * @author mikael.brannstrom
 *
 */
public class TapCodecTest {

    @Test
    public void testEncodeDecode() throws IOException {
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(
                AmPosition2.class,
                AmPositionExt.class,
                PositionInfo2.class,
                PositionKey.class,
                RtcCustomDetails.class,
                BiMessageData.class
                );
        System.out.println("Dictionary:\n" + dictionary);
        TapCodec codec = new TapCodec(dictionary);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        AmPosition2 msg = new AmPosition2();
        msg.setPositionType(PositionType.TRADE_POSITION);
        msg.setSettlementDate("2013-07-19");
        msg.setLongPositionInfo(position2(500));
        msg.setLongPositionInfo(position2(100));
        msg.setGlobalPositionKeyIdentifier("foobar åäö");

        // encode it
        codec.encode(msg, out);
        System.out.println("Encoded message size: " + out.size() + " bytes");

        String expected =
                "43 46 54 01 09 02 00 00 00 00 00 c0 48 00 0c 0b 41 6d 50 6f 73 69 74 69 6f 6e 01 01 0b 32 30 31 "+
                "33 2d 30 37 2d 31 39 17 01 af d7 c2 00 01 af d7 c2 00 01 81 ba a1 ed e8 00 01 00 01 00 00 00 00 "+
                "00 00 00 00 00 00 0b 66 6f 6f 62 61 72 20 e5 e4 f6 00 00";
        String hex = TestUtil.toHex(out.toByteArray());
        expected = expected.replaceAll("[ \\n]", "");
        hex = hex.replaceAll("[ \\n]", "");
        System.out.println("hex: " + hex);
        Assert.assertEquals(expected, hex);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Object decodedMsg = codec.decode(in);
        System.out.println("Decoded message: " + decodedMsg);

        // Naive benchmark
        int iterations = 1;
        out = new ByteArrayOutputStream(out.size() * iterations);
        TapOutputStream blinkOut = new TapOutputStream(out);
        long nanos = System.nanoTime();
        for (int i=0; i<iterations; i++) {
            codec.encode(msg, blinkOut);
        }
        nanos = System.nanoTime() - nanos;
        System.out.format("Encode %,d messages took %,d ns = %.2f micros/msg\n", iterations, nanos, (1.0*nanos/iterations/1000));

        in = new ByteArrayInputStream(out.toByteArray());
        TapInputStream blinkIn = new TapInputStream(in);
        nanos = System.nanoTime();
        for (int i=0; i<iterations; i++) {
            codec.decode(blinkIn);
        }
        nanos = System.nanoTime() - nanos;
        System.out.format("Decode %,d messages took %,d ns = %.2f micros/msg\n", iterations, nanos, (1.0*nanos/iterations/1000));

    }

    private PositionInfo position(int qty) {
        PositionInfo position = new PositionInfo();
        position.setQuantity(qty * 1000000L);
        position.setTradeIndependentquantity(qty * 1000000L);
        position.setInitialValue(qty * 1000000L * 500);
        return position;
    }
    private PositionInfo2 position2(int qty) {
        PositionInfo2 position = new PositionInfo2();
        position.setQuantity(BigDecimal.valueOf(qty, 0));
        position.setTradeIndependentquantity(BigDecimal.valueOf(qty, 0));
        position.setInitialValue(BigDecimal.valueOf(qty * 500, 0));
        return position;
    }

    @Id(1)
    public static class AmPosition {
        private Integer positionType;
        private String settlementDate;
        private PositionInfo longPositionInfo;
        private PositionInfo shortPositionInfo;
        private long unsettledPaymentIn;
        private long unsettledPaymentOut;
        private long unsettledDeliveryIn;
        private long unsettledDeliveryOut;
        private long unsettledPaymentInAffectsCollateralBalance;
        private long unsettledPaymentOutAffectsCollateralBalance;
        private PositionKey positionKey;
        private String globalPositionKeyIdentifier;
        private RtcCustomDetails customAttributes;
        private AmPositionExt amPositionExt;
        @Id(1)
        public Integer getPositionType() {
            return positionType;
        }
        public void setPositionType(Integer positionType) {
            this.positionType = positionType;
        }
        @Id(2)
        public String getSettlementDate() {
            return settlementDate;
        }
        public void setSettlementDate(String settlementDate) {
            this.settlementDate = settlementDate;
        }
        @Id(3)
        @Static
        public PositionInfo getLongPositionInfo() {
            return longPositionInfo;
        }
        public void setLongPositionInfo(PositionInfo longPositionInfo) {
            this.longPositionInfo = longPositionInfo;
        }
        @Id(4)
        @Static
        public PositionInfo getShortPositionInfo() {
            return shortPositionInfo;
        }
        public void setShortPositionInfo(PositionInfo shortPositionInfo) {
            this.shortPositionInfo = shortPositionInfo;
        }
        @Id(5)
        public long getUnsettledPaymentIn() {
            return unsettledPaymentIn;
        }
        public void setUnsettledPaymentIn(long unsettledPaymentIn) {
            this.unsettledPaymentIn = unsettledPaymentIn;
        }
        @Id(6)
        public long getUnsettledPaymentOut() {
            return unsettledPaymentOut;
        }
        public void setUnsettledPaymentOut(long unsettledPaymentOut) {
            this.unsettledPaymentOut = unsettledPaymentOut;
        }
        @Id(7)
        public long getUnsettledDeliveryIn() {
            return unsettledDeliveryIn;
        }
        public void setUnsettledDeliveryIn(long unsettledDeliveryIn) {
            this.unsettledDeliveryIn = unsettledDeliveryIn;
        }
        @Id(8)
        public long getUnsettledDeliveryOut() {
            return unsettledDeliveryOut;
        }
        public void setUnsettledDeliveryOut(long unsettledDeliveryOut) {
            this.unsettledDeliveryOut = unsettledDeliveryOut;
        }
        @Id(9)
        public long getUnsettledPaymentInAffectsCollateralBalance() {
            return unsettledPaymentInAffectsCollateralBalance;
        }
        public void setUnsettledPaymentInAffectsCollateralBalance(
                long unsettledPaymentInAffectsCollateralBalance) {
            this.unsettledPaymentInAffectsCollateralBalance = unsettledPaymentInAffectsCollateralBalance;
        }
        @Id(10)
        public long getUnsettledPaymentOutAffectsCollateralBalance() {
            return unsettledPaymentOutAffectsCollateralBalance;
        }
        public void setUnsettledPaymentOutAffectsCollateralBalance(
                long unsettledPaymentOutAffectsCollateralBalance) {
            this.unsettledPaymentOutAffectsCollateralBalance = unsettledPaymentOutAffectsCollateralBalance;
        }
        @Id(11)
        @Static
        public PositionKey getPositionKey() {
            return positionKey;
        }
        public void setPositionKey(PositionKey positionKey) {
            this.positionKey = positionKey;
        }
        @Id(12)
        public String getGlobalPositionKeyIdentifier() {
            return globalPositionKeyIdentifier;
        }
        public void setGlobalPositionKeyIdentifier(String globalPositionKeyIdentifier) {
            this.globalPositionKeyIdentifier = globalPositionKeyIdentifier;
        }
        @Id(13)
        @Static
        public RtcCustomDetails getCustomAttributes() {
            return customAttributes;
        }
        public void setCustomAttributes(RtcCustomDetails customAttributes) {
            this.customAttributes = customAttributes;
        }
        @Id(14)
        @Static
        public AmPositionExt getAmPositionExt() {
            return amPositionExt;
        }
        public void setAmPositionExt(AmPositionExt amPositionExt) {
            this.amPositionExt = amPositionExt;
        }



    }


    @Id(2)
    public static class AmPositionExt {
    }
    @Id(3)
    public static class PositionInfo {
        private Long quantity = 0L;
        private Long tradeIndependentquantity = 0L;
        private Long initialValue = 0L;
        private Long marketValue = 0L;
        private Long reservedQuantity = 0L;
        private RtcCustomDetails customAttributes;
        public PositionInfo() {
        }

        @Id(1)
        public Long getQuantity() {
            return quantity;
        }
        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }
        @Id(2)
        public Long getTradeIndependentquantity() {
            return tradeIndependentquantity;
        }
        public void setTradeIndependentquantity(Long tradeIndependentquantity) {
            this.tradeIndependentquantity = tradeIndependentquantity;
        }
        @Id(3)
        public Long getInitialValue() {
            return initialValue;
        }
        public void setInitialValue(Long initialValue) {
            this.initialValue = initialValue;
        }
        @Id(4)
        public Long getMarketValue() {
            return marketValue;
        }
        public void setMarketValue(Long marketValue) {
            this.marketValue = marketValue;
        }
        @Id(5)
        public Long getReservedQuantity() {
            return reservedQuantity;
        }
        public void setReservedQuantity(Long reservedQuantity) {
            this.reservedQuantity = reservedQuantity;
        }
        @Id(6)
        @Static
        public RtcCustomDetails getCustomAttributes() {
            return customAttributes;
        }
        public void setCustomAttributes(RtcCustomDetails customAttributes) {
            this.customAttributes = customAttributes;
        }


    }
    @Id(4)
    public static class PositionKey {
    }
    @Id(5)
    public static class RtcCustomDetails {
    }
    @Id(6)
    public static class BiMessageData {
        private Object message;

        @Id(1)
        public Object getMessage() {
            return message;
        }

        public void setMessage(Object message) {
            this.message = message;
        }

    }

    @Id(1)
    @Name("AmPosition")
    public static class AmPosition2 {
        private PositionType positionType;
        private String settlementDate;
        private PositionInfo2 longPositionInfo;
        private PositionInfo2 shortPositionInfo;
        private BigDecimal unsettledPaymentIn = BigDecimal.ZERO;
        private BigDecimal unsettledPaymentOut = BigDecimal.ZERO;
        private BigDecimal unsettledDeliveryIn = BigDecimal.ZERO;
        private BigDecimal unsettledDeliveryOut = BigDecimal.ZERO;
        private BigDecimal unsettledPaymentInAffectsCollateralBalance = BigDecimal.ZERO;
        private BigDecimal unsettledPaymentOutAffectsCollateralBalance = BigDecimal.ZERO;
        private PositionKey positionKey;
        private String globalPositionKeyIdentifier;
        private RtcCustomDetails customAttributes;
        private AmPositionExt amPositionExt;
        @Id(1)
        public PositionType getPositionType() {
            return positionType;
        }
        public void setPositionType(PositionType positionType) {
            this.positionType = positionType;
        }
        @Id(2)
        public String getSettlementDate() {
            return settlementDate;
        }
        public void setSettlementDate(String settlementDate) {
            this.settlementDate = settlementDate;
        }
        @Id(3)
        @Static
        public PositionInfo2 getLongPositionInfo() {
            return longPositionInfo;
        }
        public void setLongPositionInfo(PositionInfo2 longPositionInfo) {
            this.longPositionInfo = longPositionInfo;
        }
        @Id(4)
        @Static
        public PositionInfo2 getShortPositionInfo() {
            return shortPositionInfo;
        }
        public void setShortPositionInfo(PositionInfo2 shortPositionInfo) {
            this.shortPositionInfo = shortPositionInfo;
        }
        @Id(5)
        @Required
        @SmallDecimal
        public BigDecimal getUnsettledPaymentIn() {
            return unsettledPaymentIn;
        }
        public void setUnsettledPaymentIn(BigDecimal unsettledPaymentIn) {
            this.unsettledPaymentIn = unsettledPaymentIn;
        }
        @Id(6)
        @Required
        @SmallDecimal
        public BigDecimal getUnsettledPaymentOut() {
            return unsettledPaymentOut;
        }
        public void setUnsettledPaymentOut(BigDecimal unsettledPaymentOut) {
            this.unsettledPaymentOut = unsettledPaymentOut;
        }
        @Id(7)
        @Required
        @SmallDecimal
        public BigDecimal getUnsettledDeliveryIn() {
            return unsettledDeliveryIn;
        }
        public void setUnsettledDeliveryIn(BigDecimal unsettledDeliveryIn) {
            this.unsettledDeliveryIn = unsettledDeliveryIn;
        }
        @Id(8)
        @Required
        @SmallDecimal
        public BigDecimal getUnsettledDeliveryOut() {
            return unsettledDeliveryOut;
        }
        public void setUnsettledDeliveryOut(BigDecimal unsettledDeliveryOut) {
            this.unsettledDeliveryOut = unsettledDeliveryOut;
        }
        @Id(9)
        @Required
        @SmallDecimal
        public BigDecimal getUnsettledPaymentInAffectsCollateralBalance() {
            return unsettledPaymentInAffectsCollateralBalance;
        }
        public void setUnsettledPaymentInAffectsCollateralBalance(
                BigDecimal unsettledPaymentInAffectsCollateralBalance) {
            this.unsettledPaymentInAffectsCollateralBalance = unsettledPaymentInAffectsCollateralBalance;
        }
        @Id(10)
        @Required
        @SmallDecimal
        public BigDecimal getUnsettledPaymentOutAffectsCollateralBalance() {
            return unsettledPaymentOutAffectsCollateralBalance;
        }
        public void setUnsettledPaymentOutAffectsCollateralBalance(
                BigDecimal unsettledPaymentOutAffectsCollateralBalance) {
            this.unsettledPaymentOutAffectsCollateralBalance = unsettledPaymentOutAffectsCollateralBalance;
        }
        @Id(11)
        @Static
        public PositionKey getPositionKey() {
            return positionKey;
        }
        public void setPositionKey(PositionKey positionKey) {
            this.positionKey = positionKey;
        }
        @Id(12)
        public String getGlobalPositionKeyIdentifier() {
            return globalPositionKeyIdentifier;
        }
        public void setGlobalPositionKeyIdentifier(String globalPositionKeyIdentifier) {
            this.globalPositionKeyIdentifier = globalPositionKeyIdentifier;
        }
        @Id(13)
        @Static
        public RtcCustomDetails getCustomAttributes() {
            return customAttributes;
        }
        public void setCustomAttributes(RtcCustomDetails customAttributes) {
            this.customAttributes = customAttributes;
        }
        @Id(14)
        @Static
        public AmPositionExt getAmPositionExt() {
            return amPositionExt;
        }
        public void setAmPositionExt(AmPositionExt amPositionExt) {
            this.amPositionExt = amPositionExt;
        }
    }

    @Id(3)
    public static class PositionInfo2 {
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal tradeIndependentquantity = BigDecimal.ZERO;
        private BigDecimal initialValue = BigDecimal.ZERO;
        private BigDecimal marketValue = BigDecimal.ZERO;
        private BigDecimal reservedQuantity = BigDecimal.ZERO;
        private RtcCustomDetails customAttributes;

        @Id(1)
        @SmallDecimal
        public BigDecimal getQuantity() {
            return quantity;
        }
        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }
        @Id(2)
        @SmallDecimal
        public BigDecimal getTradeIndependentquantity() {
            return tradeIndependentquantity;
        }
        public void setTradeIndependentquantity(BigDecimal tradeIndependentquantity) {
            this.tradeIndependentquantity = tradeIndependentquantity;
        }
        @Id(3)
        @SmallDecimal
        public BigDecimal getInitialValue() {
            return initialValue;
        }
        public void setInitialValue(BigDecimal initialValue) {
            this.initialValue = initialValue;
        }
        @Id(4)
        @SmallDecimal
        public BigDecimal getMarketValue() {
            return marketValue;
        }
        public void setMarketValue(BigDecimal marketValue) {
            this.marketValue = marketValue;
        }
        @Id(5)
        @SmallDecimal
        public BigDecimal getReservedQuantity() {
            return reservedQuantity;
        }
        public void setReservedQuantity(BigDecimal reservedQuantity) {
            this.reservedQuantity = reservedQuantity;
        }
        @Id(6)
        @Static
        public RtcCustomDetails getCustomAttributes() {
            return customAttributes;
        }
        public void setCustomAttributes(RtcCustomDetails customAttributes) {
            this.customAttributes = customAttributes;
        }
    }

    public static enum PositionType {
        @Id(1)
        TRADE_POSITION,
        SETTLEMENT_POSITION,
    }

}
