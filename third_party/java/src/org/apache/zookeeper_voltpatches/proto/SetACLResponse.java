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

package org.apache.zookeeper_voltpatches.proto;

import org.apache.jute_voltpatches.*;
import org.apache.zookeeper_voltpatches.data.Stat;

import java.io.IOException;

public class SetACLResponse implements Record, Comparable<SetACLResponse> {
    private Stat stat;
    public SetACLResponse() {
    }
    public SetACLResponse(Stat stat) {
        this.stat = stat;
    }
    public Stat getStat() {
        return stat;
    }
    public void setStat(Stat m_) {
        stat = m_;
    }
    @Override
    public void serialize(OutputArchive a_, String tag) throws IOException {
        a_.startRecord(this,tag);
        a_.writeRecord(stat,"stat");
        a_.endRecord(this,tag);
    }
    @Override
    public void deserialize(InputArchive a_, String tag) throws IOException {
        a_.startRecord(tag);
        stat = new Stat();
        a_.readRecord(stat,"stat");
        a_.endRecord(tag);
    }

    @Override
    public void writeCSV(CsvOutputArchive a) throws IOException {
        a.startRecord(this,"");
        a.writeRecord(stat,"stat");
        a.endRecord(this,"");
    }

    @Override
    public String toString() {
        return toStringHelper();
    }

    @Override
    public int compareTo (SetACLResponse peer_) {
        return stat.compareTo(peer_.getStat());
    }
    @Override
    public boolean equals(Object peer_) {
        if (! (peer_ instanceof SetACLResponse)) {
            return false;
        } else {
            return peer_ == this || compareTo((SetACLResponse) peer_) == 0;
        }
    }
    @Override
    public int hashCode() {
        int result = 17;
        int ret = stat.hashCode();
        return 37*result + ret;
    }
    public static String signature() {
        return "LSetACLResponse(LStat(lllliiiliil))";
    }
}
