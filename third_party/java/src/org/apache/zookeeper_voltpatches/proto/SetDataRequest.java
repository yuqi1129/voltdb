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

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class SetDataRequest implements Record, Comparable<SetDataRequest> {
    private String path;
    private byte[] data;
    private int version;
    public SetDataRequest() {
    }
    public SetDataRequest(String path, byte[] data, int version) {
        this.path=path;
        this.data=data;
        this.version=version;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String m_) {
        path = m_;
    }
    public byte[] getData() {
        return data;
    }
    public void setData(byte[] m_) {
        data = m_;
    }
    public int getVersion() {
        return version;
    }
    public void setVersion(int m_) {
        version = m_;
    }
    @Override
    public void serialize(OutputArchive a_, String tag) throws IOException {
        a_.startRecord(this,tag);
        a_.writeString(path,"path");
        a_.writeBuffer(data,"data");
        a_.writeInt(version,"version");
        a_.endRecord(this,tag);
    }
    @Override
    public void deserialize(InputArchive a_, String tag) throws IOException {
        a_.startRecord(tag);
        path = a_.readString("path");
        data = a_.readBuffer("data");
        version = a_.readInt("version");
        a_.endRecord(tag);
    }
    @Override
    public void writeCSV(CsvOutputArchive a) throws IOException {
        a.startRecord(this,"");
        a.writeString(path,"path");
        a.writeBuffer(data,"data");
        a.writeInt(version,"version");
        a.endRecord(this,"");
    }

    @Override
    public String toString() {
        return toStringHelper();
    }

    @Override
    public int compareTo(SetDataRequest peer) throws ClassCastException {
        int ret;
        ret = path.compareTo(peer.path);
        if (ret != 0) {
            return ret;
        }
        byte[] my = data;
        byte[] ur = peer.data;
        ret = Utils.compareBytes(my,0,my.length,ur,0,ur.length);
        if (ret != 0) {
            return ret;
        } else {
            return Comparator.comparingInt(SetDataRequest::getVersion).compare(this, peer);
        }
    }
    @Override
    public boolean equals(Object peer_) {
        if (! (peer_ instanceof SetDataRequest)) {
            return false;
        } else {
            return peer_ == this || compareTo((SetDataRequest) peer_) == 0;
        }
    }
    @Override
    public int hashCode() {
        int result = 17;
        int ret;
        ret = path.hashCode();
        result = 37*result + ret;
        ret = Arrays.toString(data).hashCode();
        result = 37*result + ret;
        ret = version;
        return 37*result + ret;
    }
    public static String signature() {
        return "LSetDataRequest(sBi)";
    }
}
