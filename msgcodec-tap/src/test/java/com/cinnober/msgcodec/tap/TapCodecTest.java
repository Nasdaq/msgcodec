/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 The MsgCodec Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
    public static class AmPositionExt {
    }
    @Id(3)
    public static class PositionInfo {
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
    public static class PositionKey {
    }
    @Id(5)
    public static class RtcCustomDetails {
    }
    @Id(6)
    public static class BiMessageData {
        @Id(1)
        private Object message;

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
        @Id(1)
        private PositionType positionType;
        @Id(2)
        private String settlementDate;
        @Id(3)
        private PositionInfo2 longPositionInfo;
        @Id(4)
        private PositionInfo2 shortPositionInfo;
        @Id(5)
        @Required
        @SmallDecimal
        private BigDecimal unsettledPaymentIn = BigDecimal.ZERO;
        @Id(6)
        @Required
        @SmallDecimal
        private BigDecimal unsettledPaymentOut = BigDecimal.ZERO;
        @Id(7)
        @Required
        @SmallDecimal
        private BigDecimal unsettledDeliveryIn = BigDecimal.ZERO;
        @Id(8)
        @Required
        @SmallDecimal
        private BigDecimal unsettledDeliveryOut = BigDecimal.ZERO;
        @Id(9)
        @Required
        @SmallDecimal
        private BigDecimal unsettledPaymentInAffectsCollateralBalance = BigDecimal.ZERO;
        @Id(10)
        @Required
        @SmallDecimal
        private BigDecimal unsettledPaymentOutAffectsCollateralBalance = BigDecimal.ZERO;
        @Id(11)
        private PositionKey positionKey;
        @Id(12)
        private String globalPositionKeyIdentifier;
        @Id(13)
        private RtcCustomDetails customAttributes;
        @Id(14)
        private AmPositionExt amPositionExt;
        public PositionType getPositionType() {
            return positionType;
        }
        public void setPositionType(PositionType positionType) {
            this.positionType = positionType;
        }
        public String getSettlementDate() {
            return settlementDate;
        }
        public void setSettlementDate(String settlementDate) {
            this.settlementDate = settlementDate;
        }
        public PositionInfo2 getLongPositionInfo() {
            return longPositionInfo;
        }
        public void setLongPositionInfo(PositionInfo2 longPositionInfo) {
            this.longPositionInfo = longPositionInfo;
        }
        public PositionInfo2 getShortPositionInfo() {
            return shortPositionInfo;
        }
        public void setShortPositionInfo(PositionInfo2 shortPositionInfo) {
            this.shortPositionInfo = shortPositionInfo;
        }
        public BigDecimal getUnsettledPaymentIn() {
            return unsettledPaymentIn;
        }
        public void setUnsettledPaymentIn(BigDecimal unsettledPaymentIn) {
            this.unsettledPaymentIn = unsettledPaymentIn;
        }
        public BigDecimal getUnsettledPaymentOut() {
            return unsettledPaymentOut;
        }
        public void setUnsettledPaymentOut(BigDecimal unsettledPaymentOut) {
            this.unsettledPaymentOut = unsettledPaymentOut;
        }
        public BigDecimal getUnsettledDeliveryIn() {
            return unsettledDeliveryIn;
        }
        public void setUnsettledDeliveryIn(BigDecimal unsettledDeliveryIn) {
            this.unsettledDeliveryIn = unsettledDeliveryIn;
        }
        public BigDecimal getUnsettledDeliveryOut() {
            return unsettledDeliveryOut;
        }
        public void setUnsettledDeliveryOut(BigDecimal unsettledDeliveryOut) {
            this.unsettledDeliveryOut = unsettledDeliveryOut;
        }
        public BigDecimal getUnsettledPaymentInAffectsCollateralBalance() {
            return unsettledPaymentInAffectsCollateralBalance;
        }
        public void setUnsettledPaymentInAffectsCollateralBalance(
                BigDecimal unsettledPaymentInAffectsCollateralBalance) {
            this.unsettledPaymentInAffectsCollateralBalance = unsettledPaymentInAffectsCollateralBalance;
        }
        public BigDecimal getUnsettledPaymentOutAffectsCollateralBalance() {
            return unsettledPaymentOutAffectsCollateralBalance;
        }
        public void setUnsettledPaymentOutAffectsCollateralBalance(
                BigDecimal unsettledPaymentOutAffectsCollateralBalance) {
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

    @Id(3)
    public static class PositionInfo2 {
        @Id(1)
        @SmallDecimal
        private BigDecimal quantity = BigDecimal.ZERO;
        @Id(2)
        @SmallDecimal
        private BigDecimal tradeIndependentquantity = BigDecimal.ZERO;
        @Id(3)
        @SmallDecimal
        private BigDecimal initialValue = BigDecimal.ZERO;
        @Id(4)
        @SmallDecimal
        private BigDecimal marketValue = BigDecimal.ZERO;
        @Id(5)
        @SmallDecimal
        private BigDecimal reservedQuantity = BigDecimal.ZERO;
        @Id(6)
        private RtcCustomDetails customAttributes;

        public BigDecimal getQuantity() {
            return quantity;
        }
        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }
        public BigDecimal getTradeIndependentquantity() {
            return tradeIndependentquantity;
        }
        public void setTradeIndependentquantity(BigDecimal tradeIndependentquantity) {
            this.tradeIndependentquantity = tradeIndependentquantity;
        }
        public BigDecimal getInitialValue() {
            return initialValue;
        }
        public void setInitialValue(BigDecimal initialValue) {
            this.initialValue = initialValue;
        }
        public BigDecimal getMarketValue() {
            return marketValue;
        }
        public void setMarketValue(BigDecimal marketValue) {
            this.marketValue = marketValue;
        }
        public BigDecimal getReservedQuantity() {
            return reservedQuantity;
        }
        public void setReservedQuantity(BigDecimal reservedQuantity) {
            this.reservedQuantity = reservedQuantity;
        }
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
