// File generated by hadoop record compiler. Do not edit.
/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.zookeeper_voltpatches.data;

import org.apache.jute_voltpatches.*;

import java.io.IOException;
import java.util.Comparator;

public class StatPersisted extends Record.AbstractRecord<StatPersisted> {
    private long czxid;
    private long mzxid;
    private long ctime;
    private long mtime;
    private int version;
    private int cversion;
    private int aversion;
    private long ephemeralOwner;
    private long pzxid;
    public StatPersisted() {
    }
    public StatPersisted(
            long czxid,
            long mzxid,
            long ctime,
            long mtime,
            int version,
            int cversion,
            int aversion,
            long ephemeralOwner,
            long pzxid) {
        this.czxid = czxid;
        this.mzxid = mzxid;
        this.ctime = ctime;
        this.mtime = mtime;
        this.version = version;
        this.cversion = cversion;
        this.aversion = aversion;
        this.ephemeralOwner = ephemeralOwner;
        this.pzxid = pzxid;
    }
    public long getCzxid() {
        return czxid;
    }
    public void setCzxid(long m_) {
        czxid = m_;
    }
    public long getMzxid() {
        return mzxid;
    }
    public void setMzxid(long m_) {
        mzxid = m_;
    }
    public long getCtime() {
        return ctime;
    }
    public void setCtime(long m_) {
        ctime = m_;
    }
    public long getMtime() {
        return mtime;
    }
    public void setMtime(long m_) {
        mtime = m_;
    }
    public int getVersion() {
        return version;
    }
    public void setVersion(int m_) {
        version = m_;
    }
    public int getCversion() {
        return cversion;
    }
    public void setCversion(int m_) {
        cversion = m_;
    }
    public int getAversion() {
        return aversion;
    }
    public void setAversion(int m_) {
        aversion = m_;
    }
    public long getEphemeralOwner() {
        return ephemeralOwner;
    }
    public void setEphemeralOwner(long m_) {
        ephemeralOwner = m_;
    }
    public long getPzxid() {
        return pzxid;
    }
    public void setPzxid(long m_) {
        pzxid = m_;
    }
    @Override
    public void serialize(OutputArchive a_, String tag) throws IOException {
        a_.startRecord(this,tag);
        a_.writeLong(czxid,"czxid");
        a_.writeLong(mzxid,"mzxid");
        a_.writeLong(ctime,"ctime");
        a_.writeLong(mtime,"mtime");
        a_.writeInt(version,"version");
        a_.writeInt(cversion,"cversion");
        a_.writeInt(aversion,"aversion");
        a_.writeLong(ephemeralOwner,"ephemeralOwner");
        a_.writeLong(pzxid,"pzxid");
        a_.endRecord(this,tag);
    }
    @Override
    public void deserialize(InputArchive a_, String tag) throws IOException {
        a_.startRecord(tag);
        czxid = a_.readLong("czxid");
        mzxid = a_.readLong("mzxid");
        ctime = a_.readLong("ctime");
        mtime = a_.readLong("mtime");
        version = a_.readInt("version");
        cversion = a_.readInt("cversion");
        aversion = a_.readInt("aversion");
        ephemeralOwner = a_.readLong("ephemeralOwner");
        pzxid = a_.readLong("pzxid");
        a_.endRecord(tag);
    }

    @Override
    public int compareTo(StatPersisted peer_) throws ClassCastException {
        return Comparator.comparingLong(StatPersisted::getCzxid)
                .thenComparingLong(StatPersisted::getMzxid)
                .thenComparingLong(StatPersisted::getCtime)
                .thenComparingLong(StatPersisted::getMtime)
                .thenComparingLong(StatPersisted::getVersion)
                .thenComparingLong(StatPersisted::getCversion)
                .thenComparingLong(StatPersisted::getAversion)
                .thenComparingLong(StatPersisted::getEphemeralOwner)
                .thenComparingLong(StatPersisted::getPzxid)
                .compare(this, peer_);
    }
    @Override
    public boolean equals(Object peer_) {
        return peer_ instanceof StatPersisted && equalsHelper(peer_);
    }
    @Override
    public int hashCode() {
        int result = 17;
        int ret;
        ret = (int) (czxid^(czxid>>>32));
        result = 37*result + ret;
        ret = (int) (mzxid^(mzxid>>>32));
        result = 37*result + ret;
        ret = (int) (ctime^(ctime>>>32));
        result = 37*result + ret;
        ret = (int) (mtime^(mtime>>>32));
        result = 37*result + ret;
        ret = version;
        result = 37*result + ret;
        ret = cversion;
        result = 37*result + ret;
        ret = aversion;
        result = 37*result + ret;
        ret = (int) (ephemeralOwner^(ephemeralOwner>>>32));
        result = 37*result + ret;
        ret = (int) (pzxid^(pzxid>>>32));
        return 37*result + ret;
    }
    public static String signature() {
        return "LStatPersisted(lllliiill)";
    }
}
