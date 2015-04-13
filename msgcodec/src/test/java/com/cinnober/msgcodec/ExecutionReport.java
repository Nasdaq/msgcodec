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
package com.cinnober.msgcodec;

import java.math.BigDecimal;
import java.util.Arrays;

import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;

/**
 *  FIX 5.0 SP2 Execution Report message.
 *
 * @author mikael.brannstrom
 */
@Id('8')
public class ExecutionReport {
    @Id(37)
    @Required
    private String orderID;
    @Id(11)
    private String clOrdID;
    @Id(41)
    private String origClOrdID;
    @Id(17)
    @Required
    private String execID;
    @Id(150)
    private int execType;
    @Id(39)
    private int ordStatus;
    @Id(55)
    private String symbol;
    @Id(54)
    private int side;

    @Id(44)
    private BigDecimal price;
    @Id(31)
    private BigDecimal lastPx;
    @Id(151)
    @Required
    private BigDecimal leavesQty;
    @Id(14)
    @Required
    private BigDecimal cumQty;


    public String getOrderID() {
        return orderID;
    }
    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
    public String getClOrdID() {
        return clOrdID;
    }
    public void setClOrdID(String clOrdID) {
        this.clOrdID = clOrdID;
    }
    public String getOrigClOrdID() {
        return origClOrdID;
    }
    public void setOrigClOrdID(String origClOrdID) {
        this.origClOrdID = origClOrdID;
    }
    public String getExecID() {
        return execID;
    }
    public void setExecID(String execID) {
        this.execID = execID;
    }
    public int getExecType() {
        return execType;
    }
    public void setExecType(int execType) {
        this.execType = execType;
    }
    public int getOrdStatus() {
        return ordStatus;
    }
    public void setOrdStatus(int ordStatus) {
        this.ordStatus = ordStatus;
    }
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public int getSide() {
        return side;
    }
    public void setSide(int side) {
        this.side = side;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public BigDecimal getLastPx() {
        return lastPx;
    }
    public void setLastPx(BigDecimal lastPx) {
        this.lastPx = lastPx;
    }
    public BigDecimal getLeavesQty() {
        return leavesQty;
    }
    public void setLeavesQty(BigDecimal leavesQty) {
        this.leavesQty = leavesQty;
    }
    public BigDecimal getCumQty() {
        return cumQty;
    }
    public void setCumQty(BigDecimal cumQty) {
        this.cumQty = cumQty;
    }

    @Override
    public String toString() {
        return "ExecutionReport [orderID=" + orderID + ", clOrdID=" + clOrdID
                + ", origClOrdID=" + origClOrdID + ", execID=" + execID
                + ", execType='" + (char)execType + "', ordStatus='" + (char)ordStatus
                + "', symbol=" + symbol + ", side='" + side + "', price=" + price
                + ", lastPx=" + lastPx + ", leavesQty=" + leavesQty
                + ", cumQty=" + cumQty + "]";
    }

    public static GroupDef groupDef() {
        return new GroupDef("ExecutionReport", 56, null,
            Arrays.asList(
               new FieldDef("clOrdID", 11, false, TypeDef.STRING, null,
                   new FieldBinding(new Accessor<ExecutionReport, String>() {
                       @Override
                           public String getValue(ExecutionReport obj) {
                           return obj.getClOrdID();
                       }
                       @Override
                           public void setValue(ExecutionReport obj, String value) {
                           obj.setClOrdID(value);
                       }
                   }, String.class, null)),
               new FieldDef("cumQty", 14, true, TypeDef.DECIMAL, null,
                   new FieldBinding(new Accessor<ExecutionReport, BigDecimal>() {
                       @Override
                           public BigDecimal getValue(ExecutionReport obj) {
                           return obj.getCumQty();
                       }
                       @Override
                           public void setValue(ExecutionReport obj, BigDecimal value) {
                           obj.setCumQty(value);
                       }
               }, BigDecimal.class, null)),
               new FieldDef("execID", 17, true, TypeDef.STRING, null,
                   new FieldBinding(new Accessor<ExecutionReport, String>() {
                           @Override
                               public String getValue(ExecutionReport obj) {
                               return obj.getClOrdID();
                           }
                           @Override
                               public void setValue(ExecutionReport obj, String value) {
                               obj.setClOrdID(value);
                           }
                   }, String.class, null)),
               new FieldDef("lastPx", 31, false, TypeDef.DECIMAL, null,
                   new FieldBinding(new Accessor<ExecutionReport, BigDecimal>() {
                           @Override
                               public BigDecimal getValue(ExecutionReport obj) {
                               return obj.getLastPx();
                           }
                           @Override
                               public void setValue(ExecutionReport obj, BigDecimal value) {
                               obj.setLastPx(value);
                           }
                   }, BigDecimal.class, null)),
              new FieldDef("orderID", 37, true, TypeDef.STRING, null,
                   new FieldBinding(new Accessor<ExecutionReport, String>() {
                           @Override
                               public String getValue(ExecutionReport obj) {
                               return obj.getOrderID();
                           }
                           @Override
                               public void setValue(ExecutionReport obj, String value) {
                               obj.setOrderID(value);
                           }
                   }, String.class, null)),
              new FieldDef("ordStatus", 39, true, TypeDef.INT32, null,
                   new FieldBinding(new Accessor<ExecutionReport, Integer>() {
                           @Override
                               public Integer getValue(ExecutionReport obj) {
                               return obj.getOrdStatus();
                           }
                           @Override
                               public void setValue(ExecutionReport obj, Integer value) {
                               obj.setOrdStatus(value);
                           }
                   }, int.class, null)),
              new FieldDef("origClOrdID", 41, false, TypeDef.STRING, null,
                   new FieldBinding(new Accessor<ExecutionReport, String>() {
                           @Override
                               public String getValue(ExecutionReport obj) {
                               return obj.getOrigClOrdID();
                           }
                           @Override
                               public void setValue(ExecutionReport obj, String value) {
                               obj.setOrigClOrdID(value);
                           }
                   }, String.class, null)),
              new FieldDef("price", 44, false, TypeDef.DECIMAL, null,
                   new FieldBinding(new Accessor<ExecutionReport, BigDecimal>() {
                           @Override
                               public BigDecimal getValue(ExecutionReport obj) {
                               return obj.getPrice();
                           }
                           @Override
                               public void setValue(ExecutionReport obj, BigDecimal value) {
                               obj.setPrice(value);
                           }
                   }, BigDecimal.class, null)),
              new FieldDef("side", 54, true, TypeDef.INT32, null,
                   new FieldBinding(new Accessor<ExecutionReport, Integer>() {
                           @Override
                               public Integer getValue(ExecutionReport obj) {
                               return obj.getSide();
                           }
                           @Override
                               public void setValue(ExecutionReport obj, Integer value) {
                               obj.setSide(value);
                           }
                   }, int.class, null)),
              new FieldDef("symbol", 55, true, TypeDef.STRING, null,
                   new FieldBinding(new Accessor<ExecutionReport, String>() {
                           @Override
                               public String getValue(ExecutionReport obj) {
                               return obj.getSymbol();
                           }
                           @Override
                               public void setValue(ExecutionReport obj, String value) {
                               obj.setSymbol(value);
                           }
                   }, String.class, null)),
              new FieldDef("execType", 150, true, TypeDef.INT32, null,
                   new FieldBinding(new Accessor<ExecutionReport, Integer>() {
                           @Override
                               public Integer getValue(ExecutionReport obj) {
                               return obj.getExecType();
                           }
                           @Override
                               public void setValue(ExecutionReport obj, Integer value) {
                               obj.setExecType(value);
                           }
                   }, int.class, null)),
              new FieldDef("leavesQty", 151, true, TypeDef.DECIMAL, null,
                   new FieldBinding(new Accessor<ExecutionReport, BigDecimal>() {
                           @Override
                               public BigDecimal getValue(ExecutionReport obj) {
                               return obj.getLeavesQty();
                           }
                           @Override
                               public void setValue(ExecutionReport obj, BigDecimal value) {
                               obj.setLeavesQty(value);
                           }
                   }, BigDecimal.class, null))
              ),
            null, new GroupBinding(new Factory<ExecutionReport>() {
                @Override
                    public ExecutionReport newInstance() {
                    return new ExecutionReport();
                }
            },
                ExecutionReport.class)
            );
    }
}
