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

package org.apache.zookeeper_voltpatches.txn;

import org.apache.jute_voltpatches.*;
import org.apache.zookeeper_voltpatches.proto.SetDataRequest;

import java.io.IOException;
import java.util.Comparator;

public class CreateSessionTxn extends Record.AbstractRecord<CreateSessionTxn> {
    private long timeOut;
    public CreateSessionTxn() {
    }
    public CreateSessionTxn(long timeOut) {
        this.timeOut = timeOut;
    }
    public long getTimeOut() {
        return timeOut;
    }
    public void setTimeOut(long m_) {
        timeOut = m_;
    }
    @Override
    public void serialize(OutputArchive a_, String tag) throws IOException {
        a_.startRecord(this,tag);
        a_.writeLong(timeOut,"timeOut");
        a_.endRecord(this,tag);
    }
    @Override
    public void deserialize(InputArchive a_, String tag) throws IOException {
        a_.startRecord(tag);
        timeOut = a_.readLong("timeOut");
        a_.endRecord(tag);
    }

    @Override
    public int compareTo(CreateSessionTxn peer_) {
        return Comparator.comparingLong(CreateSessionTxn::getTimeOut).compare(this, peer_);
    }
    @Override
    public boolean equals(Object peer_) {
        return peer_ instanceof CreateSessionTxn && equalsHelper(peer_);
    }
    @Override
    public int hashCode() {
        int result = 17;
        int ret;
        ret = new Long(timeOut).hashCode();
        return 37*result + ret;
    }
    public static String signature() {
        return "LCreateSessionTxn(i)";
    }
}
