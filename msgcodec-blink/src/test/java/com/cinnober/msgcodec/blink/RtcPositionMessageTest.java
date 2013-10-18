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
package com.cinnober.msgcodec.blink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.anot.Id;

/**
 * @author mikael.brannstrom
 *
 */
public class RtcPositionMessageTest {

    @Test
    public void testEncodeDecode() throws IOException {
        ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(
                AmPosition.class,
                AmPositionExt.class,
                PositionInfo.class,
                PositionKey.class,
                RtcCustomDetails.class,
                BiMessageData.class
                );
        System.out.println("Dictionary:\n" + dictionary);
        BlinkCodec codec = new BlinkCodec(dictionary);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        AmPosition msg = new AmPosition();
        msg.setPositionType(1);
        msg.setSettlementDate("2013-07-19");
        msg.setLongPositionInfo(position(500));
        msg.setLongPositionInfo(position(100));
        msg.setGlobalPositionKeyIdentifier("foobar åäö");

        // encode it
        codec.encode(msg, out);
        System.out.println("Encoded message size: " + out.size() + " bytes");

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Object decodedMsg = codec.decode(in);
        System.out.println("Decoded message: " + decodedMsg);

        // Naive benchmark
        int iterations = 1;
        out = new ByteArrayOutputStream(out.size() * iterations);
        BlinkOutputStream blinkOut = new BlinkOutputStream(out);
        long nanos = System.nanoTime();
        for (int i=0; i<iterations; i++) {
            codec.encode(msg, blinkOut);
        }
        nanos = System.nanoTime() - nanos;
        System.out.format("Encode %,d messages took %,d ns = %.2f micros/msg\n",
                iterations, nanos, (1.0*nanos/iterations/1000));

        in = new ByteArrayInputStream(out.toByteArray());
        BlinkInputStream blinkIn = new BlinkInputStream(in);
        nanos = System.nanoTime();
        for (int i=0; i<iterations; i++) {
            codec.decode(blinkIn);
        }
        nanos = System.nanoTime() - nanos;
        System.out.format("Decode %,d messages took %,d ns = %.2f micros/msg\n",
                iterations, nanos, (1.0*nanos/iterations/1000));

    }

    private PositionInfo position(int qty) {
        PositionInfo position = new PositionInfo();
        position.setQuantity(qty * 1000000L);
        position.setTradeIndependentquantity(qty * 1000000L);
        position.setInitialValue(qty * 1000000L * 500);
        return position;
    }

    @Id(1)
    public static class AmPosition extends MsgObject {
        @Id(1)
        private Integer positionType;
        @Id(2)
        private String settlementDate;
        @Id(3)
        private PositionInfo longPositionInfo;
        @Id(4)
        private PositionInfo shortPositionInfo;
        @Id(5)
        private long unsettledPaymentIn;
        @Id(6)
        private long unsettledPaymentOut;
        @Id(7)
        private long unsettledDeliveryIn;
        @Id(8)
        private long unsettledDeliveryOut;
        @Id(9)
        private long unsettledPaymentInAffectsCollateralBalance;
        @Id(10)
        private long unsettledPaymentOutAffectsCollateralBalance;
        @Id(11)
        private PositionKey positionKey;
        @Id(12)
        private String globalPositionKeyIdentifier;
        @Id(13)
        private RtcCustomDetails customAttributes;
        @Id(14)
        private AmPositionExt amPositionExt;
        public Integer getPositionType() {
            return positionType;
        }
        public void setPositionType(Integer positionType) {
            this.positionType = positionType;
        }
        public String getSettlementDate() {
            return settlementDate;
        }
        public void setSettlementDate(String settlementDate) {
            this.settlementDate = settlementDate;
        }
        public PositionInfo getLongPositionInfo() {
            return longPositionInfo;
        }
        public void setLongPositionInfo(PositionInfo longPositionInfo) {
            this.longPositionInfo = longPositionInfo;
        }
        public PositionInfo getShortPositionInfo() {
            return shortPositionInfo;
        }
        public void setShortPositionInfo(PositionInfo shortPositionInfo) {
            this.shortPositionInfo = shortPositionInfo;
        }
        public long getUnsettledPaymentIn() {
            return unsettledPaymentIn;
        }
        public void setUnsettledPaymentIn(long unsettledPaymentIn) {
            this.unsettledPaymentIn = unsettledPaymentIn;
        }
        public long getUnsettledPaymentOut() {
            return unsettledPaymentOut;
        }
        public void setUnsettledPaymentOut(long unsettledPaymentOut) {
            this.unsettledPaymentOut = unsettledPaymentOut;
        }
        public long getUnsettledDeliveryIn() {
            return unsettledDeliveryIn;
        }
        public void setUnsettledDeliveryIn(long unsettledDeliveryIn) {
            this.unsettledDeliveryIn = unsettledDeliveryIn;
        }
        public long getUnsettledDeliveryOut() {
            return unsettledDeliveryOut;
        }
        public void setUnsettledDeliveryOut(long unsettledDeliveryOut) {
            this.unsettledDeliveryOut = unsettledDeliveryOut;
        }
        public long getUnsettledPaymentInAffectsCollateralBalance() {
            return unsettledPaymentInAffectsCollateralBalance;
        }
        public void setUnsettledPaymentInAffectsCollateralBalance(
                long unsettledPaymentInAffectsCollateralBalance) {
            this.unsettledPaymentInAffectsCollateralBalance = unsettledPaymentInAffectsCollateralBalance;
        }
        public long getUnsettledPaymentOutAffectsCollateralBalance() {
            return unsettledPaymentOutAffectsCollateralBalance;
        }
        public void setUnsettledPaymentOutAffectsCollateralBalance(
                long unsettledPaymentOutAffectsCollateralBalance) {
            this.unsettledPaymentOutAffectsCollateralBalance = unsettledPaymentOutAffectsCollateralBalance;
        }
        public PositionKey getPositionKey() {
            return positionKey;
        }
        public void setPositionKey(PositionKey positionKey) {
            this.positionKey = positionKey;
        }
        public String getGlobalPositionKeyIdentifier() {
            return globalPositionKeyIdentifier;
        }
        public void setGlobalPositionKeyIdentifier(String globalPositionKeyIdentifier) {
            this.globalPositionKeyIdentifier = globalPositionKeyIdentifier;
        }
        public RtcCustomDetails getCustomAttributes() {
            return customAttributes;
        }
        public void setCustomAttributes(RtcCustomDetails customAttributes) {
            this.customAttributes = customAttributes;
        }
        public AmPositionExt getAmPositionExt() {
            return amPositionExt;
        }
        public void setAmPositionExt(AmPositionExt amPositionExt) {
            this.amPositionExt = amPositionExt;
        }



    }


    @Id(2)
    public static class AmPositionExt extends MsgObject {
    }
    @Id(3)
    public static class PositionInfo extends MsgObject {
        @Id(1)
        private Long quantity = 0L;
        @Id(2)
        private Long tradeIndependentquantity = 0L;
        @Id(3)
        private Long initialValue = 0L;
        @Id(4)
        private Long marketValue = 0L;
        @Id(5)
        private Long reservedQuantity = 0L;
        @Id(6)
        private RtcCustomDetails customAttributes;
        public PositionInfo() {
        }

        public Long getQuantity() {
            return quantity;
        }
        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }
        public Long getTradeIndependentquantity() {
            return tradeIndependentquantity;
        }
        public void setTradeIndependentquantity(Long tradeIndependentquantity) {
            this.tradeIndependentquantity = tradeIndependentquantity;
        }
        public Long getInitialValue() {
            return initialValue;
        }
        public void setInitialValue(Long initialValue) {
            this.initialValue = initialValue;
        }
        public Long getMarketValue() {
            return marketValue;
        }
        public void setMarketValue(Long marketValue) {
            this.marketValue = marketValue;
        }
        public Long getReservedQuantity() {
            return reservedQuantity;
        }
        public void setReservedQuantity(Long reservedQuantity) {
            this.reservedQuantity = reservedQuantity;
        }
        public RtcCustomDetails getCustomAttributes() {
            return customAttributes;
        }
        public void setCustomAttributes(RtcCustomDetails customAttributes) {
            this.customAttributes = customAttributes;
        }


    }
    @Id(4)
    public static class PositionKey extends MsgObject {
    }
    @Id(5)
    public static class RtcCustomDetails extends MsgObject {
    }
    @Id(6)
    public static class BiMessageData extends MsgObject {
        @Id(1)
        private Object message;

        public Object getMessage() {
            return message;
        }

        public void setMessage(Object message) {
            this.message = message;
        }

    }

}
